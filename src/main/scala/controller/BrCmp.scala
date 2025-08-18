package controller

import chisel3._
import chisel3.util.MuxLookup
import common.Consts._

class BrCmpIo extends Bundle{
  val rs1_data = Input(UInt(WORD_LEN.W))
  val rs2_data = Input(UInt(WORD_LEN.W))
  val branch_sel = Input(UInt(BRANCH_SEL_LEN.W))
  val branch_enable = Output(Bool())
}

class BrCmp extends Module {

val io = IO(new BrCmpIo())
  val rs1 = io.rs1_data
  val rs2 = io.rs2_data
io.branch_enable := MuxLookup(io.branch_sel, 0.B, Seq(
  B_EQ -> (rs1.asSInt === rs2.asSInt),
  B_NE -> (rs1.asSInt =/= rs2.asSInt),
  B_LT -> (rs1.asSInt < rs2.asSInt),
  B_GE -> (rs1.asSInt >= rs2.asSInt),
  B_LTU -> (rs1 < rs2),
  B_GEU -> (rs1 >= rs2),
  B_X   -> 0.B
))

}

