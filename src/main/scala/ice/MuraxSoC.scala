package ice

import spinal.core._
import spinal.lib._
import spinal.lib.com.uart._
import vexriscv.demo._
import pmods._

object MuraxSoC {
  def main(args: Array[String]) = boards.IceBreaker.generate(new MuraxSoC)
}

case class MuraxSoC() extends Component {
  val io = new Bundle {
    val uart = master(Uart())
    val pmod1a = master(SevenSegmentDisplay())
    val pmod2 = master(SnapOff())
  }

  val snapOff = new SnapOffCtrl
  snapOff.io.pins <> io.pmod2

  val murax = Murax(
    MuraxConfig.default.copy(
      onChipRamHexFile = "src/main/resources/murax-app.hex"
    )
  )

  murax.io.jtag.tck := False
  murax.io.jtag.tms := False
  murax.io.jtag.tdi := False

  murax.io.mainClk := ClockDomain.current.readClockWire
  murax.systemClockDomain.setSyncWith(ClockDomain.current)

  murax.io.uart <> io.uart
  murax.io.asyncReset := ~snapOff.io.button3

  murax.io.gpioA.read := 0

  val gpio = murax.io.gpioA.write

  new SlowArea(400 Hz) {
    snapOff.io.led1 := gpio(0)
    snapOff.io.led2 := gpio(1)
    snapOff.io.led3 := gpio(2)
    snapOff.io.led4 := gpio(3)
    snapOff.io.led5 := gpio(4)

    val display = new SevenSegmentDisplayCtrl
    display.io.pins <> io.pmod1a
    display.io.value := gpio(10 downto 3).asUInt
    display.io.enable := True
  }
}
