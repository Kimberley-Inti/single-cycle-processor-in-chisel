import Core.Core_1
import chisel3._
import chiseltest._
import org.scalatest.FlatSpec
import TestPrograms._

class CoreTest extends FlatSpec with ChiselScalatestTester {
  "Core" should "execute every implemented instruction against software expectations" in {
    test(new Core_1) { c =>
      c.clock.setTimeout(0)
      c.io.imem.inst.poke(0x13.U); c.io.dmem.data_out.poke(0.U)
      c.io.debug_index.poke(0.U)
      var pc = 0L
      def step(inst: Long, write: Option[(Int, Long)] = None,
               target: Option[Long] = None, store: Option[(Long, Long)] = None,
               load: Long = 0L): Unit = {
        c.io.imem.inst.poke((inst & 0xffffffffL).U)
        c.io.dmem.data_out.poke((load & 0xffffffffL).U)
        c.io.imem.addr.expect(pc.U)
        c.io.dmem.mem_rw.expect(store.isDefined.B)
        store.foreach { case (address, value) =>
          c.io.dmem.addr.expect((address & 0xffffffffL).U)
          c.io.dmem.data_in.expect((value & 0xffffffffL).U)
        }
        c.clock.step()
        pc = target.getOrElse(pc + 4) & 0xffffffffL
        c.io.imem.addr.expect(pc.U)
        write.foreach { case (reg, value) =>
          c.io.debug_index.poke(reg.U)
          c.io.debug_data.expect((if (reg == 0) 0L else value & 0xffffffffL).U)
        }
        c.io.debug_index.poke(0.U); c.io.debug_data.expect(0.U)
      }
      def set(reg: Int, value: Long): Unit = {
        val low = (value & 0xfff).toInt
        val signedLow = if (low >= 2048) low - 4096 else low
        val upper = (value - signedLow) & 0xfffff000L
        step(u(upper, reg, 0x37), Some(reg -> upper))
        step(i(signedLow, reg, 0, reg), Some(reg -> value))
      }
      val random = new scala.util.Random(20261106L)
      val pairs = Seq(0L -> 0L, 1L -> 31L, 0xffffffffL -> 32L,
        0x80000000L -> 1L, 0x7fffffffL -> 0xffffffffL,
        0x80000000L -> 0x7fffffffL) ++
        (0 until 10).map(_ => (random.nextInt().toLong & 0xffffffffL,
          random.nextInt().toLong & 0xffffffffL))
      for ((a, valueB) <- pairs) {
        set(1, a); set(2, valueB)
        val sh = (valueB & 31).toInt
        val ops = Seq(
          (0, 0, a + valueB), (0x20, 0, a - valueB),
          (0, 1, a << sh), (0, 2, if (a.toInt < valueB.toInt) 1L else 0L),
          (0, 3, if (a < valueB) 1L else 0L), (0, 4, a ^ valueB),
          (0, 5, a >>> sh), (0x20, 5, (a.toInt >> sh).toLong),
          (0, 6, a | valueB), (0, 7, a & valueB))
        for ((f7, f3, result) <- ops)
          step(r(f7, 2, 1, f3, 3), Some(3 -> result))
        for (imm <- Seq(-2048, -1, 0, 2047)) {
          val unsignedImm = imm.toLong & 0xffffffffL
          val immediateOps = Seq(0 -> (a + imm), 2 -> (if (a.toInt < imm) 1L else 0L),
            3 -> (if (a < unsignedImm) 1L else 0L), 4 -> (a ^ unsignedImm),
            6 -> (a | unsignedImm), 7 -> (a & unsignedImm))
          for ((f3, result) <- immediateOps)
            step(i(imm, 1, f3, 3), Some(3 -> result))
        }
        for (n <- Seq(0, 1, 31)) {
          step(i(n, 1, 1, 3), Some(3 -> (a << n)))
          step(i(n, 1, 5, 3), Some(3 -> (a >>> n)))
          step(i(0x400 | n, 1, 5, 3), Some(3 -> (a.toInt >> n).toLong))
        }
        val branches = Seq(0 -> (a == valueB), 1 -> (a != valueB),
          4 -> (a.toInt < valueB.toInt), 5 -> (a.toInt >= valueB.toInt),
          6 -> (a < valueB), 7 -> (a >= valueB))
        for ((f3, taken) <- branches; offset <- Seq(-16, 12))
          step(b(offset, 2, 1, f3), target = Some(pc + (if (taken) offset else 4)))
      }
      step(u(0x80000000L, 3, 0x17), Some(3 -> (pc + 0x80000000L)))
      step(j(-32, 3), Some(3 -> (pc + 4)), Some(pc - 32))
      set(1, 0x105L)
      step(i(-4, 1, 0, 3, 0x67), Some(3 -> (pc + 4)), Some(0x100L))
      step(i(1, 0, 0, 0), Some(0 -> 0L))
      step(j(8, 0), Some(0 -> 0L), Some(pc + 8))
      set(1, 0x100L); set(2, 0xdeadbeefL)
      step(s(-4, 2, 1), store = Some(0xfcL -> 0xdeadbeefL))
      c.io.imem.inst.poke(i(-4, 1, 2, 3, 0x03).U)
      c.io.dmem.addr.expect(0xfc.U)
      step(i(-4, 1, 2, 3, 0x03), Some(3 -> 0x87654321L), load = 0x87654321L)
      // Unsupported instructions must not write a register or memory.
      c.io.imem.inst.poke(0xffffffffL.U)
      c.io.prout.regw.expect(false.B); c.io.dmem.mem_rw.expect(false.B)
      step(0xffffffffL)
    }
  }
}
