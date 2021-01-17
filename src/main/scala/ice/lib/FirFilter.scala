package ice.lib

import spinal.core._
import spinal.lib._

class FirFilter(width: BitCount, taps: Seq[Int]) extends Component {
  val io = new Bundle {
    val source = slave(Flow(UInt(width)))
    val result = out(UInt(width))
  }

  val history = History(
    io.source.payload,
    taps.length,
    io.source.valid
  )

  io.result := RegNext(
    Vec
      .tabulate(taps.length)(i => history(i) * taps(i))
      .reduce(_ + _)
      .resize(width)
  )
}
