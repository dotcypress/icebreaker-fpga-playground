package ice

import spinal.core._
import spinal.lib._
import pmods._

object HexCounter {
  def main(args: Array[String]) = boards.IceBreaker.generate(new HexCounter)
}

case class HexCounter() extends Component {
  val io = new Bundle {
    val pmod1a = master(SevenSegmentDisplay())
    val pmod1b = master(DIPSwitch())
    val pmod2 = master(SnapOff())
  }

  val coreArea = new SlowArea(400 Hz) {
    val display = new SevenSegmentDisplayCtrl
    io.pmod1a <> display.io.pins

    val dipSwitch = new DIPSwitchCtrl
    io.pmod1b <> dipSwitch.io.pins

    val snapOff = new SnapOffCtrl
    io.pmod2 <> snapOff.io.pins

    snapOff.io.led1 := snapOff.io.button1
    snapOff.io.led2 := snapOff.io.button2
    snapOff.io.led3 := snapOff.io.button3
    snapOff.io.led4 := snapOff.io.button1 & snapOff.io.button2
    snapOff.io.led5 := snapOff.io.button2 & snapOff.io.button3

    val counterEnable = dipSwitch.io.output(6)
    val displayEnable = dipSwitch.io.output(7)

    display.io.enable := snapOff.io.button1 | displayEnable
  }

  val ticker = new SlowArea(1 Hz) {
    val counter = Counter(256)
    coreArea.display.io.value := counter

    when(coreArea.counterEnable) {
      counter.increment()
    }
  }
}
