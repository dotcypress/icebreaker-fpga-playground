package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class MemoryLCD() extends PMODBundle {
  override def build() = {
    out(pin1, pin2, pin3, pin4, pin7, pin8, pin9, pin10)
  }
}

case class MemoryLCDCtrl(width: Int = 400, height: Int = 240)
    extends Component {
  val io = new Bundle {
    val displayOn = in(Bool)
    val buzzer = in(Bool)
    val hit = in(Bool)
    val pins = pmod(MemoryLCD())
    val pixels = master(Flow(Pixel(width, height)))
  }

  val powerOn = io.pins.pin1
  val vcomIntMode = io.pins.pin2
  val vcom = io.pins.pin3
  val buzzer = io.pins.pin4
  val displayClock = io.pins.pin10
  val data = io.pins.pin9
  val chipSelect = io.pins.pin8
  val displayOn = io.pins.pin7

  vcomIntMode := False
  buzzer := io.buzzer

  new SlowArea(10 Hz) {
    vcom := CounterFreeRun(2).willOverflow
  }

  displayClock := CounterFreeRun(2).willOverflow

  val displayClockDomain = ClockDomain(~displayClock, clockDomain.readResetWire)
    .setSyncWith(clockDomain)

  val displayArea = new ClockingArea(displayClockDomain) {
    val fsm = new StateMachine {

      val bit = Counter(8)
      val x = Counter(width)
      val y = Counter(height)

      io.pixels.payload.x := x
      io.pixels.payload.y := y

      powerOn := io.displayOn
      displayOn := io.displayOn

      data := False

      val idle: State = new State with EntryPoint {
        whenIsActive {
          x.clear()
          y.clear()
          bit.clear()
          goto(sendCommand)
        }
      }

      val sendCommand: State = new State {
        whenIsActive {
          data := bit === 0
          bit.increment()
          when(bit.willOverflow) {
            bit.clear()
            goto(sendAddress)
          }
        }
      }

      val sendAddress: State = new State {
        whenIsActive {
          data := y(bit)
          bit.increment()
          when(bit.willOverflow) {
            goto(sendRow)
          }
        }
      }

      val sendRow: State = new State {
        whenIsActive {
          data := ~io.hit
          x.increment()
          when(x.willOverflow) {
            goto(sendRowTrailer)
          }
        }
      }

      val sendRowTrailer: State = new StateDelay(8) {
        whenCompleted {
          y.increment()
          when(y.willOverflow) {
            goto(sendFrameTrailer)
          } otherwise {
            goto(sendAddress)
          }
        }
      }

      val sendFrameTrailer: State = new StateDelay(8) {
        whenCompleted {
          goto(idle)
        }
      }

      chipSelect := ~isActive(idle)
      io.pixels.valid := isActive(sendRow)
    }
  }
}
