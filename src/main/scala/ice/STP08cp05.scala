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
    in(pin4, pin7, pin9, pin10)
    out(pin1, pin2, pin3, pin8)
  }
}

case class STP08cp05() extends Component {
  val io = new Bundle {
    val pmod1b = master(ShiftRegisterPMOD())
  }

  val counter = Counter(8)
  val clock = Reg(Bool) init (False)
  val latch = Reg(Bool) init (False)

  val animation = new SlowArea(16 Hz) {
    val offset = CounterFreeRun(8)
  }

  val pins = io.pmod1b
  pins.pin1 := (counter === animation.offset).asBits
  pins.pin2 := clock.asBits
  pins.pin3 := latch.asBits
  pins.pin8 := 0

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
