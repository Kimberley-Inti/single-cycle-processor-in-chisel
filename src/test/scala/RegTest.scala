import chisel3._
import chiseltest._
import org.scalatest.FlatSpec
import rf.GRF

class RegTest extends FlatSpec with ChiselScalatestTester {
  "GRF" should "reset, write synchronously, disable writes and preserve x0" in {
    test(new GRF) { c =>
      c.io.reg_w.poke(false.B)
      c.io.rd_addr.poke(0.U); c.io.rd_data.poke(0.U)
      c.io.rs1_addr.poke(0.U); c.io.rs2_addr.poke(0.U); c.io.debug_index.poke(0.U)
      c.reset.poke(true.B); c.clock.step(); c.reset.poke(false.B)
      for (i <- 0 until 32) {
        c.io.rs1_addr.poke(i.U); c.io.rs1_data.expect(0.U)
      }
      for (i <- 0 until 32) {
        val value = BigInt("80000000", 16) + i
        c.io.rd_addr.poke(i.U); c.io.rd_data.poke(value.U); c.io.reg_w.poke(true.B)
        c.clock.step()
        c.io.rs1_addr.poke(i.U); c.io.rs2_addr.poke(i.U); c.io.debug_index.poke(i.U)
        val expected = if (i == 0) BigInt(0) else value
        c.io.rs1_data.expect(expected.U); c.io.rs2_data.expect(expected.U)
        c.io.debug_data.expect(expected.U)
      }
      c.io.reg_w.poke(false.B); c.io.rd_addr.poke(1.U); c.io.rd_data.poke(123.U)
      c.clock.step(); c.io.rs1_addr.poke(1.U)
      c.io.rs1_data.expect(BigInt("80000001", 16).U)
    }
  }
}
