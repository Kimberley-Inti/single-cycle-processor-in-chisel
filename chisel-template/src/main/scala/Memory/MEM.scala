package Memory

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile
import common.Consts._

class ImemIO extends Bundle{
  val addr = Input(UInt(WORD_LEN.W))
  val inst = Output(UInt(WORD_LEN.W))
}

class DmemIO extends Bundle{
  val addr = Input(UInt(WORD_LEN.W))
  val data_in = Input(UInt(WORD_LEN.W))
  val mem_rw = Input(Bool())
  val data_out = Output(UInt(WORD_LEN.W))

}

class IMem(progPath: String) extends Module {

  val io = IO(new ImemIO())

  val mem = Mem(1024, UInt(8.W))
  loadMemoryFromFile(mem, progPath)

  io.inst := Cat(
    mem(io.addr + 3.U(WORD_LEN.W)),
    mem(io.addr + 2.U(WORD_LEN.W)),
    mem(io.addr + 1.U(WORD_LEN.W)),
    mem(io.addr)
  )

}

class DMem extends Module {

  val io = IO(new DmemIO())
  val mem = Mem(1024, UInt(8.W))

  when(io.mem_rw){
    mem(io.addr) := io.data_in(7, 0)
    mem(io.addr + 1.U(WORD_LEN.W)) :=io.data_in(15, 8)
    mem(io.addr + 2.U(WORD_LEN.W)) :=io.data_in(23, 16)
    mem(io.addr + 3.U(WORD_LEN.W)) :=io.data_in(31, 24)
  }

  io.data_out := Cat(
    mem(io.addr + 3.U(WORD_LEN.W)),
    mem(io.addr + 2.U(WORD_LEN.W)),
    mem(io.addr + 1.U(WORD_LEN.W)),
    mem(io.addr))
}

