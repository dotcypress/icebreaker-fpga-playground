package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import lib.UartSink
import pmods._
import blackbox._

object Sink {
  def main(args: Array[String]) = boards.IceBreaker.generate(new Sink)
}

case class Sink() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
  }

  val packetWidth = 16 bits

  val counter = Counter(packetWidth)
  val timeout = Timeout(32 Hz)

  when(timeout.state) {
    counter.increment()
    timeout.clear()
  }

  val sink = UartSink(dataWidth = packetWidth)
  sink.io.uart <> io.uart
  sink.io.data.valid := timeout.stateRise
  sink.io.data.payload := counter.value.asBits
}
