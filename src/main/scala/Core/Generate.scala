package Core

import chisel3.stage.ChiselStage

/** sbt "runMain Core.Generate path/to/program.hex [output-directory]" */
object Generate extends App {
  require(args.length >= 1 && args.length <= 2,
    "Usage: Core.Generate <byte-per-line program.hex> [output-directory]")
  val output = if (args.length == 2) args(1) else "generated"
  (new ChiselStage).emitVerilog(new Top(args(0)), Array("--target-dir", output))
}
