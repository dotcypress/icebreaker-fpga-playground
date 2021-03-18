package ice

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

import pmods._

object STP08cp05 {
  def main(args: Array[String]) = boards.IceBreaker.generate(new STP08cp05)
}

case class ShiftRegisterPMOD() extends PMODBundle {
  override def asMaster() = {
    in(pin7, pin8, pin9)
    out(pin1, pin2, pin3, pin4, pin10)
  }
}

case class STP08cp05() extends Component {
  val io = new Bundle {
    val pmod2 = master(ShiftRegisterPMOD())
  }

  val ledData = Vec(UInt(4 bits), 8)

  val shiftReg = new STP08cp05Ctrl()
  shiftReg.io.pmod2 <> io.pmod2
  shiftReg.io.data := ledData

  val animation = new SlowArea(32 Hz) {
    ledData := Vec.tabulate(ledData.length)(renderLed)

    def renderLed(idx: Int): UInt = {
      val inc = RegInit(True)
      val offset = CounterUpDown(16, inc, ~inc)

      when(inc && offset.willOverflowIfInc) {
        inc := False
      }

      when(~inc && offset === 1) {
        inc := True
      }

      (idx + offset.value).resized
    }
  }
}

case class STP08cp05Ctrl() extends Component {
  val io = new Bundle {
    val pmod2 = master(ShiftRegisterPMOD())
    val data = in(Vec(UInt(4 bits), 8))
  }

  val gamma = Vec[UInt](0, 0, 0, 1, 1, 2, 4, 6, 10, 14, 19, 25, 32, 41, 51, 63)
  val ledCounter = Counter(8)
  val pwmCounter = Counter(6 bits)
  val clock = Reg(Bool) init (False)
  val latch = Reg(Bool) init (False)
  val data = gamma(io.data(ledCounter)) > pwmCounter

  val pins = io.pmod2
  pins.pin1 := B(clockDomain.newSlowedClockDomain(2 MHz).readClockWire)
  pins.pin2 := latch.asBits
  pins.pin3 := B(data)
  pins.pin4 := 0
  pins.pin10 := clock.asBits

  new StateMachine {
    val shiftData: State = new State with EntryPoint {
      whenIsActive {
        clock := ~clock
        when(clock.fall) {
          ledCounter.increment()
          when(ledCounter.willOverflow) {
            pwmCounter.increment()
            latch := True
            goto(latchData)
          }
        }
      }
    }
    val latchData: State = new State {
      whenIsActive {
        latch := False
        goto(shiftData)
      }
    }
  }
}
