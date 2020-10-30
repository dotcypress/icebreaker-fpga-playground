package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._

case class SevenSegmentDisplay() extends PMODBundle {
  override def build() = {
    out(pin1, pin2, pin3, pin4, pin7, pin8, pin9, pin10)
  }
}

case class SevenSegmentDisplayCtrl() extends Component {
  val io = new Bundle {
    val pins = pmod(SevenSegmentDisplay())
    val value = in(UInt(8 bits))
    val enable = in(Bool)
  }

  val fsm = new StateMachine {
    val showLowNibble = new State with EntryPoint
    val hideLowNibble = new State
    val showHighNibble = new State
    val hideHighNibble = new State

    val segments = Reg(UInt(7 bits)) init (0)

    io.pins.pin1 := ~io.enable | ~segments(0)
    io.pins.pin2 := ~io.enable | ~segments(1)
    io.pins.pin3 := ~io.enable | ~segments(2)
    io.pins.pin4 := ~io.enable | ~segments(3)
    io.pins.pin7 := ~io.enable | ~segments(4)
    io.pins.pin8 := ~io.enable | ~segments(5)
    io.pins.pin9 := ~io.enable | ~segments(6)

    val lowNibble = io.pins.pin10
    lowNibble := True

    showLowNibble.whenIsActive {
      segments := digitToSegments(io.value(3 downto 0))
      goto(hideLowNibble)
    }

    hideLowNibble.whenIsActive {
      segments := 0
      goto(showHighNibble)
    }

    showHighNibble.whenIsActive {
      segments := digitToSegments(io.value(7 downto 4))
      lowNibble := False
      goto(hideHighNibble)
    }

    hideHighNibble.whenIsActive {
      segments := 0
      lowNibble := False
      goto(showLowNibble)
    }

    def digitToSegments(digit: UInt) =
      digit.mux(
        U"x0" -> U"7'b0111111",
        U"x1" -> U"7'b0000110",
        U"x2" -> U"7'b1011011",
        U"x3" -> U"7'b1001111",
        U"x4" -> U"7'b1100110",
        U"x5" -> U"7'b1101101",
        U"x6" -> U"7'b1111101",
        U"x7" -> U"7'b0000111",
        U"x8" -> U"7'b1111111",
        U"x9" -> U"7'b1101111",
        U"xA" -> U"7'b1110111",
        U"xB" -> U"7'b1111100",
        U"xC" -> U"7'b0111001",
        U"xD" -> U"7'b1011110",
        U"xE" -> U"7'b1111001",
        U"xF" -> U"7'b1110001"
      )
  }
}
