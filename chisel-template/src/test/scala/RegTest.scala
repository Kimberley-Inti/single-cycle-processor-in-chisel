import chiseltest._
import org.scalatest.FlatSpec
import chisel3._
import common.Consts._
import rf.GRF


class RegTest extends FlatSpec  with ChiselScalatestTester {
  "GRF" should "work through combinational logic" in {
    test(new GRF) { c =>
      c.io.rs1_addr.poke("h_fddf_f0ef".asUInt(WORD_LEN.W))
      println(f"rs1_address: 0x${c.io.rs1_addr.peek().litValue}%08x")
      c.io.rs1_data.expect("h_ffff_ffdc".asUInt(WORD_LEN.W))
      println(f"actual: 0x${c.io.rs1_data.peek().litValue}%08x, expect: 0xffffffdc")
    }
  }
}