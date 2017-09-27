// See LICENSE for license details.

package squaredistance

import chisel3._
import chisel3.iotesters
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.KnownBinaryPoint
import chisel3.iotesters.PeekPokeTester
import org.scalatest.{FreeSpec, Matchers}
import chisel3.iotesters.{ChiselFlatSpec, Driver, PeekPokeTester}
import scala.util.Random

object SquareDistance {
  def apply(vector1: Seq[Double], vector2: Seq[Double]): Double = {
    assert(vector1.length == vector2.length)

    (vector1 zip vector2).map { case (x, y) => (y - x) * (y - x) }.reduceLeft(_ + _)
  }
}

class SquareDistanceHardwareTester(c: SqDist) extends PeekPokeTester(c) {
  def pokeFixedPoint(signal: FixedPoint, value: Double): Unit = {
    val bigInt = value.F(signal.binaryPoint).litValue()
    poke(signal, bigInt)
  }
  def peekFixedPoint(signal: FixedPoint): Double = {
    val bigInt = peek(signal)
    signal.binaryPoint match {
      case KnownBinaryPoint(bp) => FixedPoint.toDouble(bigInt, bp)
      case _ => throw new Exception("Cannot peekFixedPoint with unknown binary point location")
    }
  }

  for(trial <- 0 to 20) {

    val v1 = Seq.tabulate(150) { x => Random.nextDouble() } //Array(2.3,5.0,2.6,0.9).toSeq //
    val v2 = Seq.tabulate(150) { x => Random.nextDouble() } //Array(1.3,2.0,9.6,3.9).toSeq //

    v1.zipWithIndex.foreach { case (value, index) => pokeFixedPoint(c.io.in1(index), value) }
    v2.zipWithIndex.foreach { case (value, index) => pokeFixedPoint(c.io.in2(index), value) }

    step(1)
    val start1 = System.nanoTime()
    val hardwareResult = peekFixedPoint(c.io.out)
    val end1 = System.nanoTime()
    val start2 = System.nanoTime()
    val softwareResult = SquareDistance(v1, v2)
    val end2 = System.nanoTime()
    val difference = ((hardwareResult - softwareResult).abs / softwareResult ) * 100.0
    println(f"$trial%2d hardware $hardwareResult%10.8f    software $softwareResult%10.8f    " +
      f"error $difference%8.6f %%")
    println(f"$trial%2d Elapsed timeH = ${end1-start1} and timeS = ${end2-start2} ")
  }
}
/*class SquareDistanceSpec extends FreeSpec with Matchers {
  "hardware should match software up to some precision difference" - {
    "software testing with 4-vector" in {
      val v1 = Array(2.3,5.0,2.6,0.9).toSeq //Seq.tabulate(5) { x => x.toDouble }
      val v2 = Array(1.3,2.0,9.6,3.9).toSeq.reverse //Seq.tabulate(5) { x => x.toDouble }.reverse
      println(s"result is ${SquareDistance(v1, v2)}")
    }
    "hardware testing with 4-vector" in {
     iotesters.Driver.execute(Array.empty[String], () => new SqDist(10, FixedPoint(32.W, 16.BP))) { c =>
        new SquareDistanceHardwareTester(c)
      }
    }
   }
}*/
class SquareDistanceSpec extends ChiselFlatSpec {
 private val backendNames = Array[String]("firrtl", "verilator")
  for ( backendName <- backendNames ) {
    "SqDist" should s"calculate proper square distance of two vectors (with $backendName)" in {
 Driver(() => new SqDist(150, FixedPoint(64.W, 16.BP)), backendName) { c =>
        new SquareDistanceHardwareTester(c)
      }should be (true)
   }
 }
}

