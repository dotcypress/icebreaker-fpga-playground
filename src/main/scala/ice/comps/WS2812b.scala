package ice

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import spinal.lib.graphic._

case class WS2812bCtrl(stripSize: Int) extends Component {
  val rgbConfig = RgbConfig(8, 8, 8)

  val io = new Bundle {
    val colors = slave(Stream(Rgb(rgbConfig)))
    val ledData = out(Bool)
  }

  val fsm = new StateMachine {
    val buffer = Reg(Vec(Bits(rgbConfig.getWidth bits), stripSize))
    val ledCounter = Counter(stripSize)
    val bitCounter = Counter(rgbConfig.getWidth)
    val pulseCounter = Counter(6)
    val ready = Reg(Bool) init (false)

    io.ledData := False
    io.colors.ready := ready

    val read: State = new State with EntryPoint {
      onEntry {
        ledCounter := 0
        ready := True
      }

      whenIsActive {
        when(io.colors.valid) {
          ledCounter.increment()

          val color = io.colors.payload
          buffer(ledCounter) := Cat(
            Reverse(color.b.asBits),
            Reverse(color.r.asBits),
            Reverse(color.g.asBits)
          )

          when(ledCounter.willOverflow) {
            goto(send)
          }
        }
      }
    }

    val send = new State {
      onEntry {
        ready := False
        ledCounter := 0
        bitCounter := 0
        pulseCounter := 0
      }

      whenIsActive {
        pulseCounter.increment()
        io.ledData := Mux(
          buffer(ledCounter)(bitCounter),
          pulseCounter < 3,
          pulseCounter < 5
        )

        when(pulseCounter.willOverflow) {
          bitCounter.increment()
        }

        when(bitCounter.willOverflow) {
          ledCounter.increment()
        }

        when(ledCounter.willOverflow) {
          goto(delay)
        }
      }
    }

    val delay = new StateDelay(50 us) {
      onEntry {
        ready := True
        ledCounter := 0
      }

      whenCompleted(goto(read))
    }
  }
}
