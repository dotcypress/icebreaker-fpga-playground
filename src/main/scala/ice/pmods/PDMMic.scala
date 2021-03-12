package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class PDMMic() extends PMODBundle {
  override def asMaster() = {
    out(pin2, pin3)
    in(pin1, pin4, pin7, pin8, pin9, pin10)
  }
}

case class PDMMicCtrl(wordWidth: BitCount) extends Component {
  val io = new Bundle {
    val audioClock = in(Bool)
    val pins = master(PDMMic())
    val pcm = master(Flow(Bits(wordWidth)))
  }

  val pdmFilter = new PdmFilter(wordWidth.value)
  pdmFilter.io.pdm_dat := io.pins.pin4.asBool
  io.pcm.payload := pdmFilter.io.pcm.asBits
  io.pcm.valid := pdmFilter.io.pcm_vld

  io.pins.pin2 := B(io.audioClock)
  io.pins.pin3 := 0
}

class PdmFilter(
    nrBits: Int,
    nrStages: Int = 5,
    decimation: Int = 48
) extends Component {
  val io = new Bundle {
    val pdm_dat = in(Bool)
    val pcm_vld = out(Bool)
    val pcm = out(UInt(nrBits bits))
  }

  val integrators_output = Reg(UInt(nrBits bits)) init (0)

  val integrators = new Area {
    var input = UInt(nrBits bits)

    input := (False ## io.pdm_dat).resize(nrBits).asUInt

    val stages = for (stageNr <- 0 to nrStages - 1) yield new Area {
      val output = Reg(UInt(nrBits bits)) init (0)
      output := input + output
      input = UInt(nrBits bits)
      input := output

      if (stageNr == nrStages - 1)
        integrators_output := output
    }
  }

  //============================================================
  // Decimator
  //============================================================

  val decimator = new Area {
    val decim_cntr = Reg(UInt(log2Up(decimation) bits)) init (0)
    val sample_vld = Bool

    when(decim_cntr === 0) {
      decim_cntr := decimation - 1
      sample_vld := True
    }
      .otherwise {
        decim_cntr := decim_cntr - 1
        sample_vld := False
      }
  }

  //============================================================
  // CIC Combs
  //============================================================

  val combs_output_vld = Reg(Bool) init (False)
  val combs_output = Reg(UInt(nrBits bits)) init (0)

  val combs = new Area {

    var input_vld = Bool
    var input = UInt(nrBits bits)

    input_vld := decimator.sample_vld
    input := integrators_output

    val stages = for (stageNr <- 0 to nrStages - 1) yield new Area {
      var input_dly = Reg(UInt(nrBits bits)) init (0)
      var output_vld = Reg(Bool) init (False)
      var output = Reg(UInt(nrBits bits)) init (0)

      output_vld := input_vld
      when(input_vld) {
        output := input - input_dly
        input_dly := input
      }

      input_vld = Bool
      input = UInt(nrBits bits)

      input_vld := output_vld
      input := output

      if (stageNr == nrStages - 1) {
        combs_output_vld := output_vld
        combs_output := output
      }
    }
  }

  io.pcm_vld := combs_output_vld
  io.pcm := combs_output
}
