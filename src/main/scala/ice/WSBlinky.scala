package ice

import spinal.core._
import spinal.core.ClockDomain.ClockFrequency
import spinal.lib._
import pmods._
import blackbox._
import lib._

object WSBlinky {
  def main(args: Array[String]) = boards.IceBreaker.generate(new WSBlinky)
}

case class WSBlinky() extends Component {
  val io = new Bundle {
    val pmod1b_pin1 = out(Bool)
  }

  val pll = PLLPad(
    // 45.000 MHz
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

    val animation = new SlowArea(15 Hz) {
      val offset = CounterFreeRun(100)
    }

    new SlowArea(5 MHz) {
      val ledStrip = new WS2812bCtrl(300)
      ledStrip.io.colors.valid := True
      io.pmod1b_pin1 := !ledStrip.io.ledData

      val colorCounter = Counter(3)
      when(ledStrip.io.colors.ready) {
        colorCounter.increment()
      }

      val color = (colorCounter.value + animation.offset.value) % 3
      ledStrip.io.colors.r := Mux(color === 0, U"8'd32", U"8'd0")
      ledStrip.io.colors.g := Mux(color === 1, U"8'd32", U"8'd0")
      ledStrip.io.colors.b := Mux(color === 2, U"8'd32", U"8'd0")
    }
  }
}
