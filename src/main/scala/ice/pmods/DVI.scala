package ice.pmods

import spinal.core._
import spinal.lib._
import spinal.lib.fsm._
import spinal.lib.graphic._
import spinal.lib.graphic.vga._

case class DVI() extends PMODBundle {
  override def build() = {
    out(pin1, pin2, pin3, pin4, pin7, pin8, pin9, pin10)
  }
}

case class DVICtrl() extends Component {
  val rgbConfig = RgbConfig(4, 4, 4)
  val io = new Bundle {
    val pinsA = pmod(DVI())
    val pinsB = pmod(DVI())
    val pixels = slave(Stream(Rgb(rgbConfig)))
    val frameStart = out(Bool)
  }

  val ctrl = new VgaCtrl(rgbConfig)

  ctrl.io.softReset := False
  ctrl.io.timings.setAs_h640_v480_r60
  ctrl.io.pixels <> io.pixels

  io.frameStart := ctrl.io.frameStart

  io.pinsB.pin2 := ClockDomain.current.readClockWire
  io.pinsB.pin9 := ctrl.io.vga.colorEn
  io.pinsB.pin4 := ctrl.io.vga.hSync
  io.pinsB.pin10 := ctrl.io.vga.vSync

  io.pinsA.pin8 := ctrl.io.vga.color.r(0)
  io.pinsA.pin2 := ctrl.io.vga.color.r(1)
  io.pinsA.pin7 := ctrl.io.vga.color.r(2)
  io.pinsA.pin1 := ctrl.io.vga.color.r(3)

  io.pinsA.pin10 := ctrl.io.vga.color.g(0)
  io.pinsA.pin4 := ctrl.io.vga.color.g(1)
  io.pinsA.pin9 := ctrl.io.vga.color.g(2)
  io.pinsA.pin3 := ctrl.io.vga.color.g(3)

  io.pinsB.pin3 := ctrl.io.vga.color.b(0)
  io.pinsB.pin8 := ctrl.io.vga.color.b(1)
  io.pinsB.pin7 := ctrl.io.vga.color.b(2)
  io.pinsB.pin1 := ctrl.io.vga.color.b(3)
}
