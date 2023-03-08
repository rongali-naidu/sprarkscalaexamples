import sbt.Keys.libraryDependencies

ThisBuild / scalaVersion := "2.13.10"
ThisBuild / organization := "nr.spark.scala.examples"
ThisBuild / version      := "1.0.0"
// set the main class for packaging the main jar
Compile / packageBin / mainClass := Some("nr.spark.scala.examples.ReadingJsonFile")

//https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.13/3.3.1
//Above url shows groupId,ArtifactId,revision to use
//spark-core_2.13 conveys that this package is build using scala 2.13

val sparkVersion = "3.3.1"

val devDependencies = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
)

//https://mvnrepository.com/artifact/org.scalatest/scalatest_3/3.2.15
val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.2.15" % Test
)



lazy val root = (project in file("."))
  .settings(
    name := "SparkScalaExamples",
    libraryDependencies ++= devDependencies ++ testDependencies,
    autoScalaLibrary  := false ,
  //  mainClass := Some("nr.spark.scala.examples.ReadingJsonFile")
    //settings will add scala library to depdencies
    //we dont need separate scala library downloaded since spark core contains scala library

  )



