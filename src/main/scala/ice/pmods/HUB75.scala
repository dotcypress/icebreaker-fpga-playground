package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import spinal.lib.graphic.{Rgb, RgbConfig}
import spinal.lib.misc.BinTools

case class HUB75() extends PMODBundle {
  override def asMaster() = this.asOutput()
}

case class ICN2037Ctrl(
    width: Int = 64,
    height: Int = 64,
    rgbConfig: RgbConfig = RgbConfig(1, 1, 1)
) extends Component {
  val io = new Bundle {
    val pinsA = master(HUB75())
    val pinsB = master(HUB75())
  }

  val topColor = Reg(Rgb(rgbConfig))
  val bottomColor = Reg(Rgb(rgbConfig))

  val column = Reg(UInt(6 bits)) init (0)
  val row = Reg(UInt(5 bits)) init (0)
  val blank = Reg(Bool) init (False)
  val latch = Reg(Bool) init (False)
  val clock = Reg(Bool) init (False)

  io.pinsA.pin4 := B(0)
  io.pinsA.pin10 := B(0)

  io.pinsA.pin1 := topColor.r.asBits
  io.pinsA.pin2 := topColor.g.asBits
  io.pinsA.pin3 := topColor.b.asBits

  io.pinsA.pin7 := bottomColor.r.asBits
  io.pinsA.pin8 := bottomColor.g.asBits
  io.pinsA.pin9 := bottomColor.b.asBits

  io.pinsB.pin1 := row(0).asBits
  io.pinsB.pin2 := row(1).asBits
  io.pinsB.pin3 := row(2).asBits
  io.pinsB.pin4 := row(3).asBits
  io.pinsB.pin10 := row(4).asBits

  io.pinsB.pin7 := blank.asBits
  io.pinsB.pin8 := latch.asBits
  io.pinsB.pin9 := clock.asBits

  val memTop = new Mem(Rgb(RgbConfig(8, 8, 8)), 64 * 32)
  val memBottom = new Mem(Rgb(RgbConfig(8, 8, 8)), 64 * 64)

  BinTools.initRam(memTop, "src/main/resources/spongebob.rgb")
  BinTools.initRam(memBottom, "src/main/resources/spongebob.rgb")

  new StateMachine {
    val shiftData = new State with EntryPoint
    val blankData = new State
    val unblankData = new State

    val pwm = CounterFreeRun(256)

    shiftData
      .whenIsActive {
        val offset = column + (row + 1) * width
        val colorTop = memTop.readSync(offset.resized)
        val colorBottom = memBottom.readSync(offset + 32 * width)

        topColor.r := U(colorTop.r.resized > pwm)
        topColor.g := U(colorTop.g.resized > pwm)
        topColor.b := U(colorTop.b.resized > pwm)
        bottomColor.r := U(colorBottom.r.resized > pwm)
        bottomColor.g := U(colorBottom.g.resized > pwm)
        bottomColor.b := U(colorBottom.b.resized > pwm)

        clock := ~clock
        when(clock.fall()) {
          column := column + 1
          when(column === width - 1) {
            latch := True
            column := 0
            goto(blankData)
          }
        }
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
