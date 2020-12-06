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

    val display = new SevenSegmentDisplayCtrl
    io.pmod1a <> display.io.pins
    display.io.enable := True
    display.io.value := 0

    val animation = new SlowArea(10 Hz) {
      val offset = CounterFreeRun(100)
    }

    new SlowArea(5 MHz) {
      val ledStrip = new WS2812bCtrl(30)
      io.pmod1b_pin1 := !ledStrip.io.ledData

      val colorCounter = Counter(3)
      when(ledStrip.io.colors.ready) {
        colorCounter.increment()
      }

      val color = (colorCounter.value + animation.offset.value) % 3
      ledStrip.io.colors.r := Mux(color === 0, U"8'd32", U"8'd0")
      ledStrip.io.colors.g := Mux(color === 1, U"8'd32", U"8'd0")
      ledStrip.io.colors.b := Mux(color === 2, U"8'd32", U"8'd0")
      ledStrip.io.colors.valid := True
    }
  }
}
