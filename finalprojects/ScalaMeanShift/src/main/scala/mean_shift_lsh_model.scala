package msLsh
//import chisel3._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.Sorting.quickSort
import scala.math.{min, max}
//import chisel3.util.{DeqIO, EnqIO, log2Ceil}
import scala.collection.mutable.{ArrayBuffer, ListBuffer, HashMap}
import scala.util.Random
//import org.apache.spark.mllib.linalg.Vector
//import org.apache.spark.rdd.RDD
//import scala.collection.mutable.ListBuffer

class Mean_shift_lsh_model(val clustersCenter:Map[Int, Array[Double]], val clustersCardinalities:scala.collection.Map[Int, Int], val labelizedRDD:Array[(Int,(Long, Array[Double]))] ,val maxMinArray:(Array[Double], Array[Double])) extends Serializable
{
  def numCluster : Int = clustersCenter.size

  def predict(point:Array[Double]) : Int = MsLsh.prediction(point, clustersCenter)

  def predict(points:Array[Array[Double]]) : ListBuffer[Int] = {
    val res = ListBuffer.empty[Int]
    for( ind <- 0 until points.size) res += MsLsh.prediction(points(ind), clustersCenter)
    res
  }
}
