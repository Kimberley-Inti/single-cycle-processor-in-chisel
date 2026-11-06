import chisel3._
import chiseltest._
import common.Consts._
import imm.ImmSel
import org.scalatest.FlatSpec
import TestPrograms._

class ImmSelTest extends FlatSpec with ChiselScalatestTester {
  "ImmSel" should "decode all immediate formats including signed limits" in {
    test(new ImmSel) { c =>
      def check(inst: Long, sel: UInt, expected: Long): Unit = {
        c.io.inst.poke((inst & 0xffffffffL).U); c.io.imm_sel.poke(sel)
        c.io.immo.expect((expected & 0xffffffffL).U)
      }
      for (value <- Seq(-2048, -1, 0, 1, 2047)) {
        check(i(value, 1, 0, 2), IMM_I, value)
        check(s(value, 2, 1), IMM_S, value)
      }
      for (value <- Seq(-4096, -4, 0, 4, 4094))
        check(b(value, 2, 1, 0), IMM_B, value)
      for (value <- Seq(-1048576, -36, 0, 4, 1048574))
        check(j(value, 1), IMM_J, value)
      for (value <- Seq(0L, 0x12345000L, 0x80000000L, 0xfffff000L))
        check(u(value, 1, 0x37), IMM_U, value)
      check(0xffffffffL, IMM_R, 0)
      check(0xffffffffL, 7.U, 0)
    }
  }
}
