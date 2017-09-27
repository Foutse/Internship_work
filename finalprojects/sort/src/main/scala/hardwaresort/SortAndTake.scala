package hardwaresort

import chisel3._
import chisel3.experimental.FixedPoint
import chisel3.internal.firrtl.KnownBinaryPoint
import chisel3.iotesters.PeekPokeTester
import chisel3.util.log2Ceil

//scalastyle:off magic.number
/**
  * Implements a naive and non-optimized hardware sort of FixedPoint numbers.  Sorts the inputs and returns either
  * the outputSize highest or lowest depending on reverseSort.
  * This has a very primitive flow control.  Parent sets when inputs are to be read, and then should wait until
  * sort is complete.  Sort may be complete before circuit realizes it.  (this could be fixed)
  *
  * Basic stragey is to copy inputs to a vector
  * then adjacent registers and flip them if the first is greater than the second
  * When selecting register pairs on even cycles compare 0 to 1, 2 to 3... on odd cycles compare 1 to 2, 3 to 4 ...
  *
  * @param inputSize   how many values to sort
  * @param outputSize  how many of the top or bottom sorted values to return
  * @param fixedType   Size of FixedPointer numbers in input
  * @param reverseSort returns lowest sorted values when false, highest sorted values when true
  */
class SortAndTake(val inputSize: Int, val outputSize: Int, fixedType: FixedPoint, reverseSort: Boolean = false)
  extends Module {
  val io = IO(new Bundle {
    val inputs    = Input(Vec(inputSize, fixedType))
    val newInputs = Input(Bool())
    val outputs   = Output(Vec(outputSize, fixedType))
    val sortDone  = Output(Bool())
  })

  val sortReg      = Reg(Vec(inputSize, FixedPoint(64.W,16.BP)))
  val busy         = RegInit(false.B)
  val sortCounter  = RegInit(0.U(log2Ceil(inputSize).W))
  val isEvenCycle  = RegInit(false.B)

val dataX      = Array(Array(12.1,3.5,14.0,9.0,56.3,4.1),Array(22.1,3.52,4.0,91.0,6.3,4.1),Array(12.1,3.25,4.0,9.0,26.3,4.1),Array(2.1,3.5,4.0,9.0,6.3,4.1),Array(23.1,31.5,4.10,9.10,6.3,4.1),Array     (25.1,3.5,4.0,19.0,6.13,4.21)).map(_.map(_.F(fixedType.getWidth.W, fixedType.binaryPoint)))
val tab        = Array(20.1,3.0,12.3,4.0,2.3,1.0).map(_.F(fixedType.getWidth.W, fixedType.binaryPoint))
val distances  = dataX.map{ row =>
    val keyRowPairs   = tab.zip(row)
    val rowProducts   = keyRowPairs.map { case (x, y) => (y - x) * (y - x) }
    val rowSum        = rowProducts.reduce(_ + _)
    rowSum}


 when(io.newInputs) {
    // when parent module loads new inputs to be sorted, we load registers and prepare to sort
    //sortReg.zip(io.inputs).foreach { case (reg, in) => reg := in }
    sortReg.zip(distances).foreach { case (reg, in) => reg := in }

    busy := true.B
    sortCounter := 0.U
    isEvenCycle := false.B
  }
    .elsewhen(busy) {
      isEvenCycle := ! isEvenCycle
 printf("counter is %d\n", sortCounter)

      sortCounter := sortCounter + 1.U
      when(sortCounter > inputSize.U) {
        busy := false.B
      }

      when(isEvenCycle) {
        sortReg.toList.sliding(2, 2).foreach {
          case regA :: regB :: Nil =>
            when(regA > regB) {
              // a is bigger than b, so flip this pair
              regA := regB
              regB := regA
            }
          case _ =>
          // this handles end case when there is nothing to compare register to
        }
      }
        .otherwise {
          sortReg.tail.toList.sliding(2, 2).foreach {
            case regA :: regB :: Nil =>
              when(regA > regB) {
                // a is bigger than b, so flip this pair
                regA := regB
                regB := regA
              }
            case _ =>
              // this handles end case when there is nothing to compare register to
          }
        }
    }

  io.sortDone := ! busy

  private val orderedRegs = if(reverseSort) sortReg.reverse else sortReg
  io.outputs.zip(orderedRegs).foreach { case (out, reg) =>
    out := reg
  }
}

class SortTester(c: SortAndTake) extends PeekPokeTester(c) {

 override def pokeFixedPoint(signal: FixedPoint, value: Double): Unit = {
    val bigInt = value.F(signal.binaryPoint).litValue()
    poke(signal, bigInt)
  }
 override def peekFixedPoint(signal: FixedPoint): Double = {
    val bigInt = peek(signal)
    signal.binaryPoint match {
      case KnownBinaryPoint(bp) => FixedPoint.toDouble(bigInt, bp)
      case _ => throw new Exception("Cannot peekFixedPoint with unknown binary point location")
    }
  }

  def showOutputs(): Unit = {
    for(i <- 0 until c.outputSize) {
   // for(i <- 0 until 15) {
      print(f"${peekFixedPoint(c.io.outputs(i))}%10.5f ")
    }
    println()
  }
 // for(i <- 0 until c.inputSize) {
  //for(i <- 0 until 15) {
 //   pokeFixedPoint(c.io.inputs(i), (c.inputSize - i).toDouble / 2.0)
    //pokeFixedPoint(c.tab(i), (15 - i).toDouble / 2.0)
 // }
  poke(c.io.newInputs, 1)
  step(1)

  poke(c.io.newInputs, 0)
  step(1)

  // wait for sort to finish

  while(peek(c.io.sortDone) == 0) {
    showOutputs()
    step(1)
  }

  showOutputs()

}

object SortTest {
  def main(args: Array[String]): Unit = {
    iotesters.Driver.execute(Array.empty[String], () => new SortAndTake(6, 6, FixedPoint(64.W, 32.BP))) { c =>
      new SortTester(c)
    }

    iotesters.Driver.execute(
      Array.empty[String],
      () => new SortAndTake(6, 7, FixedPoint(64.W, 32.BP), reverseSort = true)
    ) { c =>
      new SortTester(c)
    }
  }
}
