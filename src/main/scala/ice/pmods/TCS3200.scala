package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class TCS3200() extends PMODBundle {
  override def asMaster() = {
    in(pin2, pin3, pin4)
    out(pin1, pin7, pin8, pin9, pin10)
  }
}

case class Color(width: BitCount) extends Bundle {
  val luma, red, green, blue = UInt(width)
}

object Channel extends SpinalEnum {
  val luma, red, green, blue = newElement()
}

case class TCS3200Ctrl(
    width: BitCount = 8 bits,
    measurePeriod: TimeNumber = 2 ms
) extends Component {
  val io = new Bundle {
    val pins = master(TCS3200())
    val color = master(Flow(Color(width)))
  }

  val fsm = new StateMachine {
    val measure = new State() with EntryPoint
    val store = new State()

    val channel = Reg(Channel) init (Channel.luma)
    val luma, red, green, blue = Reg(UInt(width)) init (0)
    val pulseCounter = Counter(width)
    val measureTimer = Timeout(measurePeriod)

    val s0, s1 = True
    val s2 = channel === Channel.luma || channel === Channel.green
    val s3 = channel === Channel.blue || channel === Channel.green

    io.color.payload.luma := luma
    io.color.payload.red := red
    io.color.payload.green := green
    io.color.payload.blue := blue
    io.color.valid := False

    val vco = io.pins.pin4.as(Bool)
    io.pins.pin7 := B(0)
    io.pins.pin8 := B(s0)
    io.pins.pin1 := B(s1)
    io.pins.pin10 := B(s2)
    io.pins.pin9 := B(s3)

    measure.whenIsActive {
      when(vco.rise()) {
        pulseCounter.increment()
      }

      when(measureTimer) {
        measureTimer.clear()
        pulseCounter.clear()

        switch(channel) {
          is(Channel.luma) {
            luma := pulseCounter
            channel := Channel.red
            goto(measure)
          }
          is(Channel.red) {
            red := pulseCounter
            channel := Channel.green
            goto(measure)
          }
          is(Channel.green) {
            green := pulseCounter
            channel := Channel.blue
            goto(measure)
          }
          is(Channel.blue) {
            blue := pulseCounter
            channel := Channel.luma
            goto(store)
          }
        }
      }
    }

    store.whenIsActive {
      io.color.valid := True
      goto(measure)
    }
  }
}
