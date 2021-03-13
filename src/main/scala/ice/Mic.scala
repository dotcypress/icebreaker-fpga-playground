package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import lib.UartSink
import pmods._
import spinal.lib.misc.Prescaler

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

  val pcmWidth = 8 bit
  val channel = dipSwitch.io.output(0) ? AudioChannel.left | AudioChannel.right

  val i2s = new SlowArea(6 MHz) {
    var bitClock = RegInit(False)
    bitClock := ~bitClock
  }

  val mic = new I2SMicCtrl(pcmWidth)
  mic.io.bitClock := i2s.bitClock
  mic.io.channel := channel
  mic.io.pins <> io.pmod2

  val sink = UartSink(2 MHz, pcmWidth)
  sink.io.uart <> io.uart
  sink.io.data <> mic.io.pcm.toStream
}
