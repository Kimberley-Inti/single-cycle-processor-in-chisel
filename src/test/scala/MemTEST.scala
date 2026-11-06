import Memory.DMem
import chisel3._
import chiseltest._
import org.scalatest.FlatSpec

class MemTEST extends FlatSpec with ChiselScalatestTester {
  "DMem" should "store little-endian words and preserve data with writes disabled" in {
    test(new DMem) { c =>
      c.io.mem_rw.poke(true.B); c.io.addr.poke(16.U); c.io.data_in.poke("h44332211".U)
      c.clock.step()
      c.io.addr.poke(20.U); c.io.data_in.poke("h88776655".U); c.clock.step()
      c.io.mem_rw.poke(false.B); c.io.addr.poke(16.U); c.io.data_out.expect("h44332211".U)
      c.io.addr.poke(17.U); c.io.data_out.expect("h55443322".U)
      c.io.data_in.poke(0.U); c.clock.step()
      c.io.addr.poke(20.U); c.io.data_out.expect("h88776655".U)
      c.io.mem_rw.poke(true.B); c.io.addr.poke(1020.U); c.io.data_in.poke("hdeadbeef".U)
      c.clock.step(); c.io.mem_rw.poke(false.B); c.io.data_out.expect("hdeadbeef".U)
    }
  }
}
