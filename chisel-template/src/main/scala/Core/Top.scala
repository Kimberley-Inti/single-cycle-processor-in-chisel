package Core

import chisel3._
import chisel3.util._
import chisel3.stage.ChiselStage
import common.Consts._
import common.Instructions._
import Core._
import Memory._
import exit._

class TopIO extends Bundle{

  val exit = Output(Bool())

  // debug
  val debug_index = Input(UInt(REG_INDEX_LEN.W))
  val debug_data = Output(UInt(WORD_LEN.W))

  val pr_out = Output(new Bundle {
    val pc   = UInt(WORD_LEN.W)
    val inst = UInt(WORD_LEN.W)
    val RS1  = UInt(REG_INDEX_LEN.W)
    val RS2  = UInt(REG_INDEX_LEN.W)
    val RD   = UInt(REG_INDEX_LEN.W)
    val rs1_data = UInt(WORD_LEN.W)
    val rs2_data = UInt(WORD_LEN.W)
    val rd_data  = UInt(WORD_LEN.W)
    val ALU  = UInt(WORD_LEN.W)
    val pc_sel  = Bool()
    val alu_ctr = UInt(EXE_FUN_LEN.W)
    val op1_sel = Bool()
    val op2_sel = Bool()
    val imm_sel = UInt(IMM_SEL_LEN.W)
    val regw   = Bool()
    val memrw  = Bool()
    val wb_sel = UInt(WB_SEL_LEN.W)
    val dmem   = UInt(WORD_LEN.W)
  })

  val dmem_addr = Input(UInt(WORD_LEN.W))
  val dmem_en = Input(Bool())
  val dmem_data = Output(UInt(WORD_LEN.W))

}

class Top(progPath:String) extends Module {

  val io = IO(new TopIO())

  val core = Module(new Core_1())
  val imem = Module(new IMem(progPath))
  val dmem = Module(new DMem())
  val exit = Module(new Exit())

  imem.io <> core.io.imem

  // dmme <> core
  dmem.io.mem_rw := core.io.dmem.mem_rw
  dmem.io.data_in :=  core.io.dmem.data_in
  dmem.io.addr  := core.io.dmem.addr
  core.io.dmem.data_out := dmem.io.data_out


  exit.io.inst := imem.io.inst
  io.exit := exit.io.dout

  io.pr_out := core.io.prout

  // debug
  core.io.debug_index := io.debug_index
  io.debug_data := core.io.debug_data


  when(io.dmem_en){
    dmem.io.addr := io.dmem_addr
  }
  io.dmem_data := dmem.io.data_out


}

