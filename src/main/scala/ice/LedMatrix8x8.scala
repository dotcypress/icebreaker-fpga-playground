package ice

import spinal.core._
import spinal.lib._
import pmods._

object LedMatrix8x8 {
  def main(args: Array[String]) = IceBreaker.generate(new LedMatrix8x8)
}

case class LedMatrix8x8() extends Component {
  val io = new Bundle {
    val pmod2 = pmod(LedMatrix())
  }

  val animation = new SlowArea(1000 Hz) {
    val counter = CounterFreeRun(64)

    val width = 8
    val height = 8

    val matrix = new LedMatrixCtrl(width, height)
    io.pmod2 <> matrix.io.pins

    when(matrix.io.pixel.valid) {
      // matrix.io.on := matrix.io.pixel.x === U(width / 2) || matrix.io.pixel.y === U(height / 2)
    }

  }
}
