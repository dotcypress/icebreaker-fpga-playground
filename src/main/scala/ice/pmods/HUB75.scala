package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class HUB75() extends PMODBundle {
  override def build() = {
    out(pin1, pin2, pin3, pin4, pin7, pin8, pin9, pin10)
  }
}

case class ICN2037Ctrl(width: Int = 64, height: Int = 64) extends Component {
  val io = new Bundle {
    val pinsA = pmod(HUB75())
    val pinsB = pmod(HUB75())

    val rgb0 = in(UInt(3 bits))
    val rgb1 = in(UInt(3 bits))

    val row = out(UInt(5 bits))
    val pixel = master(Stream(UInt(log2Up(width - 1) bits)))
  }

  val addr = Reg(UInt(5 bits)) init (0)
  io.row := addr

  val clock = Reg(Bool)
  val blank = Reg(Bool)
  val latch = Reg(Bool)

  io.pinsB.pin7 := blank
  io.pinsB.pin8 := latch
  io.pinsB.pin9 := clock

  io.pinsA.pin4 := False
  io.pinsA.pin10 := False

  io.pinsA.pin1 := io.rgb0(0)
  io.pinsA.pin2 := io.rgb0(1)
  io.pinsA.pin3 := io.rgb0(2)

  io.pinsA.pin7 := io.rgb1(0)
  io.pinsA.pin8 := io.rgb1(1)
  io.pinsA.pin9 := io.rgb1(2)

  io.pinsB.pin1 := addr(0)
  io.pinsB.pin2 := addr(1)
  io.pinsB.pin3 := addr(2)
  io.pinsB.pin4 := addr(3)
  io.pinsB.pin10 := addr(4)

  val fsm = new StateMachine {
    val shiftData = new State with EntryPoint
    val latching = new State
    val nextRow = new State

    val pixelCounter = Counter(width * 2)

    io.pixel.valid := True
    io.pixel.payload := (pixelCounter.value / 2).resized

    shiftData
      .onEntry {
        pixelCounter.clear()
      }
      .whenIsActive {
        clock := ~clock
        pixelCounter.increment()
        when(pixelCounter.willOverflowIfInc) {
          goto(latching)
        }
      }
      .onExit {
        blank := True
      }

    latching.whenIsActive {
      latch := True
      goto(nextRow)
    }

    nextRow.whenIsActive {
      addr := addr + 1
      latch := False
      blank := False
      goto(shiftData)
    }
  }
}
