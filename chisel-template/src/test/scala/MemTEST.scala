import Memory.DMem
import chiseltest._
import org.scalatest.FlatSpec
import chisel3._
import common.Consts._



class MemTEST extends FlatSpec  with ChiselScalatestTester {
  "Mem" should "work through combinational logic" in {
    test(new DMem) { c =>
      c.io.addr.poke("h_fddf_f0ef".asUInt(WORD_LEN.W))

    }
  }
}