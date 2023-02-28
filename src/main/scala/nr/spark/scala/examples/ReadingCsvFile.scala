package nr.spark.scala.examples

import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.SparkSession

object ReadingCsvFile {

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
    .getOrCreate()

    logger.info("spark.conf=" + spark.conf.getAll.toString())

    val sourceDF = readSource(spark,args(0))

    val countDF = countByJob(sourceDF)

    countDF.foreach(row => {
      logger.info("Job: " + row.getString(0) + " Count: " + row.getLong(1))
    })

    logger.info(countDF.collect().mkString("->"))


  }

  def readSource(spark:SparkSession, fileName:String):DataFrame = {
    spark.read.format("csv")
      .option("header","true")
      .option("inferSchema","true")
      .load(fileName)
  }

  def countByJob(df: DataFrame):DataFrame = {
    df.select("name","age","job")
      .where("age > 20")
      .groupBy("job")
      .count()
  }

}
