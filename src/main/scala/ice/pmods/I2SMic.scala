package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class I2SMic() extends PMODBundle {
  override def asMaster() = {
    out(pin2, pin3, pin4)
    in(pin1, pin7, pin8, pin9, pin10)
  }
}

object AudioChannel extends SpinalEnum {
  val left, right = newElement()
}

case class I2SMicCtrl(
    i2sClock: ClockDomain,
    pcmWidth: Int = 24,
    wordWidth: Int = 32
) extends Component {
  val io = new Bundle {
    val channel = in(AudioChannel)
    val pins = master(I2SMic())
    val pcm = master(Flow(Bits(pcmWidth bits)))
  }

  val fifo = new StreamFifoCC(
    Bits(pcmWidth bits),
    8,
    pushClock = i2sClock,
    popClock = clockDomain
  )
  fifo.io.pop.toFlow >> io.pcm

  val mic = new ClockingArea(i2sClock) {
    val ws = Reg(Bool) init (False)
    val pcm = Reg(Bits(pcmWidth bits)) init (0)
    fifo.io.push.payload := pcm
    fifo.io.push.valid := False

    val frameWidth = wordWidth * 2

    val bitCounter = CounterFreeRun(frameWidth)
    val sample = Reg(Bits(frameWidth bits)) init (0)
    sample(bitCounter) := io.pins.pin1.asBool

    when(bitCounter.willOverflowIfInc) {
      pcm := (io.channel === AudioChannel.left) ?
        sample(1, pcmWidth bits) |
        sample(32, pcmWidth bits)
      ws := ~ws
      fifo.io.push.valid := True
    }
  }

  io.pins.pin2 := B(i2sClock.readClockWire)
  io.pins.pin3 := B(io.channel === AudioChannel.right)
  io.pins.pin4 := B(mic.ws)
}
