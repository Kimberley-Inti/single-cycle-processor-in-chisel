import Core.Top
import chiseltest.ChiselScalatestTester
import org.scalatest.FlatSpec
import chiseltest._
import chisel3._
import chisel3.util.experimental.loadMemoryFromFile
import common.Consts.{REG_INDEX_LEN, WORD_LEN}

class TopTest extends FlatSpec  with ChiselScalatestTester {
  "Top" should "work through hex" in {
    test(new Top("src/hex/final.hex")) {
      var clk = 0
      c =>
      while (!c.io.exit.peek().litToBoolean) {
        println(s"--------clk: ${clk}------------")
        println(f"PC = 0x${c.io.pr_out.pc.peek().litValue()}%08x, Inst = 0x${c.io.pr_out.inst.peek().litValue}%08x")
        //println(f"RS1: ${c.io.pr_out.RS1.peek().litValue},RS2: ${c.io.pr_out.RS2.peek().litValue},RD: ${c.io.pr_out.RD.peek().litValue} ")
        // reg write
        println(f"rd_index: ${c.io.pr_out.RD.peek().litValue}, rd_data: ${c.io.pr_out.rd_data.peek().litValue} ")
        //println(f"ALU = ${c.io.pr_out.ALU.peek().litValue}")
        //println(f"pc_sel: ${c.io.pr_out.pc_sel.peek().litValue},alu_ctr: ${c.io.pr_out.alu_ctr.peek().litValue} ")
        //println(f"op1_sel: ${c.io.pr_out.op1_sel.peek().litValue},op2_sel: ${c.io.pr_out.op2_sel.peek().litValue},imm_sel: ${c.io.pr_out.imm_sel.peek().litValue} ")
        println(f"regw: ${c.io.pr_out.regw.peek().litValue},wb_sel: ${c.io.pr_out.wb_sel.peek().litValue} ")
        //println(f"memrw: ${c.io.pr_out.memrw.peek().litValue},dmem: ${c.io.pr_out.dmem.peek().litValue} ")
        c.clock.step(1)
        clk = clk + 1
      }
      println(s"--------Finish------------")
      //c.dmem.io.addr.poke("h_2000".U(WORD_LEN.W))
      // c.imem.io.addr.poke("h_2000".U)
      // c.dmem.io.data_out.expect("h_d".U)
      // println(s"Mem:${c.dmem.io.debug_data.peek().litValue()}")
      c.io.dmem_en.poke(true.B)
      c.io.dmem_addr.poke(0x2004.U)
      c.clock.step(1)
      val dmem_out = c.io.dmem_data.peek().litValue
      println(f"0x$dmem_out%x")
      c.io.dmem_data.expect(BigInt("beefdead", 16).U)
    }
  }

}
