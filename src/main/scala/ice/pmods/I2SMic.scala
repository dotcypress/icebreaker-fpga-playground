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
    pcmWidth: BitCount = 24 bits,
    wordWidth: BitCount = 32 bits
) extends Component {
  val io = new Bundle {
    val channel = in(AudioChannel)
    val bitClock = in(Bool)
    val pins = master(I2SMic())
    val pcm = master(Flow(Bits(pcmWidth)))
  }

  val leftChannel = io.channel === AudioChannel.left
  val ws = Reg(Bool) init (False)
  val pcm = Reg(Bits(pcmWidth)) init (0)

  io.pins.pin2 := B(io.bitClock)
  io.pins.pin3 := B(~leftChannel)
  io.pins.pin4 := B(ws)
  io.pcm.payload := 0
  io.pcm.valid := False

  val frameWidth = wordWidth.value * 2
  val bitCounter = Counter(frameWidth)
  val sample = Reg(Bits(frameWidth bits)) init (0)

  when(io.bitClock.rise) {
    bitCounter.increment()
    sample(bitCounter) := io.pins.pin1.asBool

    when(bitCounter.willOverflow) {
      io.pcm.payload := leftChannel ?
        sample(1, pcmWidth).reversed |
        sample(0, pcmWidth).reversed
      io.pcm.valid := True
      ws := ~ws
    }
  }
}
