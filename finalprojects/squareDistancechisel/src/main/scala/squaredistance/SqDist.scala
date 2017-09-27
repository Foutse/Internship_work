// See LICENSE.txt for license details.
package squaredistance

import chisel3._
import chisel3.util._
import scala.collection.mutable.ArrayBuffer
import scala.util.Random
import scala.math.{min, max}
import chisel3.util.{DeqIO, EnqIO, log2Ceil}
import chisel3.experimental.FixedPoint


/**
  * calculating the The square distance of two vectors v1 and v2
  */

object GenFIR {
  def apply[T <: Data with Num[T]](v1: Vec[T], v2: Vec[T]): T = {
    val tmp = (v1 zip v2).map { case (x, y) => (y - x) * (y - x) }
    tmp.reduceLeft(_ + _)
  }
}

class SqDist(val n: Int, val fixedType: FixedPoint) extends Module {
  val io = IO(new Bundle {
    val in1 = Input(Vec(n, fixedType))
    val in2 = Input(Vec(n, fixedType))
    val out = Output(fixedType)

  })

   val start = System.nanoTime()
  io.out := GenFIR(io.in1, io.in2)
  val end = System.nanoTime()
  println(s"elapsed time = ${end-start}")
}

object SqDistDriver extends App {
  val fixedWidth = 64
  val binaryPoint = 32
  val n = 5
  chisel3.Driver.execute(args, () => new SqDist(n, FixedPoint(fixedWidth.W, binaryPoint.BP)))
}
