package msLsh
//import chisel3._
//import chisel3.util.{DeqIO, EnqIO, log2Ceil}
import scala.collection.mutable.{ArrayBuffer, ListBuffer, HashMap}
import scala.util.Random
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import scala.io.Source
import java.io.FileOutputStream
import java.io.File
//import org.apache.spark.SparkContext
//import org.apache.spark.SparkContext._
//import org.apache.spark.SparkConf
//import org.apache.spark.mllib.linalg.{Vector, Vectors}



object Main {
  def main(args: Array[String]): Unit = {
    //val tutArgs = args.slice(1, args.length)
	//val sc = new SparkContext(new SparkConf)

	val meanShift = msLsh.MsLsh
        // val file = Source.fromFile(args)(scala.io.Codec.ISO8859)
	val data = io.Source.fromFile(args(0)).getLines().map(_.split(",").map(_.toDouble))
					.zipWithIndex
					.map{ case(data, id) => (id.toLong, data)}.toArray //.cache
	//printf("size = %d\n", data.size)

	//val model = meanShift.train(sc=4, data, k=15, epsilon1=args(1).toDouble, epsilon2=args(2).toDouble, epsilon3=args(3).toDouble, ratioToStop=1.0, yStarIter=args(4).toInt, cmin=args(5).toInt, normalisation=args(6).toBoolean, w=1, nbseg=100, nbblocs1=args(7).toInt, nbblocs2=args(8).toInt, nbLabelIter=args(9).toInt)  

         val model = meanShift.train(sc=2, data, k=60, epsilon1=0.001, epsilon2=0.05, epsilon3=0.06, ratioToStop=0.01, yStarIter=10, cmin=0, normalisation=true, w=1, nbseg=100, nbblocs1=50, nbblocs2=50, nbLabelIter=1)

	val nbd = data.length
	val nbd2 = model.head.clustersCardinalities.values.reduce(_+_)

	println("nbd : " + nbd + "\nnbd2 : " + nbd)

	model.head.clustersCardinalities.foreach(println)
	//meanShift.savelabeling(model(0),"/myPath/label")
	//meanShift.saveClusterInfo(model(0),"/myPath/clusterInfo")
        meanShift.saveImageAnalysis(model.head, "/users/yuehgoh/ChiselWork/MyChiselProject9",1)
        meanShift.savelabeling(model.head,"//users/yuehgoh/ChiselWork/MyChiselProject9/label",1)
	meanShift.saveClusterInfo(model.head,"//users/yuehgoh/ChiselWork/MyChiselProject9/clusterInfo")


	}




}
 
