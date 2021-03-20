package ice

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

import pmods._
import spinal.lib.misc.Prescaler

object LedAnimation {
  def main(args: Array[String]) = boards.IceBreaker.generate(new LedAnimation)
}

case class RotaryEncoder() extends PMODBundle {
  override def asMaster() = this.asInput()
}

case class ShiftRegister() extends PMODBundle {
  override def asMaster() = {
    in(pin7, pin8, pin9)
    out(pin1, pin2, pin3, pin4, pin10)
  }
}

case class LedAnimation() extends Component {
  val io = new Bundle {
    val pmod1a = master(SevenSegmentDisplay())
    val pmod2 = master(ShiftRegister())
    val pmod1b = master(RotaryEncoder())
  }

  val angle = Reg(UInt(3 bits)) init (0)

  val display = new SevenSegmentDisplayCtrl
  display.io.pins <> io.pmod1a
  display.io.enable := True
  display.io.value := angle.resized

  val rotaryEncoder = RotaryEncoderCtrl()
  rotaryEncoder.io.pins <> io.pmod1b

  when(rotaryEncoder.io.switch.rise) {
    angle := 0
  }

  when(rotaryEncoder.io.cw) {
    angle := angle + 1
  }

  when(rotaryEncoder.io.ccw) {
    angle := angle - 1
  }

  val ledData = Vec(UInt(4 bits), 8)

  val shiftReg = new STP08cp05Ctrl()
  shiftReg.io.pmod <> io.pmod2
  shiftReg.io.data := ledData

  val animation = new SlowArea(4 kHz) {
    ledData := Vec.tabulate(ledData.length)(renderLed)

    def renderLed(idx: Int): UInt = {
      Mux(idx === (7 - angle), 15, 0)
    }
  }
}

case class RotaryEncoderCtrl() extends Component {
  val io = new Bundle {
    val pins = master(RotaryEncoder())

    val switch = out(Bool)
    val cw = out(Bool)
    val ccw = out(Bool)
  }

  val encoderClock = debounce(io.pins.pin2)
  val encoderData = debounce(io.pins.pin3)
  io.switch := debounce(~io.pins.pin1)
  io.cw := False
  io.ccw := False

  when(encoderClock.rise) {
    io.cw := ~encoderData
    io.ccw := encoderData
  }
}

object debounce {
  def apply(input: Bits, factor: BitCount = 14 bits): Bool = {
    var state = RegInit(False)
    val counter = Counter(factor, input.asBool =/= state)
    when(counter.willOverflow) {
      state := ~state
    }
    state
  }
}

case class STP08cp05Ctrl() extends Component {
  val io = new Bundle {
    val pmod = master(ShiftRegister())
    val data = in(Vec(UInt(4 bits), 8))
  }

  val gamma = Vec[UInt](0, 1, 1, 2, 3, 5, 7, 10, 14, 18, 24, 30, 37, 45, 53, 63)
  val ledCounter = Counter(8)
  val pwmCounter = Counter(6 bits)
  val clock = Reg(Bool) init (False)
  val latch = Reg(Bool) init (False)
  val data = gamma(io.data(ledCounter)) > pwmCounter

  io.pmod.pin1 := B(clockDomain.newSlowedClockDomain(2 MHz).readClockWire)
  io.pmod.pin2 := latch.asBits
  io.pmod.pin3 := B(data)
  io.pmod.pin4 := 0
  io.pmod.pin10 := clock.asBits

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
