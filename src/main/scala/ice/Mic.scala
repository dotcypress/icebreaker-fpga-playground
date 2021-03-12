package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import lib.UartSink
import pmods._
import blackbox._

object Mic {
  def main(args: Array[String]) = boards.IceBreaker.generate(new Mic)
}

case class Mic() extends Component {
  val io = new Bundle {
    val pmod2 = master(PDMMic())
    val uart = master(Uart())
  }

  val config = PLLConfig(B"4'b0000", B"7'b1010101", B"3'b100", B"3'b001")
  val pll = PLLPad(config)

  pll.clockPin := ClockDomain.current.readClockWire
  pll.reset := ClockDomain.current.readResetWire

  val pllClockDomain = ClockDomain(
    pll.coreClockOut,
    ClockDomain.current.readResetWire,
    frequency = FixedFrequency(64.512 MHz)
  )

  new ClockingArea(pllClockDomain) {
    val audio = new SlowArea(4.608 MHz) {
      val clock = CounterFreeRun(2).willOverflow
    }

    val wordWidth = 32 bits

    val mic = new PDMMicCtrl(wordWidth)
    mic.io.pins <> io.pmod2
    mic.io.audioClock := audio.clock

    val sink = UartSink(2 MHz, wordWidth)
    sink.io.uart <> io.uart
    sink.io.data <> mic.io.pcm.toStream
  }
}