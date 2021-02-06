package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import spinal.lib.graphic.RgbConfig
import spinal.lib.graphic.Rgb

case class HUB75() extends PMODBundle {
  override def asMaster() = this.asOutput()
}

case class ICN2037Ctrl(
    width: Int = 64,
    height: Int = 64,
    rgbConfig: RgbConfig = RgbConfig(1, 1, 1)
) extends Component {
  val io = new Bundle {
    val colors = in(Vec(Rgb(rgbConfig), Rgb(rgbConfig)))

    val pinsA = master(HUB75())
    val pinsB = master(HUB75())
    val pixel = master(Flow(Pixel(width, height / 2)))
  }

  val blank = Reg(Bool) init (False)
  val latch = Reg(Bool) init (False)
  val clock = Reg(Bool) init (False)
  val row = Reg(UInt(5 bits)) init (0)

  io.pinsA.pin4 := B(0)
  io.pinsA.pin10 := B(0)

  io.pinsA.pin1 := io.colors(0).r.asBits
  io.pinsA.pin2 := io.colors(0).g.asBits
  io.pinsA.pin3 := io.colors(0).b.asBits

  io.pinsA.pin7 := io.colors(1).r.asBits
  io.pinsA.pin8 := io.colors(1).g.asBits
  io.pinsA.pin9 := io.colors(1).b.asBits

  io.pinsB.pin1 := row(0).asBits
  io.pinsB.pin2 := row(1).asBits
  io.pinsB.pin3 := row(2).asBits
  io.pinsB.pin4 := row(3).asBits
  io.pinsB.pin10 := row(4).asBits

  io.pinsB.pin7 := blank.asBits
  io.pinsB.pin8 := latch.asBits
  io.pinsB.pin9 := clock.asBits

  new StateMachine {
    val shiftData = new State with EntryPoint
    val latchData = new State
    val blankData = new State
    val unblankData = new State

    val pixelCounter = Counter(width * 2)

    io.pixel.payload.x := (pixelCounter.value / 2).resized
    io.pixel.payload.y := row + 1
    io.pixel.valid := False

    shiftData
      .whenIsActive {
        clock := ~clock
        pixelCounter.increment()
        io.pixel.valid := True
        when(pixelCounter.willOverflowIfInc) {
          pixelCounter.clear()
          goto(latchData)
        }
      }

    latchData.whenIsActive {
      latch := True
      goto(blankData)
    }

    blankData.whenIsActive {
      latch := False
      blank := True
      goto(unblankData)
    }

    unblankData
      .whenIsActive {
        blank := False
        row := row + 1
        goto(shiftData)
      }
  }
}
