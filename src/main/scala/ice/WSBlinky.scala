package ice

import spinal.core._
import spinal.lib._
import pmods._
import spinal.core.ClockDomain.ClockFrequency

object WSBlinky {
  def main(args: Array[String]) = IceBreaker.generate(new WSBlinky)
}

case class WSBlinky() extends Component {
  val io = new Bundle {
    val pmod1a = pmod(SevenSegmentDisplay())
    val pmod1b_pin1 = out(Bool)
  }

  // 45.000 MHz
  val pll = SB_PLL40_PAD(
    PLLConfig(B"4'b0000", B"7'b0111011", B"3'b101", B"3'b100")
  )

  pll.clockPin := ClockDomain.current.readClockWire
  pll.reset := ClockDomain.current.readResetWire

  new ClockingArea(
    ClockDomain(
      pll.coreClockOut,
      ClockDomain.current.readResetWire,
      frequency = FixedFrequency(45 MHz)
    )
  ) {
    val animation = new SlowArea(10 Hz) {
      val counter = CounterFreeRun(3)
    }

    val display = new SevenSegmentDisplayCtrl
    io.pmod1a <> display.io.pins
    display.io.enable := True
    display.io.value := animation.counter.resized

    val ledStrip = new WS2812bCtrl(24)
    io.pmod1b_pin1 := !ledStrip.io.ledData

    ledStrip.io.colors.valid := True
    ledStrip.io.colors.r := Mux(animation.counter === 0, U"8'd32", U"8'd0")
    ledStrip.io.colors.g := Mux(animation.counter === 1, U"8'd32", U"8'd0")
    ledStrip.io.colors.b := Mux(animation.counter === 2, U"8'd32", U"8'd0")
  }
}
