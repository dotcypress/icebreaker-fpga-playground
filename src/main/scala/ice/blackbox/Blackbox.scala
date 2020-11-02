package ice

import spinal.core._
import spinal.lib._

case class PLLConfig(
    var DIVR: Bits,
    var DIVF: Bits,
    var DIVQ: Bits,
    var FILTER_RANGE: Bits
) {
  def applyTo(bb: BlackBox): Unit = {
    bb.addGeneric("DIVR", DIVR)
    bb.addGeneric("DIVF", DIVF)
    bb.addGeneric("DIVQ", DIVQ)
    bb.addGeneric("FILTER_RANGE", FILTER_RANGE)
  }
}

case class SB_PLL40_PAD(p: PLLConfig) extends BlackBox {
  val clockPin = in(Bool).setName("PACKAGEPIN")
  val coreClockOut = out(Bool).setName("PLLOUTCORE")
  val reset = in(Bool).setName("RESETB")

  p.applyTo(this)
}