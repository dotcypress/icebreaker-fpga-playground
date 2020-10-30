package ice

import spinal.core._
import spinal.lib._
import pmods._

object LedPanel {
  def main(args: Array[String]) = IceBreaker.generate(new LedPanel)
}

case class LedPanel() extends Component {
  val io = new Bundle {
    val pmod1a = pmod(HUB75())
    val pmod1b = pmod(HUB75())
  }

  val ledPanel = new ICN2037Ctrl
  io.pmod1a <> ledPanel.io.pinsA
  io.pmod1b <> ledPanel.io.pinsB

  val animation = new SlowArea(100 Hz) {
    val counter = CounterFreeRun(64)
  }

  ledPanel.io.rgb0 := 0
  ledPanel.io.rgb0(0) := animation.counter.value === ledPanel.io.pixel
  ledPanel.io.rgb0(1) := (64 - animation.counter.value) === ledPanel.io.pixel

  ledPanel.io.rgb1 := 0
  ledPanel.io.rgb1(0) := (64 - animation.counter.value) === ledPanel.io.pixel
  ledPanel.io.rgb1(1) := animation.counter.value === ledPanel.io.pixel
}
