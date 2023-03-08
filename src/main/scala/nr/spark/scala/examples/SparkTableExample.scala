package nr.spark.scala.examples


import nr.spark.scala.examples.ReadingJsonFile.getClass
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.{SaveMode, SparkSession}

object SparkTableExample extends Serializable {


  @transient lazy val logger:Logger = Logger.getLogger(getClass.getName)

  def main(args:Array[String]): Unit = {

    if (args.length == 0) {
      logger.error("Usage ReadingJsonFile FileName")
      System.exit(1)
    }

    logger.info("This is beginning of the spark application")
    val sparkAppConf = new SparkConf()
    sparkAppConf.set("spark.app.name","Reading_JSON_File")
    sparkAppConf.set("spark.master","local[3]")

    val spark = SparkSession.builder()
      .config(sparkAppConf)
      .enableHiveSupport()
      .getOrCreate()

    val sourceDF = spark.read.format("json").load(args(0))

    sourceDF.show(5)

    spark.sql("CREATE DATABASE IF NOT EXISTS HR_DB")
    spark.catalog.setCurrentDatabase("HR_DB")

    sourceDF.write
      .mode(SaveMode.Overwrite)
      //.partitionBy("age")
      //.bucketBy(4,"age")
      //.sortBY("age")
      .saveAsTable("person_dtls")

    spark.catalog.listTables("HR_DB")
    spark.table("person_dtls").show()
    spark.read.table("person_dtls").show()
    logger.info("This is ending of the spark application")
    spark.stop()

  }

}
