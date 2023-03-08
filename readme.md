# Spark Scala Examples
## Table of contents
1. [Introduction](#l3h1)
2. [Steps for Merging into GitHub Public Repository](#l3h2)
3. [build.sbt details](#l3h3)
4. [Setting up JVM (VM properties)](#l3h4)
5. [How Spark Configuration is set](#l3h5)
6. [How log4j properties are set](#l3h6)
7. [Steps for deploying Spark Application to Cluster and Submit it](#l3h7)
8. [Useful sbt commands](#l3h8)
9. [Scala Example details](#l3h9)
   1. [ReadingJsonFile](#l4h1)
   2. [ReadingCsvFile](#l4h2)
   3. [ReadingJsonFileUsingDS](#l4h3)
   4. [UdfExample](#l4h4)
   5. [SparkTableExample](#l4h5)


### Introduction  
<a id="l3h1"></a>
This project contains severals example Spark Applications. 
The following sections provides some key notes i made .
* [Spark API Doc Link](https://spark.apache.org/docs/2.4.0/api.html)
* SparkSession.builder() doesnt use new keyword for creating the object. this is due to the "apply method concept"


### Steps for Merging into GitHub Public Repository
<a id="l3h2"></a>
* I enabled git for the project in IntelliJ IDEA IDE
* use this link for [github credentials](https://ginnyfahs.medium.com/github-error-authentication-failed-from-command-line-3a545bfd0ca8)
* Follow below steps for merging into github repository

```
   git remote add origin https://github.com/rongali-naidu/sparkscalaexamples.git  
   git branch -M main
   git push -u origin main
```


### build.sbt details
<a id="l3h3"></a>
* [spark-core]( https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.13/3.3.1 )
* Above link shows the entries to be added for spark core dependencies
* "ThisBuild /" is prefixed to properties for those which are common across the sub-projects


### Setting up JVM (VM properties)
<a id="l3h4"></a>
* -Dlog4j.configuration=file:log4j.properties
* -Dspark.yarn.app.container.log.dir=app_logs
*  Note that spark.yarn.app.container.log.dir will be set for Yarn cluster but needs to be run for Spark local mode
* -Dlogfile.name=spark_scala


### How Spark Configuration is set
<a id="l3h5"></a>
* Precidence : 4 :  ${SPARK_HOME}/bin/spark-env.sh script , which gets invoked by spark_submit sets up some spark related environment variables
* Precidence : 3  :  ${SPARK_HOME}/conf/spark-defaults.conf will have default configurations
* Precidence : 2 : some configuration options are passed to spark-submit using --conf flag and other spark-command options like driver-memory. 
* Spark-submit merges spark-default.conf,spark environment variables and other values passed through --conf flags and sets those into the SparkSession object using SparkSession.set method
* Precidence : 1 : some configrations are set with in the application code using SparkConf object.
* if a property is set at multiple places, the option set at application code's SparkConf object takes highest precedence


### How log4j properties are set
<a id="l3h6"></a>
* note that we didnt set any sbt dependencies for log4j since it is included as transient dependency of spark-core
* log4j.properties file is included in the project root folder and passed as JVM parameter
* Other option to include log4j.properties is under resources folder
* Default [log4j.properties](https://docs.oracle.com/cd/E29578_01/webhelp/cas_webcrawler/src/cwcg_config_log4j_file.html)
* log4j.properties file is added under the project root folder.It has
necessary lo4j settings 


### Steps for deploying Spark Application to Cluster and Submit it
<a id="l3h7"></a>
* if there are multiple object classes with main methods, then add 
`Compile / packageBin / mainClass := Some("nr.spark.scala.examples.ReadingJsonFile")` setting in build.sbt file
* build using `package` in sbt console
* it will generate the application jar under "target/scala-<version> folder" . In this case, it created "sparkscalaexamples_2.13-1.0.0.jar"
* All the files and folder under resources folder(whch is under src folder) are copied under root folder of Jar file
* Since it didnt include the "data folder", we need to copy the people_newline_delimited.json manually to the cluster
* We also need to copy any other files which are referred in spark_submit , for ex, log4j.properties
* copy all the required files to the spark cluster node . 
  * one way is using scp. 
```
scp -P 2222 * cluster-user@cluster_name:/<directory_path>
```
  * scp needs destination port if its different from the default scp port
* login to cluster node . one way is using ssh.
* note that spark expects the data file either in HDFS or in cloud storage like SG or GFS
* Since we have the fie available locally, we need to upload it to HDFS stoage of the cluster
  * create hdfs folder 
```
hdfs dfs -mkdir /user/root/data
```
  * In this example, we will run the application as "root" user
  * copy the data from the local cluster storage to hdfs 
```
hdfs dfs -copyFromLocal <file_name> /user/root/data/
```
* submit the spark application
```
spark-submit 
   --verbose 
   --class nrspark.scala.examples.ReadJsonFile
   --files log4j.properties
   --conf 'spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j.properties -Dlogfile.name=Sample-app-driver'
   --conf 'spark.executor.extraJavaOptions=-Dlog4j.configuration=log4j.properties -Dlogfile.name=Sample-app-executor'
   --master yarn
   sparkscalaexamples_2.13-1.0.0.jar /user/root/data/<file_name>
```
* `--files` option copies the specified files onto the working directory of the driver
* we can display the contents of the log files onto the console using
```
yarn logs -applicationId application_<> -log_files Sample-app-driver.log
```


### Useful sbt commands
<a id="l3h8"></a>
* sbt test
* sbt package


### Scala Example details
<a id="l3h9"></a>
* The object classes are extended to Serializable
* In scala, Object classes supports Java static methods, static vaiables


#### ReadingJsonFile
<a id="l4h1"></a>
* This scala application reads from Json file,applies filter and writes output to a file
arguement set  = "data/people_newline_delimited.json"
* It writes to two different csv files. second write tries to consolidate the output into single file
* The write uses overrite folder setting for re-running cases


#### ReadingCsvFile
<a id="l4h2"></a>
* For this example, added two unit test cases. 
* unit tests are tested in "sbt shell" using `test` command
* Log output is pasted below 
```
23/02/27 21:22:18 INFO scala.examples.ReadingCsvFile$: This is beginning of the spark application
23/02/27 21:22:24 INFO scala.examples.ReadingCsvFile$: spark.conf=HashMap(spark.sql.warehouse.dir -> file:/home/naidu/workspace/SparkScalaExamples/spark-warehouse, spark.driver.port -> 44417, spark.app.name -> Reading_JSON_File, spark.yarn.app.container.log.dir -> app_logs, spark.driver.extraJavaOptions -> -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.nio.cs=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.util.calendar=ALL-UNNAMED --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED, spark.master -> local[3], spark.app.id -> local-1677561742249, spark.executor.extraJavaOptions -> -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.nio.cs=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.util.calendar=ALL-UNNAMED --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED, spark.driver.host -> 10.0.0.118, spark.app.startTime -> 1677561740033, spark.executor.id -> driver)
23/02/27 21:22:31 INFO scala.examples.ReadingCsvFile$: Job: Developer Count: 2
23/02/27 21:22:31 INFO scala.examples.ReadingCsvFile$: Job: Architect Count: 1
23/02/27 21:22:31 INFO scala.examples.ReadingCsvFile$: [Developer,2]->[Architect,1]
```

#### ReadingJsonFileUsingDS
<a id="l4h3"></a>
* This is similar to ReadingJsonFile but done using Dataset API
* Note that Dataframe is special case of Dataset ie Dataset[Row]
* we can convert Dataframe to Dataset with the help of case Class and spark.implicits._
* Advantage of Datasets : Supports Type Safe API 
* Disadvantage of Datasets : Looses some of the optimizations done for Row format
* Dataset[Row] format . Row object serialization is managed by Spark using Tungsten binary format (aka UnsafeRow)
* Dataset[CustomObject]. this custom object serialization is managed by builtin java serializers
* More on Spark Tungsten ginary format [Link1](https://spoddutur.github.io/spark-notes/deep_dive_into_storage_formats.html)
  [Link2](https://medium.com/@goyalsaurabh66/project-tungsten-and-catalyst-sql-optimizer-9d3c83806b63)

#### UdfExample
<a id="l4h4"></a>

* Create method (deriveStageOfLife)
* Register method with udf and use it in Column Expression
  * registering the udf function using signature of deriveStageOfLife
  * udf function registers udf and returns a refernece of the registered udf
  * registeration of the udf function will result in serialization of the function
  * and sending the function to executors
* Register the method with Catalog and use it in SQL expressions
  * Note that registering UDF is not enough to use the UDF in the SQL expression
  * we need to register with the Catalog using  spark.udf.register. This will register as SQL Function

#### SparkTableExample
<a id="l4h5"></a>


**How to enable Hive access in Spark?**

* Add additional dependency in build.sbt file
org.apache.spark %% spark-hive % sparkVerion

* add ".enableHiveSupport()" option while creating spark session. This options enables connection to Hive Catalog

**Comparing Spark catalog and engine on the lines of database?**

1) Database engine ... this is obvious and it is Spark Engine.
2) Metadata Catalog ... aka Metastore. Spark provides inmemory catalog by default . Also provides support to change it to persistent external catalog options like Hive metastore and AWS GLue Catalog.
3) Data Storage ... Managed and External. Managed tables are stored in the path specified by either hive.metastore.warehouse.dir vs spark.sql.warehouse.dir".

More on Metastores:

1) Spark In-memory Catalog. It is maintained for each session so its looses details after session ends.
2) Hive Catalog. we can enable spark to use Hive Catalog by setting "enableHiveSupport()" while creating spark session.[Databricks doc on enabling Hive metastore](https://docs.databricks.com/data/metastores/external-hive-metastore.html)
3) AWS GLue Catalog . Besides enabling Hive support, we need to set " hive.metastore.client.factory.class" to ""com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory""
   more details are available here .[AWS docs on enable AWS GLue catalog](https://docs.aws.amazon.com/emr/latest/ReleaseGuide/emr-spark-glue.html) ,
   [databricks doc on enable AWS GLue Catalog](https://docs.databricks.com/data/metastores/aws-glue-metastore.html)


**what happens when we add .enableHiveSupport() while creating spark session?**

* Enables Hive support in SparkSession (that makes sure that the Hive classes are on CLASSPATH and sets spark.sql.catalogImplementation internal configuration property to hive)

* When not configured by the hive-site.xml, SparkSession automatically creates metastore_db in the current directory and creates a directory configured by spark.sql.warehouse.dir, which defaults to the directory spark-warehouse in the current directory that the Spark application is started. This is what i observed while running the application in IntelliJ IDEA IDE.

* In a production environment, you can deploy a Hive metastore in two modes: local and remote.
* Local mode : The metastore client running inside a cluster connects to the underlying metastore database directly via JDBC.
* Remote mode : Instead of connecting to the underlying database directly, the metastore client connects to a separate metastore service via the Thrift protocol. The metastore service connects to the underlying database.
* for local mode, we need to set javax.jdo options and for remote mode, we need to set hive.metastore.uris option.
[Setting external-hive-metastore](https://docs.databricks.com/data/metastores/external-hive-metastore.html)


**How Spark stores data for the tables?**

Data can be stored externally (specified using location in DDL) or internally (managed tables) (spark.sql.warehouse.dir : Spark / Hive (data) warehouse HDFS folder)
Sparks default table storage format is parquet

Managed table:
for creating manged table, we need access to persistent catalog like Hive, GLue
Manged tables offer bucketing (Oracle hash partitions kind of feature)
Tables can be created using either dataframe API or Spark.SQL

```
df.write
.mode(SaveMode.Overwrite)
.saveAsTable("table_name")

creating external table:
df.write
.mode(SaveMode.Overwrite)
.option("path", "/path/to/external/table")
.saveAsTable("emp.employee")




External table using spark sql:

spark.sql("CREATE TABLE table_name (
col_name int,
...
)
LOCATION "data_file_location"
USING PARQUET")



spark.sql("CREATE DATABASE IF NOT EXISTS airline_db")

spark.catalog.setCurrentDatabase("airline_db")

spark.catalog.listTables("airline_db").show() //this shows all tables in the airline_db

```

In spark-shell, executed below commands

```


spark.conf.get("spark.sql.catalogImplementation")
hive

scala> spark.conf.getAll
res13: Map[String,String] = Map(spark.sql.warehouse.dir -> file:/home/naidu/spark-warehouse, spark.executor.extraJavaOptions -> -XX:+IgnoreUnrecognizedVMOptions --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.nio.cs=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.util..

scala> spark.catalog.currentDatabase
res16: String = default


spark.catalog.listDatabases().foreach(r => print(r.name))


```

** what is --conf spark.logConf=true option? **

**hive.metastore.warehouse.dir vs spark.sql.warehouse.dir?**
* hive.metastore.warehouse.dir is used to specify the default HDFC location on the cluster where Hive stores table data.
spark.sql.warehouse.dir is used to specify the default HDFC location on the cluster where Spark stores table data.
* While using AWS Glue , we can use the hive-site configuration classification to specify a location in Amazon S3 for hive.metastore.warehouse.dir, which applies to all Hive tables
* When Hive Metastore is used with Spark, the property used by Spark to determine the default location for managed tables is hive.metastore.warehouse.dir. However, if you override this property in Spark's configuration using spark.sql.warehouse.dir, Spark will use the latter property instead. It's worth noting that spark.sql.warehouse.dir also affects other Spark components, such as the default location for checkpointing and global temporary views.
* Note that the hive.metastore.warehouse.dir property in hive-site.xml is deprecated since Spark 2.0.0. Instead, use spark.sql.warehouse.dir to specify the default location of database in warehouse.
[sql-data-sources-hive-tables](https://spark.apache.org/docs/latest/sql-data-sources-hive-tables.html)
[how-to-connect-spark-to-remote-hive](https://sparkbyexamples.com/apache-hive/how-to-connect-spark-to-remote-hive/)
* Alternatively, If you don’t have hive-site.xml, you can also create SparkSession by pointing hive.metastore.uris to the remote Hive cluster metastore. In the below, change the IP address and Port according to your Hive metastore server address and po
```
import org.apache.spark.sql.SparkSession

// Create Spark Session with Hive enabled
val spark = SparkSession.builder().master("local[*]")
.appName("SparkByExamples.com")
.config("hive.metastore.uris", "thrift://192.168.1.190:9083")
.config("spark.sql.warehouse.dir","/users/hive/warehouse")
.enableHiveSupport()
.getOrCreate()
    `
```
