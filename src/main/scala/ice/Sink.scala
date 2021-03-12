package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import lib.UartSink
import pmods._

object Sink {
  def main(args: Array[String]) = boards.IceBreaker.generate(new Sink)
}

case class Sink() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
  }

  val packetWidth = 16 bits
  val counter = Counter(packetWidth)

  val sink = UartSink(
    dataWidth = packetWidth,
    endianness = BIG
  )
  sink.io.uart <> io.uart
  sink.io.data <> counter.toFlow.toStream.transmuteWith(Bits)

  val timeout = Timeout(32 Hz)
  when(timeout) {
    counter.increment()
    timeout.clear()
  }
}
