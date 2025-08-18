package controller

import chisel3._
import chisel3.util._
import common.Consts._
import common.Instructions._

class CUIo extends Bundle{
    var inst = Input(UInt(WORD_LEN.W))

    val alu_ctr = Output(UInt(EXE_FUN_LEN.W))
    val reg_w = Output(Bool())
    val op1_sel = Output(Bool())
    val op2_sel = Output(Bool())
    val branch_sel = Output(UInt(BRANCH_SEL_LEN.W))
    val mem_rw = Output(Bool())
    val wb_sel = Output(UInt(WB_SEL_LEN.W))
    val imm_sel = Output(UInt(IMM_SEL_LEN.W))
    val jump = Output(Bool())
}

class CU extends Module{


    val io = IO(new CUIo())

    val signals = ListLookup(io.inst, List(ALU_X, OP1_RS1, OP2_IMM, IMM_I, B_X, WRITE_UEN, WRITE_UEN, WB_X, JUMP_UEN),
        Array(
        LUI    -> List(ALU_COPY2, OP1_RS1, OP2_IMM, IMM_U, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        AUIPC  -> List(ALU_ADD,   OP1_PC,  OP2_IMM, IMM_U, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        JAL    -> List(ALU_ADD,   OP1_PC,  OP2_IMM, IMM_J, B_X,     WRITE_EN,  WRITE_UEN, WB_PC  , JUMP_EN   ),
        JALR   -> List(ALU_ADD,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_PC  , JUMP_EN   ),
        BEQ    -> List(ALU_ADD,   OP1_PC, OP2_IMM, IMM_B, B_EQ,    WRITE_UEN, WRITE_UEN, WB_X    , JUMP_UEN  ),
        BNE    -> List(ALU_ADD,   OP1_PC, OP2_IMM, IMM_B, B_NE,    WRITE_UEN, WRITE_UEN, WB_X    , JUMP_UEN  ),
        BLT    -> List(ALU_ADD,   OP1_PC, OP2_IMM, IMM_B, B_LT,    WRITE_UEN, WRITE_UEN, WB_X    , JUMP_UEN  ),
        BGE    -> List(ALU_ADD,   OP1_PC, OP2_IMM, IMM_B, B_GE,    WRITE_UEN, WRITE_UEN, WB_X    , JUMP_UEN  ),
        BLTU   -> List(ALU_ADD,   OP1_PC, OP2_IMM, IMM_B, B_LTU,   WRITE_UEN, WRITE_UEN, WB_X    , JUMP_UEN  ),
        BGEU   -> List(ALU_ADD,   OP1_PC, OP2_IMM, IMM_B, B_GEU,   WRITE_UEN, WRITE_UEN, WB_X    , JUMP_UEN  ),
        LW     -> List(ALU_ADD,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_MEM , JUMP_UEN  ),
        SW     -> List(ALU_ADD,   OP1_RS1, OP2_IMM, IMM_S, B_X,     WRITE_UEN, WRITE_EN,  WB_X   , JUMP_UEN  ),
        ADDI   -> List(ALU_ADD,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SLTIU  -> List(ALU_SLTU,  OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        XORI   -> List(ALU_XOR,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        ORI    -> List(ALU_OR,    OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        ANDI   -> List(ALU_AND,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SLLI   -> List(ALU_SLL,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SRLI   -> List(ALU_SRL,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SRAI   -> List(ALU_SRA,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        ADD    -> List(ALU_ADD,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SUB    -> List(ALU_SUB,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SLL    -> List(ALU_SLL,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SLT    -> List(ALU_SLT,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SLTI   -> List(ALU_SLT,   OP1_RS1, OP2_IMM, IMM_I, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SLTU   -> List(ALU_SLTU,  OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        XOR    -> List(ALU_XOR,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SRL    -> List(ALU_SRL,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        SRA    -> List(ALU_SRA,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        OR     -> List(ALU_OR,    OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  ),
        AND    -> List(ALU_AND,   OP1_RS1, OP2_RS2, IMM_R, B_X,     WRITE_EN,  WRITE_UEN, WB_ALU , JUMP_UEN  )
        )
    )
    val aluop :: op1Sel :: op2Sel :: immSel :: branch_sel :: regW :: memW ::wbSel :: jump :: Nil = signals

    io.alu_ctr := aluop.asUInt
    io.op1_sel := op1Sel.asUInt
    io.op2_sel := op2Sel.asUInt
    io.imm_sel := immSel.asUInt
    io.branch_sel := branch_sel.asUInt
    io.reg_w  := regW.asBool
    io.mem_rw := memW.asBool
    io.wb_sel := wbSel.asUInt
    io.jump   := jump.asBool

}
