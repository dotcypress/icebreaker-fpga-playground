package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class HUB75() extends PMODBundle {
  override def asMaster() = this.asOutput()
}

case class ICN2037Ctrl(width: Int = 64, height: Int = 64) extends Component {
  val io = new Bundle {
    val pinsA = master(HUB75())
    val pinsB = master(HUB75())

    val rgb0 = in(UInt(3 bits))
    val rgb1 = in(UInt(3 bits))

    val row = out(UInt(5 bits))
    val pixel = master(Flow(UInt(log2Up(width - 1) bits)))
  }

  val addr = Reg(UInt(5 bits)) init (0)
  io.row := addr

  val clock = Reg(Bool)
  val blank = Reg(Bool)
  val latch = Reg(Bool)

  io.pinsB.pin7 := blank.asBits
  io.pinsB.pin8 := latch.asBits
  io.pinsB.pin9 := clock.asBits

  io.pinsA.pin4 := B(0)
  io.pinsA.pin10 := B(0)

  io.pinsA.pin1 := io.rgb0(0).asBits
  io.pinsA.pin2 := io.rgb0(1).asBits
  io.pinsA.pin3 := io.rgb0(2).asBits

  io.pinsA.pin7 := io.rgb1(0).asBits
  io.pinsA.pin8 := io.rgb1(1).asBits
  io.pinsA.pin9 := io.rgb1(2).asBits

  io.pinsB.pin1 := addr(0).asBits
  io.pinsB.pin2 := addr(1).asBits
  io.pinsB.pin3 := addr(2).asBits
  io.pinsB.pin4 := addr(3).asBits
  io.pinsB.pin10 := addr(4).asBits

  val fsm = new StateMachine {
    val shiftData = new State with EntryPoint
    val latchData = new State
    val nextRow = new State

    val pixelCounter = Counter(width * 2)

    io.pixel.valid := False
    io.pixel.payload := (pixelCounter.value / 2).resized

    shiftData
      .onEntry {
        pixelCounter.clear()
      }
      .whenIsActive {
        io.pixel.valid := True
        clock := ~clock
        pixelCounter.increment()
        when(pixelCounter.willOverflowIfInc) {
          goto(latchData)
        }
      }
      .onExit {
        blank := True
      }

    latchData.whenIsActive {
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
