package nr.spark.scala.examples

import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.{Dataset, Row, SparkSession}

case class Person(name:String, age:Long)

object ReadingJsonFileUsingDS extends Serializable {

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

    //Dataframe is a dataset with ROw data type
    val sourceDF:Dataset[Row] = spark.read.format("json").load(args(0))
    sourceDF.printSchema()
    //sourceDF.filter("ageValue > 20").show() //org.apache.spark.sql.AnalysisException: Column 'ageValue' does not exist

    //we can convert thos dataframe to DS
    //It requires case Class and converting Row Object to the case Class object
    //below conversion is handled using a function in implicits so import it
    import spark.implicits._
    val sourceDS:Dataset[Person] = sourceDF.select("name","age").as[Person]
    sourceDF.printSchema()

    //sourceDF.show(5)
    //sourceDS.filter("ageValue  < 20").show() //Run time binding API : No error while compiling
    //sourceDS.filter(row => row.Agevalue < 20) //Type Safe API...ttrowed..Compile time error : "Cannt resolve symbol AgeValue
    sourceDS.filter(row => row.age < 20).show()

    //Dataset[Row] format . Row object serialization is managed by Spark using Tungsten binary format (aka UnsafeRow)
    //Dataset[CustomObject]. this custom object serialization is managed by builtin java serializers
    //https://spoddutur.github.io/spark-notes/deep_dive_into_storage_formats.html
    //https://medium.com/@goyalsaurabh66/project-tungsten-and-catalyst-sql-optimizer-9d3c83806b63

    sourceDF.write.mode("overwrite").format("csv").option("header","true").option("overwrite","true").save("data/persons.csv")
    //sourceDF.write.mode("overwrite").format("csv").option("header","true").option("overwrite","true").partitionBy("col1","col2").save("data/persons.csv")
    sourceDF.coalesce(1).write.mode("overwrite").format("csv").option("header","true").save("data/persons_single_file.csv")
    logger.info("This is ending of the spark application")
    spark.stop()

  }


}
