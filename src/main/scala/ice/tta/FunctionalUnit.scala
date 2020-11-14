package ice.tta

import spinal.core._
import spinal.lib._

class FunctionalUnit[T <: BitCount](width: T) extends Bundle {
  val source = slave(Flow(UInt(width)))
  val sink = master(Flow(UInt(width)))
}
