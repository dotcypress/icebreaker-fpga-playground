package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import pmods._

object Echo {
  def main(args: Array[String]) = IceBreaker.generate(new Echo)
}

case class Echo() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
  }

  val uartCtrl = new UartCtrl()
  uartCtrl.io.config.setClockDivider(19.2 kHz)
  uartCtrl.io.config.frame.dataLength := 7
  uartCtrl.io.config.frame.parity := UartParityType.NONE
  uartCtrl.io.config.frame.stop := UartStopType.ONE
  uartCtrl.io.writeBreak := False
  uartCtrl.io.uart <> io.uart

  val rx = uartCtrl.io.read.toFlow.toReg()
  val tx = Stream(Bits(8 bits))
  tx.payload := rx
  tx.valid := CounterFreeRun(
    clockDomain.frequency.getValue.toInt
  ).willOverflow
  tx >-> uartCtrl.io.write
}
