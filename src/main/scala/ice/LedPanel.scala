package ice

import spinal.core._
import spinal.lib._
import pmods._

object LedPanel {
  def main(args: Array[String]) = boards.IceBreaker.generate(new LedPanel)
}

case class LedPanel() extends Component {
  val io = new Bundle {
    val pmod1a = master(HUB75())
    val pmod1b = master(HUB75())
  }

  val ledPanel = new ICN2037Ctrl
  io.pmod1a <> ledPanel.io.pinsA
  io.pmod1b <> ledPanel.io.pinsB

  val core = new SlowArea(1000 Hz) {
    val x = Reg(SInt(7 bit)) init (10)
    val y = Reg(SInt(7 bit)) init (32)
    val xVelocity = Reg(SInt(3 bits)) init (2)
    val yVelocity = Reg(SInt(3 bits)) init (1)

    when(x <= 2) {
      xVelocity := 1
    }.elsewhen(x >= 60) {
      xVelocity := -2
    }

    when(y <= 2) {
      yVelocity := 2
    }.elsewhen(y >= 58) {
      yVelocity := -1
    }

    x := x + xVelocity
    y := y + yVelocity
  }

  val pixel = ledPanel.io.pixel.payload

  ledPanel.io.colors(0).r := U(
    pixel.x === 0 | pixel.x === 63 | pixel.y <= 1
  )
  ledPanel.io.colors(1).r := U(
    pixel.x === 0 | pixel.x === 63 | pixel.y >= 30
  )

  ledPanel.io.colors(0).g := U(0)
  ledPanel.io.colors(1).g := U(0)

  ledPanel.io.colors(0).b := U(
    core.x.abs === pixel.x & core.y.abs === pixel.y
  )
  ledPanel.io.colors(1).b := U(
    core.x.abs === pixel.x & (core.y.abs - 32) === pixel.y
  )
}
