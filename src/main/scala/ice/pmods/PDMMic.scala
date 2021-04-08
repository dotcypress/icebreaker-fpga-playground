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
  pdmFilter.io.pdm.valid := io.audioClock.fall
  pdmFilter.io.pdm.payload := io.pins.pin7.asUInt

  io.pins.pin1 := B(io.audioClock)
}

class PdmFilter(wordWidth: BitCount, stages: Int = 4, decimation: Int = 64)
    extends Component {
  val io = new Bundle {
    val pdm = in(Flow(UInt(1 bit)))
    val pcm = out(Flow(UInt(wordWidth)))
  }

  val intergators = new Integrators(wordWidth, stages)
  val decimator = Counter(decimation, intergators.io.output.valid)
  val combs = new Combs(wordWidth, stages)

  intergators.io.input <> io.pdm.resized
  combs.io.input <> intergators.io.output.takeWhen(decimator.willOverflow)
  combs.io.output <> io.pcm
}

class Integrators(wordWidth: BitCount, stages: Int) extends Component {
  val io = new Bundle {
    val input = slave(Flow(UInt(wordWidth)))
    val output = out(Flow(UInt(wordWidth)))
  }

  var input = UInt(wordWidth)
  var inputValid = Bool()

  input := io.input.payload
  inputValid := io.input.valid

  val filterOutput = Reg(UInt(wordWidth)) init (0)
  io.output.payload := filterOutput
  io.output.valid := inputValid

  for (stage <- 0 to (stages - 1)) yield new Area {
    val stageOutput = Reg(UInt(wordWidth)) init (0)

    when(inputValid) {
      stageOutput := input + stageOutput
    }

    input = UInt(wordWidth)
    input := stageOutput

    if (stage == stages - 1) {
      filterOutput := stageOutput
    }
  }
}

class Combs(wordWidth: BitCount, stages: Int) extends Component {
  val io = new Bundle {
    val input = slave(Flow(UInt(wordWidth)))
    val output = out(Flow(UInt(wordWidth)))
  }

  val filterOutput = Reg(UInt(wordWidth)) init (0)
  val filterOutputValid = Reg(Bool) init (False)

  io.output.payload := filterOutput
  io.output.valid := filterOutputValid

  var inputValid = Bool
  var input = UInt(wordWidth)

  inputValid := io.input.valid
  input := io.input.payload

  for (stage <- 0 to stages - 1) yield new Area {
    var inputDelayed = Reg(UInt(wordWidth)) init (0)
    var stageOutputValid = Reg(Bool) init (False)
    var stageOutput = Reg(UInt(wordWidth)) init (0)

    stageOutputValid := inputValid
    when(inputValid) {
      stageOutput := input - inputDelayed
      inputDelayed := input
    }

    inputValid = Bool
    input = UInt(wordWidth)

    inputValid := stageOutputValid
    input := stageOutput

    if (stage == stages - 1) {
      filterOutputValid := stageOutputValid
      filterOutput := stageOutput
    }
  }
}
