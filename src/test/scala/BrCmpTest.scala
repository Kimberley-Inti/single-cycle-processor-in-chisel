import chiseltest._
import org.scalatest.FlatSpec
import chisel3._
import common.Consts._
import controller.BrCmp


class BrCmpTest extends FlatSpec  with ChiselScalatestTester {
  "BrCmp" should "work through combinational logic" in {
    test(new BrCmp) { c =>
      c.io.rs1_data.poke(10.U)
      c.io.rs2_data.poke(5.U)
      c.io.branch_sel.poke(B_NE)
      println(f"rs1_data: 0x${c.io.rs1_data.peek().litValue}, rs2_data: 0x${c.io.rs2_data.peek().litValue}")
      println(f"branch_sel: 0x${c.io.branch_sel.peek().litValue}")
      c.io.branch_enable.expect(true.B)
      println(f"actual: 0x${c.io.branch_enable.peek().litValue}, expect: 1")
    }
  }

}