package ice.tta

import spinal.core._
import spinal.lib._

class ImmediateUnit[T <: BitCount](width: T) extends FunctionalUnit(width) {
  val value = in(UInt(width))
}

case class ImmFU[T <: BitCount](width: T) extends Component {
  val io = new ImmediateUnit(width)

  io.sink.payload := io.value
  io.sink.valid := True
}
