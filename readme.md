# Spark Scala Examples

### Introduction
* [Spark API Doc Link](https://spark.apache.org/docs/2.4.0/api.html)
* SparkSession.builder() doesnt use new keyword for creating the object. this is due to the "apply method concept"

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

### Useful sbt commands
* sbt compile
* sbt build

### Scala Example details
* The object classes are extended to Serializable
* In scala, Object classes supports Java static methods, static vaiables
* 
#### ReadingJsonFile
This scala application reads from Json file,applies filter and writes output to a file
arguement set  = "data/people_newline_delimited.json"

#### ConvertJsonToParquet
This application reads Json file and converts to Parquet file




