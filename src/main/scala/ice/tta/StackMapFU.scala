package ice.tta

import spinal.core._
import spinal.lib._

case class StackMapFU[T <: BitCount](
    width: T,
    stackSize: Int,
    op: (Vector[UInt]) => UInt
) extends Component {
  require(stackSize > 1)

  val io = new FunctionalUnit(width)

  val dataType = UInt(width)
  val zero = dataType.getZero

  val result = Reg(dataType) init (zero)
  io.sink.payload := result

  val ptr = Reg(UInt(log2Up(stackSize) bits)) init (stackSize - 1)
  val stack = Reg(Vec(dataType, stackSize))
  stack.foreach(_ init (zero))

  var valid = Reg(Bool())
  io.sink.valid := valid

  when(io.source.valid) {
    when(ptr === 0) {
      valid := True
      result := op(io.source.payload +: stack.vec)
      ptr := stackSize - 1
    } otherwise {
      stack(ptr) := io.source.payload
      ptr := ptr - 1
    }
  }
}
