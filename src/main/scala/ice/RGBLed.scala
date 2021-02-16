package ice

import spinal.core._
import spinal.lib._
import spinal.lib.graphic._
import blackbox._
import lib._

object RGBLed {
  def main(args: Array[String]) = boards.IceBreaker.generate(new RGBLed)
}

case class RGBLed() extends Component {
  val colorConfig = RgbConfig(1, 1, 1)
  val io = new Bundle {
    val ledRGB = out(Rgb(colorConfig))
  }

  val driver = RgbCtrl()
  driver.red <> io.ledRGB.r
  driver.green <> io.ledRGB.g
  driver.blue <> io.ledRGB.b

  driver.enable := True
  driver.current := True

  new SlowArea(4 Hz) {
    val color = Reg(Bits(3 bits)) init (1)
    color := color.rotateLeft(1)

    driver.pwmRed := U(color(0))
    driver.pwmGreen := U(color(1))
    driver.pwmBlue := U(color(2))
  }
}
