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

  io.ledData := False
  io.colors.ready := False

  val fsm = new StateMachine {
    val read = new State with EntryPoint
    val send = new State
    val delay = new StateDelay(50 us)

    val pulseCounter = Counter(6)
    val bitCounter = Counter(rgbConfig.getWidth)
    val ledCounter = Counter(stripSize)
    val outbox = Reg(Bits(rgbConfig.getWidth bits))
    val mem = Mem(Bits(rgbConfig.getWidth bits), wordCount = stripSize)

    mem.write(
      enable = isActive(read) && io.colors.valid,
      address = ledCounter,
      data = Cat(
        Reverse(io.colors.payload.b.asBits),
        Reverse(io.colors.payload.r.asBits),
        Reverse(io.colors.payload.g.asBits)
      )
    )

    outbox := mem
      .readSync(
        enable = isActive(send),
        address = ledCounter
      )

    read
      .onEntry {
        ledCounter := 0
      }
      .whenIsActive {
        io.colors.ready := True

        when(io.colors.valid) {
          ledCounter.increment()
          when(ledCounter.willOverflow) {
            goto(send)
          }
        }
      }

    send
      .onEntry {
        ledCounter := 0
        bitCounter := 0
        pulseCounter := 0
      }
      .whenIsActive {
        pulseCounter.increment()

        io.ledData := Mux(
          outbox(bitCounter),
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

    delay.whenCompleted(goto(read))
  }
}
