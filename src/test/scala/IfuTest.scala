import chisel3._
import chiseltest._
import org.scalatest.FlatSpec
import ifu.IFU

class IfuTest extends FlatSpec with ChiselScalatestTester {
  "IFU" should "reset, increment, select the supplied target and wrap at 32 bits" in {
    test(new IFU) { c =>
      c.io.data_in.poke("h00008067".U); c.io.pc_sel.poke(false.B); c.io.npc.poke(64.U)
      c.reset.poke(true.B); c.clock.step(); c.reset.poke(false.B)
      c.io.addr.expect(0.U); c.io.inst.expect("h00008067".U)
      c.clock.step(); c.io.addr.expect(4.U)
      c.io.pc_sel.poke(true.B); c.clock.step(); c.io.addr.expect(64.U)
      c.io.npc.poke("hfffffffc".U); c.clock.step(); c.io.addr.expect("hfffffffc".U)
      c.io.pc_sel.poke(false.B); c.clock.step(); c.io.addr.expect(0.U)
    }
  }
}
