package imm

import chisel3._
import chisel3.util._
import common.Consts._

class ImmIo extends Bundle{
  val inst = Input(UInt(WORD_LEN.W))
  val immo = Output(UInt(WORD_LEN.W))
  val imm_sel = Input(UInt(IMM_SEL_LEN.W))
}

class ImmSel extends Module{

  val io = IO(new ImmIo())

  val iImm = io.inst(31, 20)
  val iImmSext = Cat(Fill(20, iImm(11)), iImm)
  val sImm = Cat(io.inst(31, 25), io.inst(11, 7))
  val sImmSext = Cat(Fill(20, sImm(11)), sImm)
  val bImm = Cat(io.inst(31), io.inst(7), io.inst(30, 25), io.inst(11, 8))
  val bImmSext = Cat(Fill(19, bImm(11)), bImm, 0.U(1.W))
  val uImm = io.inst(31, 12)
  val uImmSext = Cat(uImm, Fill(12, 0.U))
  val jImm = Cat(io.inst(31), io.inst(19, 12), io.inst(20), io.inst(30,21))
  val jImmSext = Cat(Fill(11, jImm(19)), jImm, 0.U(1.W))

  io.immo := MuxLookup(io.imm_sel, 0.U(WORD_LEN.W), Seq(
    IMM_I -> iImmSext,
    IMM_S -> sImmSext,
    IMM_B -> bImmSext,
    IMM_J -> jImmSext,
    IMM_U -> uImmSext
  ))

}
