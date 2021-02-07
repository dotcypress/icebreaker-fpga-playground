package ice

import spinal.core._
import spinal.lib._
import pmods._
import blackbox._

object LedPanel {
  def main(args: Array[String]) = boards.IceBreaker.generate(new LedPanel)
}

case class LedPanel() extends Component {
  val io = new Bundle {
    val pmod1a = master(HUB75())
    val pmod1b = master(HUB75())
  }

  // 60 MHz
  val config = PLLConfig(B"4'b0000", B"7'b0110100", B"3'b100", B"3'b001")
  val pll = PLLPad(config)

  pll.clockPin := ClockDomain.current.readClockWire
  pll.reset := ClockDomain.current.readResetWire

  val display = new ClockingArea(
    ClockDomain(pll.coreClockOut, ClockDomain.current.readResetWire)
  ) {

    val ledPanel = new ICN2037Ctrl
    io.pmod1a <> ledPanel.io.pinsA
    io.pmod1b <> ledPanel.io.pinsB
  }
}
