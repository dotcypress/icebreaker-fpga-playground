package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import lib.UartSink
import pmods._

object Mic {
  def main(args: Array[String]) = boards.IceBreaker.generate(new Mic)
}

case class Mic() extends Component {
  val io = new Bundle {
    val pmod2 = master(I2SMicrophone())
    val uart = master(Uart())
  }

  val sink = UartSink(2 MHz)
  sink.io.uart <> io.uart

  val i2s = new SlowArea(1 MHz) {
    val clock = CounterFreeRun(2).willOverflow
  }

  val mic = new I2SMicrophoneCtrl(ClockDomain(i2s.clock, clockDomain.readResetWire))
  mic.io.pins <> io.pmod2
  mic.io.channel := AudioChannel.left

  sink.io.data.valid := mic.io.pcm.valid
  sink.io.data.payload := mic.io.pcm.payload(23 downto 16).asBits
}
