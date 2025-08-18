package alu

import chisel3._
import chisel3.util._
import common.Consts._

class AluIO extends Bundle{

  val alu_ctr = Input(UInt(EXE_FUN_LEN.W))
  val alu_result = Output(UInt(WORD_LEN.W))
  val op1 = Input(UInt(WORD_LEN.W))
  val op2 = Input(UInt(WORD_LEN.W))

}

class ALU extends Module{

  val io = IO(new AluIO())

  io.alu_result := MuxLookup(io.alu_ctr, 0.U(WORD_LEN.W), Seq(
    ALU_ADD -> (io.op1 + io.op2),
    ALU_SUB-> (io.op1 - io.op2),
    ALU_AND -> (io.op1 & io.op2),
    ALU_OR  -> (io.op1 | io.op2),
    ALU_XOR -> (io.op1 ^ io.op2),
    ALU_SLL -> (io.op1 << io.op2(4,0)),
    ALU_SRL -> (io.op1 >> io.op2(4,0)),
    ALU_SRA -> (io.op1.asSInt >> io.op2(4,0)).asUInt,
    ALU_SLT -> (io.op1.asSInt < io.op2.asSInt).asUInt,
    ALU_SLTU -> (io.op1 < io.op2).asUInt,
    ALU_COPY2 -> io.op2,
    ALU_X    -> 0.U(WORD_LEN.W)

  )
  )

}
