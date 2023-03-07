package nr.spark.scala.examples

import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, expr, udf}

object UdfExample extends Serializable {

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

    val sourceDF = spark.read.format("json").load(args(0))

    sourceDF.show(5)

    //registering the udf function using signature of deriveStageOfLife
    //udf function registers udf and returns a refernece of the registered udf
    //registeration of the udf function will result in serialization of the function
    //and sending the function to executors
    val deriveStageOfLifeUdf = udf(deriveStageOfLife(_:Long):String)

    //using udf in Column object expression
    val modDF = sourceDF.withColumn("stageOfLife",deriveStageOfLifeUdf(col("age")))
    //error in calling UDF (deriveStageOfLife(col("age")) :Type mismatch. Required: Long, found: Column
    //solution : register the UDF and call it using the UDF reference deriveStageOfLifeUdf

    modDF.show()

    //registering UDF with catalog.
    //Note that registering UDF is not enough to use the UDF in the SQL expression
    //we need to register with the Catalog. This will register as SQL Function
    spark.udf.register("deriveStageOfLifeUdf",deriveStageOfLife(_:Long):String)

    //cross checking if the function is registered with Catalog
    spark.catalog.listFunctions().filter(row => row.name == "deriveStageOfLifeUdf").show()

    //using udf in Column object expression
    //Note that withColumn requires Column object so used expr on top of the SQL expression
    val mod2DF = sourceDF.withColumn("stageOfLife",expr("deriveStageOfLifeUdf('age')"))
    mod2DF.show()

    //sourceDF.write.mode("overwrite").format("csv").option("header","true").option("overwrite","true").save("data/persons.csv")
    //sourceDF.write.mode("overwrite").format("csv").option("header","true").option("overwrite","true").partitionBy("col1","col2").save("data/persons.csv")
    //sourceDF.coalesce(1).write.mode("overwrite").format("csv").option("header","true").save("data/persons_single_file.csv")
    logger.info("This is ending of the spark application")
    spark.stop()
  }

  def deriveStageOfLife(age:Long) : String = {
    age match {
      case x if x<1 => "infant"
      case x if x<3 => "toddler"
      case x if x<9 => "child"
      case x if x<18 => "teenage"
      case _ => "adult"
    }
  }

}
