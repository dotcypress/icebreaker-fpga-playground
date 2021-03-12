package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class I2SMicrophone() extends PMODBundle {
  override def asMaster() = {
    out(pin2, pin3, pin4)
    in(pin1, pin7, pin8, pin9, pin10)
  }
}

object AudioChannel extends SpinalEnum {
  val left, right = newElement()
}

case class I2SMicrophoneCtrl(
    i2sClock: ClockDomain,
    wordWidth: BitCount = 32 bits,
    pcmWidth: BitCount = 24 bits
) extends Component {
  assert(wordWidth.value - pcmWidth.value - 1 > 0)
  val io = new Bundle {
    val channel = in(AudioChannel)
    val pins = master(I2SMicrophone())
    val pcm = master(Flow(UInt(pcmWidth)))
  }

  val fifo = new StreamFifoCC(
    UInt(pcmWidth),
    32,
    pushClock = i2sClock,
    popClock = clockDomain
  )
  fifo.io.pop.toFlow >> io.pcm

  val mic = new ClockingArea(i2sClock) {
    val ws = Reg(Bool) init (False)
    val sample = Reg(Bits(wordWidth)) init (0)

    val pcm = Reg(UInt(pcmWidth)) init (0)
    fifo.io.push.payload := pcm
    fifo.io.push.valid := False

    val bitCounter = CounterFreeRun(wordWidth.value)
    sample(bitCounter) := ~io.pins.pin1.asBool

    when(bitCounter.willOverflow) {
      pcm := sample(1, pcmWidth).reversed.asSInt.abs
      fifo.io.push.valid := ws
      ws := ~ws
    }
  }

  io.pins.pin2 := B(i2sClock.readClockWire)
  io.pins.pin3 := B(io.channel === AudioChannel.right)
  io.pins.pin4 := B(mic.ws)
}
