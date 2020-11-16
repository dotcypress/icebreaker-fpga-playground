package ice.tta

import spinal.core._
import spinal.lib._

class CoreFunctionalUnit[T <: BitCount](width: T)
    extends FunctionalUnit(width) {
  val imm = out(UInt(width))
  val xbar = out(Wire(width))
}

case class CoreFU[T <: BitCount](width: T, insWidth: T, memWidth: Int)
    extends Component {
  val io = new CoreFunctionalUnit(width)

  val mem = Mem(UInt(width), wordCount = memWidth)
  val ins = Reg(UInt(insWidth)) init (0)
  val pc = Reg(UInt(width)) init (0)

  io.imm := ins(0).mux {
    0 -> U(0)
    1 -> ins((insWidth.value / 2) downto 1)
  }

  io.xbar.from := ins(0).mux {
    0 -> ins((insWidth.value / 2) downto 1)
    1 -> U(0)
  }

  io.sink.payload := pc
  io.sink.valid := True

  def setPC(addr: UInt) = {
    pc := addr
    ins := mem(addr)
  }

  def inc() = {
    setPC(pc + insWidth.value)
  }

  when(io.source.valid) {
    setPC(io.source.payload)
  }
}
