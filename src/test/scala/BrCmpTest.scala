import chisel3._
import chiseltest._
import common.Consts._
import controller.BrCmp
import org.scalatest.FlatSpec

class BrCmpTest extends FlatSpec with ChiselScalatestTester {
  "BrCmp" should "compare signed and unsigned boundaries for every branch" in {
    test(new BrCmp) { c =>
      val values = Seq(0L, 1L, 0x7fffffffL, 0x80000000L, 0xffffffffL)
      for (a <- values; b <- values) {
        c.io.rs1_data.poke(a.U); c.io.rs2_data.poke(b.U)
        val cases = Seq(B_X -> false, B_EQ -> (a == b), B_NE -> (a != b),
          B_LT -> (a.toInt < b.toInt), B_GE -> (a.toInt >= b.toInt),
          B_LTU -> (a < b), B_GEU -> (a >= b))
        for ((sel, expected) <- cases) {
          c.io.branch_sel.poke(sel); c.io.branch_enable.expect(expected.B)
        }
      }
    }
  }
}
