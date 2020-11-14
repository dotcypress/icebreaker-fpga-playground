package ice.tta

import spinal.core._
import spinal.lib._

case class MapFU[T <: BitCount](
    width: T,
    op: (UInt) => UInt
) extends Component {
  val io = new FunctionalUnit(width)

  val dataType = UInt(width)
  val zero = dataType.getZero

  val result = Reg(dataType) init (zero)
  io.sink.payload := result

  var valid = Reg(Bool())
  io.sink.valid := valid

  when(io.source.valid) {
    valid := True
    result := op(io.source.payload)
  }
}
