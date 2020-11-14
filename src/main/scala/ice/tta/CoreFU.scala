package ice.tta

import spinal.core._
import spinal.lib._

class CoreFunctionalUnit[T <: BitCount](width: T)
    extends FunctionalUnit(width) {
  val imm = out(UInt(width))
  val ins = out(UInt(width))
}

case class CoreFU[T <: BitCount](width: T, memWidth: Int) extends Component {
  val io = new CoreFunctionalUnit(width)

  val pc = Reg(UInt(width)) init (0)
  val ins = Reg(UInt(width)) init (0)
  val mem = Mem(UInt(width), wordCount = memWidth)

  io.ins := ins
  io.imm := ins(4 downto 0).resized

  io.sink.payload := pc
  io.sink.valid := True

  def setPC(addr: UInt) = {
    pc := addr
    ins := mem(addr.resized).resized
  }

  def inc() = {
    setPC(pc + 1)
  }

  when(io.source.valid) {
    setPC(io.source.payload)
  }
}
