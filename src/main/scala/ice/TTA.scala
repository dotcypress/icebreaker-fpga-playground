package ice

import spinal.core._
import spinal.lib._
import pmods._
import tta._

object TTA {
  def main(args: Array[String]) = IceBreaker.generate(new TTA)
}

case class TTA() extends Component {
  val io = new Bundle {
    val pmod1a = pmod(SevenSegmentDisplay())
    val pmod2 = pmod(SnapOff())
  }

  val control = new SlowArea(400 Hz) {
    val snapOff = new SnapOffCtrl
    io.pmod2 <> snapOff.io.pins

    snapOff.io.led1 := snapOff.io.button1
    snapOff.io.led2 := snapOff.io.button2
    snapOff.io.led3 := False
    snapOff.io.led4 := False

    val counterEnable = ~snapOff.io.button1

    val display = new SevenSegmentDisplayCtrl
    display.io.enable := True
    io.pmod1a <> display.io.pins
  }

  var core = new ResetArea(control.snapOff.io.button3, false) {
    val core = new SlowArea(4 Hz) {
      val busWidth = (32 bits)

      val pc = CoreFU(busWidth, 4 bits, 256)
      pc.io.source.valid := False
      pc.io.source.payload := 0

      val imm = ImmFU(busWidth)
      imm.io.value := pc.io.imm

      val reg = MapFU(busWidth, (input) => input)
      val inc = MapFU(busWidth, _ + 1)
      val dec = MapFU(busWidth, _ - 1)
      val add = StackMapFU(busWidth, 2, (s) => s(0) + s(1))
      val sub = StackMapFU(busWidth, 2, (s) => s(0) - s(1))

      imm.io.sink >> add.io.source
      add.io.sink >> reg.io.source

      control.snapOff.io.led5 := reg.io.sink.valid
      control.display.io.value := reg.io.sink.payload.resized
    }
  }
}
