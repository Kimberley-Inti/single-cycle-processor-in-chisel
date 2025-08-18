import chiseltest._
import org.scalatest.FlatSpec
import chisel3._
import common.Consts._
import imm.ImmSel


class ImmSelTest extends FlatSpec  with ChiselScalatestTester {
  "ImmGen" should "work through combinational logic" in {
    test(new ImmSel) { c =>
      c.io.inst.poke("h_fddf_f0ef".asUInt(WORD_LEN.W))
      c.io.imm_sel.poke(IMM_J)
      println(f"instr: 0x${c.io.inst.peek().litValue}%08x")
      c.io.immo.expect("h_ffff_ffdc".asUInt(WORD_LEN.W))
      println(f"actual: 0x${c.io.immo.peek().litValue}%08x, expect: 0xffffffdc")
    }
  }
}