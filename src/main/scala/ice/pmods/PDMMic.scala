package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import spinal.lib.misc.Prescaler

case class PDMMic() extends PMODBundle {
  override def asMaster() = {
    out(pin1)
    in(pin2, pin3, pin4, pin7, pin8, pin9, pin10)
  }
}

case class PDMMicCtrl(wordWidth: BitCount) extends Component {
  val io = new Bundle {
    val audioClock = in(Bool)
    val pins = master(PDMMic())
    val pcm = master(Flow(UInt(wordWidth)))
  }

  val pdmFilter = new PdmFilter(wordWidth)
  pdmFilter.io.pcm <> io.pcm
  pdmFilter.io.pdm.valid := io.audioClock.rise
  pdmFilter.io.pdm.payload := io.pins.pin7.asBool

  io.pins.pin1 := B(io.audioClock)
}

class PdmFilter(wordWidth: BitCount, decimation: Int = 60) extends Component {
  val io = new Bundle {
    val pdm = in(Flow(Bool))
    val pcm = out(Flow(UInt(wordWidth)))
  }

  val decimator = Counter(decimation, io.pdm.valid)

  io.pcm.valid := io.pdm.valid && decimator.willOverflow
  io.pcm.payload := History(
    io.pdm.payload,
    decimation,
    io.pdm.valid
  )
    .map(b => U(b).resize(wordWidth))
    .reduce(_ + _)
}
