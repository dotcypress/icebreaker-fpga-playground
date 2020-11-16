package ice.tta

import spinal.core._
import spinal.lib._

case class Wire[T <: BitCount](width: T) extends Bundle {
  val from = UInt(width)
  val to = UInt(width)
}

case class XBar[T <: BitCount](width: T, layout: Int, memWidth: Int)
    extends Component {
  val io = new CoreFunctionalUnit(width)

  val mem = Mem(UInt(width), wordCount = memWidth)
  val pc = Reg(UInt(width)) init (0)
  val ins = Reg(UInt(width)) init (0)

  io.imm := ins(0).mux {
    0 -> U(0)
    1 -> ins(4 downto 0).resized
  }

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
