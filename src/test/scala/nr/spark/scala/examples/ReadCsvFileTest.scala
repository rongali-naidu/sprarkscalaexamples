package nr.spark.scala.examples

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import nr.spark.scala.examples.ReadingCsvFile.{countByJob, readSource}

import scala.collection.mutable

class ReadCsvFileTest extends AnyFunSuite with BeforeAndAfterAll {

  @transient var spark:SparkSession = _

  override def beforeAll():Unit = {
    val sparkAppConf = new SparkConf()
    sparkAppConf.set("spark.app.name","Reading_JSON_File")
    sparkAppConf.set("spark.master","local[3]")

    spark = SparkSession.builder()
      .config(sparkAppConf)
      .getOrCreate()
  }

  override def afterAll():Unit = {
    spark.stop()
  }
  test("Testing the record count") {
    val sourceDF = readSource(spark,"src/main/resources/people.csv")
    val recCount = sourceDF.count()
    assert(recCount == 3, "Record count should be 3")
  }

  test("Testing Output") {
    val sourceDF = readSource(spark,"src/main/resources/people.csv")
    val outDF = countByJob(sourceDF)
    val jobCountMap = new mutable.HashMap[String,Long]
    outDF.collect().foreach(row => jobCountMap.put(row.getString(0),row.getLong(1)))
    assert(jobCountMap("Developer") == 2, "Developer job should have 2")
    assert(jobCountMap("Architect") == 1, "Architect job should have 2")
  }

}
