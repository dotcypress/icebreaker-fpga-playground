package ice

import spinal.core._
import spinal.lib._
import pmods._
import lib._

object ColorSensor {
  def main(args: Array[String]) = boards.IceBreaker.generate(new ColorSensor)
}

case class ColorSensor() extends Component {
  val io = new Bundle {
    val pmod1a = pmod(SevenSegmentDisplay())
    val pmod1b = pmod(SnapOff())
    val pmod2 = pmod(TCS3200())
  }

  val colorSensor = new TCS3200Ctrl
  colorSensor.io.pins <> io.pmod2

  val snapOff = new SnapOffCtrl
  io.pmod1b <> snapOff.io.pins

  snapOff.io.led1 := False
  snapOff.io.led2 := False
  snapOff.io.led3 := False
  snapOff.io.led4 := False
  snapOff.io.led5 := False

  val ui = new SlowArea(1 kHz) {
    val display = new SevenSegmentDisplayCtrl
    display.io.pins <> io.pmod1a
    display.io.enable := True
  }

  new SlowArea(4 Hz) {
    val filter = new FirFilter(8 bit, Seq(1, 1, 1, 1, 1, 1, 1, 1))
    filter.io.source.translateFrom(colorSensor.io.color)((to, from) => {
      when(snapOff.io.button1) {
        to := from.red
      } elsewhen (snapOff.io.button2) {
        to := from.green
      } elsewhen (snapOff.io.button3) {
        to := from.blue
      } otherwise {
        to := from.luma
      }
    })
    ui.display.io.value := filter.io.result
  }
}
