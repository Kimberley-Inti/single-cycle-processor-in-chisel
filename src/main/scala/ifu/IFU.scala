package ifu

import chisel3._
import chisel3.util._
import common.Consts._

class IFUIO extends Bundle{
  val addr = Output(UInt(WORD_LEN.W))
  val inst = Output(UInt(WORD_LEN.W))
  val data_in = Input(UInt(WORD_LEN.W))
  val pc_sel = Input(Bool())
  val npc = Input(UInt(WORD_LEN.W))

}

class IFU extends Module{

  val io = IO(new IFUIO())

  val pc_reg = RegInit(START_ADDR)

  val pc_4 = pc_reg + 4.U(WORD_LEN.W)

  pc_reg := Mux(io.pc_sel, io.npc, pc_4)

  io.addr := pc_reg
  io.inst := io.data_in

}
