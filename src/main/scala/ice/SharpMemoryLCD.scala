package ice

import spinal.core._
import spinal.lib._
import pmods._

object SharpMemoryLCD {
  def main(args: Array[String]) = boards.IceBreaker.generate(new SharpMemoryLCD)
}

case class SharpMemoryLCD() extends Component {
  val io = new Bundle {
    val pmod1a = master(MemoryLCD())
    val pmod2 = master(SnapOff())
  }

  val display = new MemoryLCDCtrl
  io.pmod1a <> display.io.pins

  val snapOff = new SnapOffCtrl
  io.pmod2 <> snapOff.io.pins

  snapOff.io.led1 := snapOff.io.button1
  snapOff.io.led2 := snapOff.io.button2
  snapOff.io.led3 := snapOff.io.button3
  snapOff.io.led4 := snapOff.io.button1 & snapOff.io.button2
  snapOff.io.led5 := snapOff.io.button2 & snapOff.io.button3

  val offset = new SlowArea(40 Hz) {
    val counter = CounterFreeRun(32)
  }

  display.io.displayOn := ~snapOff.io.button3

  display.io.hit := offset.counter === display.io.pixels.payload.x.resized || offset.counter === display.io.pixels.payload.y.resized

  new SlowArea(1000 Hz) {
    display.io.buzzer := snapOff.io.button1 & CounterFreeRun(2).willOverflow
  }
}
