package common

import chisel3._
import chisel3.util._

object Consts {

  val WORD_LEN = 32
  val START_ADDR = 0.U(WORD_LEN.W)
  val BUBBLE = 0x00000013.U(WORD_LEN.W) // [ADDI x0,x0,0] = BUBBLE

  val REG_INDEX_LEN = 5 // rs1,rs2,wb

  val VTYPE_ADDR = 0xC21
  val EXE_FUN_LEN = 4
  val ALU_X = 0.U(EXE_FUN_LEN.W)
  val ALU_ADD = 1.U(EXE_FUN_LEN.W)
  val ALU_SUB = 2.U(EXE_FUN_LEN.W)
  val ALU_AND = 3.U(EXE_FUN_LEN.W)
  val ALU_OR = 4.U(EXE_FUN_LEN.W)
  val ALU_XOR = 5.U(EXE_FUN_LEN.W)
  val ALU_SLL = 6.U(EXE_FUN_LEN.W)
  val ALU_SRL = 7.U(EXE_FUN_LEN.W)
  val ALU_SRA = 8.U(EXE_FUN_LEN.W)
  val ALU_SLT = 9.U(EXE_FUN_LEN.W)
  val ALU_SLTU = 10.U(EXE_FUN_LEN.W)
  val ALU_COPY1 = 11.U(EXE_FUN_LEN.W)
  val ALU_COPY2 = 12.U(EXE_FUN_LEN.W)


  val OP1_RS1 = 0.B
  val OP1_PC = 1.B



  val OP2_RS2 = 0.B
  val OP2_IMM = 1.B

  val MEN_LEN = 2
  val MEN_X = 0.U(MEN_LEN.W)
  val MEN_S = 1.U(MEN_LEN.W)
  val MEN_V = 2.U(MEN_LEN.W)

  val REN_LEN = 2
  val REN_X = 0.U(REN_LEN.W)
  val REN_S = 1.U(REN_LEN.W)
  val REN_V = 2.U(REN_LEN.W)

  val WB_SEL_LEN = 2
  val WB_ALU = 0.U(WB_SEL_LEN.W)
  val WB_MEM = 1.U(WB_SEL_LEN.W)
  val WB_PC = 2.U(WB_SEL_LEN.W)
  val WB_X = 3.U(WB_SEL_LEN.W)

  val MW_LEN = 3
  val MW_X = 0.U(MW_LEN.W)
  val MW_W = 1.U(MW_LEN.W)
  val MW_H = 2.U(MW_LEN.W)
  val MW_B = 3.U(MW_LEN.W)
  val MW_HU = 4.U(MW_LEN.W)
  val MW_BU = 5.U(MW_LEN.W)

  val IMM_SEL_LEN = 3
  val IMM_I = 0.U(IMM_SEL_LEN.W)
  val IMM_S = 1.U(IMM_SEL_LEN.W)
  val IMM_B = 2.U(IMM_SEL_LEN.W)
  val IMM_J = 3.U(IMM_SEL_LEN.W)
  val IMM_U = 4.U(IMM_SEL_LEN.W)
  val IMM_R = 5.U(IMM_SEL_LEN.W)


  val BRANCH_SEL_LEN = 3
  val B_X = 0.U(BRANCH_SEL_LEN.W)
  val B_EQ = 1.U(BRANCH_SEL_LEN.W)
  val B_NE = 2.U(BRANCH_SEL_LEN.W)
  val B_LT = 3.U(BRANCH_SEL_LEN.W)
  val B_GE = 4.U(BRANCH_SEL_LEN.W)
  val B_LTU = 5.U(BRANCH_SEL_LEN.W)
  val B_GEU = 6.U(BRANCH_SEL_LEN.W)

  val WRITE_EN = 1.B
  val WRITE_UEN = 0.B

  val JUMP_UEN = 0.B
  val JUMP_EN = 1.B

}
