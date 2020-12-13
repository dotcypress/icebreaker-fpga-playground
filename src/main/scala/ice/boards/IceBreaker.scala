package ice.boards

import java.nio.file._
import spinal.core._
import spinal.lib._

object IceBreaker {
  def generate[T <: Component](gen: => T) {
    val targetDirectory = Paths.get("target/bitstream")
    if (!Files.exists(targetDirectory)) {
      Files.createDirectory(targetDirectory)
    }
    new SpinalConfig(
      defaultClockDomainFrequency = FixedFrequency(12 MHz),
      defaultConfigForClockDomains = ClockDomainConfig(
        resetKind = ASYNC,
        resetActiveLevel = LOW
      ),
      targetDirectory = targetDirectory.toString()
    ).generateVerilog(gen)
  }
}
