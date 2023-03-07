# Spark Scala Examples

### Introduction
This project contains severals example Spark Applications. 
The following sections provides some key notes i made .
* [Spark API Doc Link](https://spark.apache.org/docs/2.4.0/api.html)
* SparkSession.builder() doesnt use new keyword for creating the object. this is due to the "apply method concept"


### Steps for Merging into GitHub Public Repository
* I enabled git for the project in IntelliJ IDEA IDE
* use this link for [github credentials](https://ginnyfahs.medium.com/github-error-authentication-failed-from-command-line-3a545bfd0ca8)
* Follow below steps for merging into github repository

```
   git remote add origin https://github.com/rongali-naidu/sparkscalaexamples.git  
   git branch -M main
   git push -u origin main
```


### build.sbt details
* [spark-core]( https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.13/3.3.1 )
* Above link shows the entries to be added for spark core dependencies
* "ThisBuild /" is prefixed to properties for those which are common across the sub-projects

### Setting up JVM (VM properties)
* -Dlog4j.configuration=file:log4j.properties
* -Dspark.yarn.app.container.log.dir=app_logs
*  Note that spark.yarn.app.container.log.dir will be set for Yarn cluster but needs to be run for Spark local mode
* -Dlogfile.name=spark_scala

### How Spark Configuration is set
* Precidence : 4 :  ${SPARK_HOME}/bin/spark-env.sh script , which gets invoked by spark_submit sets up some spark related environment variables
* Precidence : 3  :  ${SPARK_HOME}/conf/spark-defaults.conf will have default configurations
* Precidence : 2 : some configuration options are passed to spark-submit using --conf flag and other spark-command options like driver-memory. 
* Spark-submit merges spark-default.conf,spark environment variables and other values passed through --conf flags and sets those into the SparkSession object using SparkSession.set method
* Precidence : 1 : some configrations are set with in the application code using SparkConf object.
* if a property is set at multiple places, the option set at application code's SparkConf object takes highest precedence

### How log4j properties are set
* note that we didnt set any sbt dependencies for log4j since it is included as transient dependency of spark-core
* log4j.properties file is included in the project root folder and passed as JVM parameter
* Other option to include log4j.properties is under resources folder
* Default [log4j.properties](https://docs.oracle.com/cd/E29578_01/webhelp/cas_webcrawler/src/cwcg_config_log4j_file.html)
* log4j.properties file is added under the project root folder.It has
necessary lo4j settings 

### Steps for deploying Spark Application to Cluster and Submit it
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
* sbt test
* sbt package

### Scala Example details
* The object classes are extended to Serializable
* In scala, Object classes supports Java static methods, static vaiables
* 
#### ReadingJsonFile
* This scala application reads from Json file,applies filter and writes output to a file
arguement set  = "data/people_newline_delimited.json"
* It writes to two different csv files. second write tries to consolidate the output into single file
* The write uses overrite folder setting for re-running cases


#### ReadingCsvFile
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
* This is similar to ReadingJsonFile but done using Dataset API
* Note that Dataframe is special case of Dataset ie Dataset[Row]
* we can convert Dataframe to Dataset with the help of case Class and spark.implicits._
* Advantage of Datasets : Supports Type Safe API 
* Disadvantage of Datasets : Looses some of the optimizations done for Row format
* Dataset[Row] format . Row object serialization is managed by Spark using Tungsten binary format (aka UnsafeRow)
* Dataset[CustomObject]. this custom object serialization is managed by builtin java serializers
* More on Spark Tungsten ginary format [Link1](https://spoddutur.github.io/spark-notes/deep_dive_into_storage_formats.html)
  [Link2](https://medium.com/@goyalsaurabh66/project-tungsten-and-catalyst-sql-optimizer-9d3c83806b63)



