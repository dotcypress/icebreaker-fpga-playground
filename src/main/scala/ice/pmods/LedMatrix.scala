package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class Pixel(maxWidth: Int, maxHeight: Int) extends Bundle {
  val x = UInt(log2Up(maxWidth) bits)
  val y = UInt(log2Up(maxHeight) bits)
}

case class LedMatrix() extends PMODBundle {
  override def build() = {
    out(pin8, pin9, pin10)
    in(pin1, pin2, pin3, pin4, pin7)
  }
}

case class LedMatrixCtrl(width: Int, height: Int) extends Component {
  val io = new Bundle {
    val pins = pmod(LedMatrix())
    val pixel = master(Flow(Pixel(width, height)))
  }

  val active = Reg(Bool)
  val clock = Reg(Bool)
  val latch = Reg(Bool)

  io.pins.pin10 := active
  io.pins.pin9 := clock
  io.pins.pin8 := latch

  val fsm = new StateMachine {
    val shiftData = new State with EntryPoint
    val latchData = new State
    val nextFrame = new State

    val x = Counter(width)
    val y = Counter(height)

    active := True
    io.pixel.payload.x := x
    io.pixel.payload.y := y
    io.pixel.valid := False

    shiftData
      .onEntry {
        x.clear()
        y.clear()
      }
      .whenIsActive {
        io.pixel.valid := True
        clock := ~clock
        x.increment()
        when(x.willOverflow) {
          y.increment()
        }
        when(y.willOverflowIfInc) {
          goto(latchData)
        }
      }

    latchData.whenIsActive {
      latch := True
      goto(nextFrame)
    }

    nextFrame.whenIsActive {
      latch := False
      goto(shiftData)
    }
  }
}
