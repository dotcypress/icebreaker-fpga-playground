package ice.lib

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._

case class UartSink(
    baudrate: HertzNumber = 115.2 kHz,
    dataWidth: BitCount = 8 bits,
    fifoDepth: Int = 32,
    endianness: Endianness = LITTLE,
    padding: Boolean = false
) extends Component {
  val io = new Bundle {
    val data = slave(Stream(Bits(dataWidth)))
    val uart = master(Uart())
  }

  val uartCtrl = new UartCtrl()
  uartCtrl.io.uart <> io.uart
  uartCtrl.io.config.setClockDivider(baudrate)
  uartCtrl.io.config.frame.dataLength := 7
  uartCtrl.io.config.frame.parity := UartParityType.NONE
  uartCtrl.io.config.frame.stop := UartStopType.ONE
  uartCtrl.io.writeBreak := False

  var fifo = new StreamFifo(Bits(dataWidth), fifoDepth)
  fifo.io.push <> io.data

  if (dataWidth.value == 8) {
    fifo.io.pop <> uartCtrl.io.write
  } else {
    StreamWidthAdapter(
      input = fifo.io.pop,
      output = uartCtrl.io.write,
      endianness = endianness,
      padding = padding
    )
  }
}
