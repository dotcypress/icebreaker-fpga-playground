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

  val counter = Counter(8)
  val clock = Reg(Bool) init (False)
  val latch = Reg(Bool) init (False)

  val animation = new SlowArea(8 Hz) {
    val offset = CounterFreeRun(8)
  }

  val pins = io.pmod2
  pins.pin1 := B(clockDomain.newSlowedClockDomain(2 MHz).readClockWire)
  pins.pin2 := latch.asBits
  pins.pin3 := (counter === animation.offset).asBits
  pins.pin4 := 0
  pins.pin10 := clock.asBits

  new StateMachine {
    val shiftData: State = new State with EntryPoint {
      whenIsActive {
        clock := ~clock
        when(clock.fall) {
          counter.increment()
          when(counter.willOverflow) {
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
