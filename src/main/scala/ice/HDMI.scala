package ice

import spinal.core._
import spinal.lib._
import blackbox._
import pmods._

object HDMI {
  def main(args: Array[String]) = boards.IceBreaker.generate(new HDMI)
}

case class HDMI() extends Component {
  val io = new Bundle {
    val pmod1a = pmod(DVI())
    val pmod1b = pmod(DVI())
  }

  // 25.125 MHz
  val config = PLLConfig(B"4'b0000", B"7'b1000010", B"3'b101", B"3'b001")
  val pll = PLLPad(config)

  pll.clockPin := ClockDomain.current.readClockWire
  pll.reset := ClockDomain.current.readResetWire

  val display = new ClockingArea(
    ClockDomain(pll.coreClockOut, ClockDomain.current.readResetWire)
  ) {
    val dvi = new DVICtrl

    io.pmod1a <> dvi.io.pinsA
    io.pmod1b <> dvi.io.pinsB

    val counter = Reg(UInt(4 bits))

    dvi.io.pixels.valid := True
    dvi.io.pixels.r := counter
    dvi.io.pixels.g := 0
    dvi.io.pixels.b := 0

    counter := counter + 1
    when(dvi.io.frameStart) {
      counter := 0
    }
  }
}
