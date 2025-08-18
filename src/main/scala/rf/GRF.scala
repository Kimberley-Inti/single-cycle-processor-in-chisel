package rf

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._

class RegFileIO extends Bundle{
  val rs1_addr = Input(UInt(REG_INDEX_LEN.W))
  val rs2_addr = Input(UInt(REG_INDEX_LEN.W))
  val rs1_data = Output(UInt(WORD_LEN.W))
  val rs2_data = Output(UInt(WORD_LEN.W))
  val rd_addr = Input(UInt(REG_INDEX_LEN.W))
  val rd_data = Input(UInt(WORD_LEN.W))
  val reg_w  = Input(Bool())

  val debug_index = Input(UInt(REG_INDEX_LEN.W))
  val debug_data = Output(UInt(WORD_LEN.W))
}

class GRF extends Module{

  val io = IO(new RegFileIO())

  val regfile = RegInit(VecInit(Seq.fill(32)(0.U(WORD_LEN.W))))

  when(io.reg_w && io.rd_addr =/= 0.U){
    regfile(io.rd_addr) := io.rd_data
    //println(p"Write GRF X${io.rd_addr}: 0x${io.rd_data}%08x")
  }

  io.rs1_data := regfile(io.rs1_addr)
  io.rs2_data := regfile(io.rs2_addr)

  io.debug_data := regfile(io.debug_index)
}
