package exit

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._

class ExitIO extends Bundle {
  val inst = Input(UInt(WORD_LEN.W))
  val dout = Output(Bool())
}

class Exit extends Module {
  val io = IO(new ExitIO)

  io.dout := (io.inst === "h_00000073".U(WORD_LEN.W))
}