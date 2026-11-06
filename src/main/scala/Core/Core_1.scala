package Core

import chisel3.util._
import Memory.{DmemIO, ImemIO}
import alu.ALU
import common.Consts._
import common.Instructions.JALR
import controller.{BrCmp, CU}
import ifu.IFU
import imm.ImmSel
import rf.GRF
import chisel3._


class CoreIO extends Bundle{
  val imem = Flipped(new ImemIO())
  val dmem = Flipped(new DmemIO())

  // debug
  val debug_index = Input(UInt(REG_INDEX_LEN.W))
  val debug_data = Output(UInt(WORD_LEN.W))

  val prout = Output(new Bundle {
    val pc    = UInt(WORD_LEN.W)
    val inst  = UInt(WORD_LEN.W)
    val RS1   = UInt(REG_INDEX_LEN.W)
    val RS2   = UInt(REG_INDEX_LEN.W)
    val RD    = UInt(REG_INDEX_LEN.W)
    val rs1_data = UInt(WORD_LEN.W)
    val rs2_data = UInt(WORD_LEN.W)
    val rd_data  = UInt(WORD_LEN.W)
    val ALU      = UInt(WORD_LEN.W)
    val pc_sel   = Bool()
    val alu_ctr  = UInt(EXE_FUN_LEN.W)
    val op1_sel  = Bool()
    val op2_sel  = Bool()
    val imm_sel  = UInt(IMM_SEL_LEN.W)
    val regw     = Bool()
    val memrw    = Bool()
    val wb_sel   = UInt(WB_SEL_LEN.W)
    val dmem     = UInt(WORD_LEN.W)
  })
}

class Core_1 extends Module{
  val io = IO(new CoreIO())

  val ifu = Module(new IFU())
  val grf = Module(new GRF())
  val immsel = Module(new ImmSel())
  val alu = Module(new ALU())
  val cu = Module(new CU())
  val brcmp = Module(new BrCmp())

  //core
  io.imem.addr    := ifu.io.addr

  io.dmem.data_in := grf.io.rs2_data
  io.dmem.addr := alu.io.alu_result
  io.dmem.mem_rw := cu.io.mem_rw

  //ifu
  ifu.io.data_in  := io.imem.inst
  ifu.io.pc_sel := cu.io.jump || brcmp.io.branch_enable
  // JALR clears bit zero after adding the signed immediate (RV32I).
  ifu.io.npc := Mux(JALR === ifu.io.inst,
    alu.io.alu_result & "hfffffffe".U(WORD_LEN.W), alu.io.alu_result)

  //cu := ifu
  cu.io.inst      := ifu.io.inst

  //grf := ifu
  grf.io.rs1_addr := ifu.io.inst(19, 15)
  grf.io.rs2_addr := ifu.io.inst(24, 20)
  grf.io.rd_addr  := ifu.io.inst(11, 7)
  grf.io.rd_data  := MuxLookup(cu.io.wb_sel, 0.U(WORD_LEN.W), Seq(
    WB_ALU  -> alu.io.alu_result,
    WB_MEM  -> io.dmem.data_out,
    WB_PC   -> (ifu.io.addr + 4.U(WORD_LEN.W)),
    WB_X    -> 0.U(WORD_LEN.W)
  ))
  grf.io.reg_w    := cu.io.reg_w


  //brcmp := grf
  brcmp.io.rs1_data := grf.io.rs1_data
  brcmp.io.rs2_data := grf.io.rs2_data
  brcmp.io.branch_sel := cu.io.branch_sel

  //immsel
  immsel.io.imm_sel := cu.io.imm_sel
  immsel.io.inst := ifu.io.inst

  //alu
  alu.io.op1 := Mux(cu.io.op1_sel, ifu.io.addr, grf.io.rs1_data)
  alu.io.op2 := Mux(cu.io.op2_sel, immsel.io.immo, grf.io.rs2_data)
  alu.io.alu_ctr := cu.io.alu_ctr

  //info
  io.prout.pc   := ifu.io.addr
  io.prout.inst := ifu.io.inst
  io.prout.RS1  := grf.io.rs1_addr
  io.prout.RS2  := grf.io.rs2_addr
  io.prout.RD   := grf.io.rd_addr
  io.prout.rs1_data  := grf.io.rs1_data
  io.prout.rs2_data  := grf.io.rs2_data
  io.prout.rd_data   := grf.io.rd_data
  io.prout.ALU     := alu.io.alu_result
  io.prout.pc_sel  := ifu.io.pc_sel
  io.prout.alu_ctr := cu.io.alu_ctr
  io.prout.op1_sel := cu.io.op1_sel
  io.prout.op2_sel := cu.io.op2_sel
  io.prout.imm_sel := cu.io.imm_sel
  io.prout.regw   := cu.io.reg_w
  io.prout.memrw  := cu.io.mem_rw
  io.prout.wb_sel := cu.io.wb_sel
  io.prout.dmem   := io.dmem.data_out

  // debug
  grf.io.debug_index := io.debug_index
  io.debug_data := grf.io.debug_data
}
