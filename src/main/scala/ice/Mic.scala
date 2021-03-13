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
    val pmod1a = master(DIPSwitch())
    val pmod2 = master(I2SMic())
    val uart = master(Uart())
  }

  val dipSwitch = new DIPSwitchCtrl(false)
  io.pmod1a <> dipSwitch.io.pins

  val channel = dipSwitch.io.output(0) ? AudioChannel.left | AudioChannel.right

  val i2s = new SlowArea(6 MHz) {
    val clock = CounterFreeRun(2).willOverflow
  }

  val mic = new I2SMicCtrl(
    ClockDomain(i2s.clock, clockDomain.readResetWire),
    8
  )
  mic.io.pins <> io.pmod2
  mic.io.channel := channel

  val sink = UartSink(2 MHz, 8 bits)
  sink.io.uart <> io.uart
  sink.io.data <> mic.io.pcm.toStream
}
