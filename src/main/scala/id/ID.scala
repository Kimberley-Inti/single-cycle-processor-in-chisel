package id

import chisel3._
import common.Consts._

class IdIO extends Bundle{
  val inst = Input(UInt(WORD_LEN.W))
  val rs1_addr = Output(UInt(REG_INDEX_LEN.W))
  val rs2_addr = Output(UInt(REG_INDEX_LEN.W))
  val rd_addr = Output(UInt(REG_INDEX_LEN.W))
}

class ID extends Module {

  val io = IO(new IdIO())

  io.rs1_addr := io.inst(19, 15)
  io.rs2_addr := io. inst(24, 20)
  io.rd_addr := io.inst(11, 7)

}
