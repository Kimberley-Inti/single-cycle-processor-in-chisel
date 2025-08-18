import chiseltest._
import org.scalatest.FlatSpec
import chisel3._
import common.Consts._
import ifu.IFU

class IfuTest extends FlatSpec  with ChiselScalatestTester {
  "IFU" should "work through combinational logic" in {
    test(new IFU) { c =>
      c.io.data_in.poke("h_0000_8067".asUInt(WORD_LEN.W))
      c.io.pc_sel.poke(1.B)
      println(f"instr: 0x${c.io.pc_sel.peek().litValue}%08x")
      c.clock.step(1)
      c.io.addr.expect("h_0000_0040".asUInt(WORD_LEN.W))
      println(f"actual: 0x${c.io.addr.peek().litValue}%08x, expect: 0x00000040")
    }
  }
}