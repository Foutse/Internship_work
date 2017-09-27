
// See LICENSE.txt for license details.
//package example
package msLsh
//import chisel3._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.util.Sorting.quickSort
import scala.math.{min, max}

object Fcts extends Serializable {
//Create a tab with random vector where component are taken on normal law N(0,1) for LSH
 //abstract class tabHash (val nb:Int, val dim:Int, val a:Int) extends Module {
  def tabHash ( nb:Int, dim:Int) : Array[Array[Double]]={
 val tabHash0 = new Array[Array[Double]](nb)
    for( ind <- 0 until nb) {
      val vechash1 = new Array[Double](dim)
      for( ind2 <- 0 until dim) vechash1(ind2) = Random.nextGaussian
      tabHash0(ind) = vechash1
    }
    tabHash0
  }


  def sqdist (u: Array[Double], v: Array[Double]) : Double ={
     var dist=0.0
      //var n =v1.size
      for( i <-0 until u.size){
        dist+=(u(i)-v(i))*(u(i)-v(i))
        }
     dist
}
/*
///////////////////////////////////////////////
//Copute hashCode
///////////////////////////////////////////////
*/
/*
def hashCode(x: Any): Int = {
  var code = x.hashCode()
  //val arr =  x.productArity
  //var i = 0
  //while (i < arr) {
    //val elem = x.productElement(i)
    code = code * 41 + (if (elem == null) 0 else elem.hashCode())
    //i += 1
  //}
  code
}
*/
def nonNegativeMod(x:Int, mod:Int): Int ={
	val rawMod =x % mod
    	rawMod + (if (rawMod<0) mod else 0)
}
/*
////////////////////////////////////////////////
//desence function
//////////////////////////////////////////
*/
 def dense(firstValue: Double, otherValues: Double*): Array[Double] = {
    val tmp = firstValue +: otherValues 
    tmp.toArray
 }

 def dense1(values: Double*): Array[Double] = {
    values.toArray
 }
/**
   *  Generate the hash value for a given vector x depending on w, b, tabHash1
   */
//object Hashfunc {
def hashfunc( x : Array[Double],w:Double,  b:Double, tabHash1: Array[Array[Double]] ) : Double={
    //val x = new Array[Double](n)
    //val tabHash1= new Array[Array[Double]](n)
    val tabHash = new Array[Double](tabHash1.size)
    for( ind <- tabHash1.indices) {
      var sum = 0.0
      for( ind2 <- 0 until x.size ) {
        sum += ( x(ind2) * tabHash1(ind)(ind2) )
        }     
      tabHash(ind) = (sum + b) / w
    }
    tabHash.reduce(_ + _)
  }
//}
 
 /**
   * Function which compute centroïds
   */
//object ComputeCentroid{
 /* def computeCentroid(tab : Array[(Array[Double], Double)], k:Int) : Array[Double]={
   // val tab = new Array[Array[Double]](n)
   // val vectors = tab.map(_._1)
    val vectorsReduct = tab.reduce(_+_)
    val centroid = vectorsReduct.map(_ / k)
    //Vectors.dense(centroid)
    centroid
  }
*/
 def add(a:Array[Double],b:Array[Double]):Array[Double] = {
    var res = Array.ofDim[Double](a.length)
    for (i <- 0 to a.length-1) {
      res(i) += a(i) + b(i)
    }
    res
  }

  def computeCentroid(tab : Array[(Array[Double], Double)], k:Int) : Array[Double]={
   // val tab = new Array[Array[Double]](n)
    val vectors = tab.map(_._1)
    val vectorsReduct = vectors.reduceLeft[Array[Double]]{
      (a,b) => add(a,b)
    }
    val centroid = vectorsReduct.map(_ / k)
    centroid
 }
/*
def add1(a:Array[Double],b:Long):Array[Double] = {
    var res = Array.ofDim[Double](a.length)
    for (i <- 0 to a.length-1) {
      res(i) += a(i) + b
    }
    res
  }
*/

 //} 

  /**
   * Scale data to they fit on range [0,1]
   * Return a tuple where :
   *   First element is the scale rdd
   *   Second element is the array of max value for each component
   *   Third element is the array of min value for each component
   * Theses array are used in order to descale RDD
   */
  def scaleRdd(rdd1:Array[(Long, Array[Double])]) : (Array[(Long,Array[Double])], Array[Double], Array[Double]) = {
   // rdd1.cache //putting in memory
    val vecttest = rdd1.head
    val size1 = vecttest._2.size
    //val size1 = vecttest.size
//rdd1=array[(long, array[long])] 

    val minMaxArray = rdd1.map{ case (id, vector) => vector.toArray.map(value => (value, value))}.reduce( (v1, v2) => v1.zip(v2).map{ case (((min1, max1), (min2, max2))) => (min(min1, min2), max(max1, max2))})

    val minArray = minMaxArray.map{ case ((min, max)) => min }
    val maxArray = minMaxArray.map{ case ((min, max)) => max }

    val rdd2 = rdd1.map{ case (id, vector) => {
      val tabcoord = new Array[Double](size1)
      for( ind <- 0 until size1) {
        val coordXi = ( vector(ind) - minArray(ind) ) / (maxArray(ind) - minArray(ind))
        tabcoord(ind) = coordXi
      }
      (id, tabcoord)
    }}
    (rdd2, maxArray, minArray)
  }

  /**
   * Restore centroid's original value
   */
  def descaleRDDcentroid(rdd1:Array[(Int,Array[Double])], maxMinArray:(Array[Double], Array[Double])) : Array[(Int,Array[Double])] = {
    val vecttest = rdd1.head
    //val size1 = vecttest.size
    val size1 = vecttest._2.size
    val maxArray = maxMinArray._1
    val minArray = maxMinArray._2
    val rdd2 = rdd1.map{ case (label, vector) => {
      val tabcoord = new Array[Double](size1)
      for( ind <- 0 until size1) {
        val coordXi = vector(ind) * ( maxArray(ind) - minArray(ind) ) + minArray(ind)
        tabcoord(ind) = coordXi
      }
      (label, tabcoord)         
    }}
    rdd2
  }	

}
