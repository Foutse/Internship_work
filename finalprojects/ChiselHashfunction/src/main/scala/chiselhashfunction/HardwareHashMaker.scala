
package chiselhashfunction

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.KnownBinaryPoint
import chisel3.iotesters.PeekPokeTester


/**
  * This class creates a hash function for keys of a particular size and depth
  * It creates a fixed table at construction time of a keySize X tableRows of doubles
  * @param keySize     number of doubles in the key
  * @param tableRows   depth of the hash
  * @param w           divisor
  * @param b           offset
  * @param weights     coefficients used for hash calculations
  */
class HashMaker(keySize: Int, tableRows: Int, w: Double, b: Double, weights: Array[Array[Double]]) {
  /**
    * compute key for hash
    * multiplies x element wise against each row of weights, scales each of these sums then adds them together
    * @param key  the key to compute a hash for
    * @return
    */
  def apply(key: Array[Double]): Double = {
    assert(key.length == keySize, s"Error: HashMaker with keySize $keySize got key ${key.mkString(",")}")

    val intermediateSums = weights.indices.map { rowNum =>
      val rowSum = key.zip(weights(rowNum)).foldLeft(0.0) { case (accumulator, (xAtIndex, vectorAtIndex)) =>
        accumulator + (xAtIndex * vectorAtIndex)
      }
      (rowSum + b) / w
    }
    intermediateSums.reduce(_ + _)
  }
}

/**
   * This is a simple hardware version of the above HashMaker
   */ 
class HardwareHashMaker(val fixedType: FixedPoint,
                        val keySize: Int,
                        val hashDepth: Int,
                        val w: Double,
                        val b: Double,
                        val weights: Array[Array[Double]]) extends Module {
  val io = IO(new Bundle {
    val x   = Input(Vec(keySize, fixedType))
    val out = Output(fixedType)
  })

  // convert the divisor into a settable binary point, kind of like a shift

  assert((math.log(w) / math.log(2)).toInt.toDouble == (math.log(w) / math.log(2)), "w $w is not a power of 2")
  private val shiftDivider = (math.log(w) / math.log(2)).toInt
  private val newBinaryPoint = fixedType.binaryPoint match {
    case KnownBinaryPoint(n) => n + shiftDivider + 4
    case _ => throw new Exception(s"binary point of fixedPoint for HashFunction must be known")
  }
  println(s"Shift divider $shiftDivider newBinaryPoint $newBinaryPoint")

  // create a hashDepth X keySize table of random numbers with a gaussian distribution

  private val tabHash0 = weights.map(_.map(_.F(fixedType.getWidth.W, fixedType.binaryPoint)))

  private val offset = b.F(fixedType.binaryPoint)

  private val intermediateSums = (0 until hashDepth).map { ind1 =>
    val sum: FixedPoint = io.x.zip(tabHash0(ind1)).foldLeft(0.F(fixedType.binaryPoint)) { case (accum, (x, t)) =>
      accum + (x * t)
    }

    (sum + offset).setBinaryPoint(newBinaryPoint)
//    ((sum + offset).asUInt() >> shiftDivider).asFixedPoint(fixedType.binaryPoint)
  }

  io.out := intermediateSums.reduce(_ + _)

}
