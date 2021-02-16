package ice.blackbox

import spinal.core._
import spinal.lib._

case class PLLConfig(
    var DIVR: Bits,
    var DIVF: Bits,
    var DIVQ: Bits,
    var FILTER_RANGE: Bits
) {
  def applyTo(bb: PLLPad): Unit = {
    bb.addGeneric("DIVR", DIVR)
    bb.addGeneric("DIVF", DIVF)
    bb.addGeneric("DIVQ", DIVQ)
    bb.addGeneric("FILTER_RANGE", FILTER_RANGE)
  }
}

case class PLLPad(config: PLLConfig) extends BlackBox {
  setDefinitionName("SB_PLL40_PAD")
  val clockPin = in(Bool).setName("PACKAGEPIN")
  val coreClockOut = out(Bool).setName("PLLOUTCORE")
  val reset = in(Bool).setName("RESETB")
  config.applyTo(this)
}

case class GlobalBuffer() extends BlackBox {
  setDefinitionName("SB_GB")
  val input = in(Bool).setName("USER_SIGNAL_TO_GLOBAL_BUFFER")
  val output = out(Bool).setName("GLOBAL_BUFFER_OUTPUT")
}

case class RgbCtrlConfig(
    var halfCurrent: Boolean = false,
    var redCurrent: String = "0b000001",
    var blueCurrent: String = "0b000001",
    var greenCurrent: String = "0b000001"
) {
  def applyTo(bb: RgbCtrl): Unit = {
    bb.addGeneric("CURRENT_MODE", if (halfCurrent) "0b1" else "0b0")
    bb.addGeneric("RGB0_CURRENT", redCurrent)
    bb.addGeneric("RGB1_CURRENT", blueCurrent)
    bb.addGeneric("RGB2_CURRENT", greenCurrent)
  }
}

case class RgbCtrl(
    config: RgbCtrlConfig = RgbCtrlConfig()
) extends BlackBox {
  setDefinitionName("SB_RGBA_DRV")
  val current = in(Bool).setName("CURREN")
  val enable = in(Bool).setName("RGBLEDEN")

  val pwmRed = in(UInt(1 bit)).setName("RGB0PWM")
  val pwmGreen = in(UInt(1 bit)).setName("RGB1PWM")
  val pwmBlue = in(UInt(1 bit)).setName("RGB2PWM")

  val red = out(UInt(1 bit)).setName("RGB0")
  val green = out(UInt(1 bit)).setName("RGB1")
  val blue = out(UInt(1 bit)).setName("RGB2")
  config.applyTo(this)
}