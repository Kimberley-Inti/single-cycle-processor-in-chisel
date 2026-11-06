import Core.Top
import chisel3._
import chiseltest._
import org.scalatest.FlatSpec
import TestPrograms._

class TopTest extends FlatSpec with ChiselScalatestTester {
  "Top" should "execute backward branches, memory, JAL and odd-target JALR" in {
    val program = Seq(
      i(256, 0, 0, 1),           // 0: data base
      u(0xbeefe000L, 2, 0x37),   // 4: LUI
      i(-339, 2, 0, 2),          // 8: x2 = 0xbeefdead
      s(0, 2, 1),               // 12: SW
      i(0, 1, 2, 3, 0x03),      // 16: LW
      i(3, 0, 0, 4),            // 20: loop counter
      i(-1, 4, 0, 4),           // 24
      b(-4, 0, 4, 1),           // 28: BNE back to 24
      j(8, 5),                  // 32: JAL to 40, link 36
      i(99, 0, 0, 6),           // 36: skipped
      i(53, 0, 0, 7),           // 40
      i(-4, 7, 0, 8, 0x67),     // 44: JALR (53 - 4) & ~1 = 48
      u(0, 9, 0x17),            // 48: AUIPC = 48
      0x73L)                    // 52: ECALL/exit
    val generated = hex("integration", program)
    assert(java.nio.file.Files.readAllLines(java.nio.file.Paths.get(generated)) ==
      java.nio.file.Files.readAllLines(java.nio.file.Paths.get("examples/integration.hex")),
      "checked-in example must match the instruction listing")
    test(new Top("examples/integration.hex")) { c =>
      c.io.debug_index.poke(0.U); c.io.dmem_en.poke(false.B); c.io.dmem_addr.poke(0.U)
      var cycles = 0
      while (!c.io.exit.peek().litToBoolean && cycles < 40) {
        c.clock.step(); cycles += 1
      }
      assert(cycles < 40, "program did not reach ECALL within 40 cycles")
      c.io.exit.expect(true.B); c.io.pr_out.pc.expect(52.U)
      val expected = Seq(0 -> 0L, 1 -> 256L, 2 -> 0xbeefdeadL, 3 -> 0xbeefdeadL,
        4 -> 0L, 5 -> 36L, 6 -> 0L, 7 -> 53L, 8 -> 48L, 9 -> 48L)
      for ((reg, value) <- expected) {
        c.io.debug_index.poke(reg.U); c.io.debug_data.expect(value.U)
      }
      c.io.dmem_en.poke(true.B); c.io.dmem_addr.poke(256.U)
      c.io.dmem_data.expect("hbeefdead".U)
    }
  }

  it should "prevent debug reads from redirecting stores" in {
    val program = Seq(i(256, 0, 0, 1), i(42, 0, 0, 2), s(0, 2, 1),
      i(99, 0, 0, 2), s(0, 2, 1), 0x73L)
    test(new Top(hex("debug-store", program))) { c =>
      c.io.debug_index.poke(0.U); c.io.dmem_en.poke(false.B); c.io.dmem_addr.poke(256.U)
      c.clock.step(4); c.io.pr_out.pc.expect(16.U)
      c.io.dmem_en.poke(true.B); c.io.dmem_data.expect(42.U)
      c.clock.step(); c.io.dmem_data.expect(42.U); c.io.exit.expect(true.B)
    }
  }
}
