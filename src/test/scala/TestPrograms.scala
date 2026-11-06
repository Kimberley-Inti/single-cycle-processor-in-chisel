import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

object TestPrograms {
  def i(imm: Int, rs1: Int, f3: Int, rd: Int, op: Int = 0x13): Long =
    ((imm.toLong & 0xfff) << 20) | (rs1.toLong << 15) | (f3.toLong << 12) | (rd.toLong << 7) | op
  def r(f7: Int, rs2: Int, rs1: Int, f3: Int, rd: Int): Long =
    (f7.toLong << 25) | (rs2.toLong << 20) | (rs1.toLong << 15) | (f3.toLong << 12) | (rd.toLong << 7) | 0x33
  def s(imm: Int, rs2: Int, rs1: Int): Long =
    (((imm.toLong >> 5) & 0x7f) << 25) | (rs2.toLong << 20) | (rs1.toLong << 15) |
      (2L << 12) | ((imm.toLong & 31) << 7) | 0x23
  def b(imm: Int, rs2: Int, rs1: Int, f3: Int): Long =
    (((imm.toLong >> 12) & 1) << 31) | (((imm.toLong >> 5) & 63) << 25) |
      (rs2.toLong << 20) | (rs1.toLong << 15) | (f3.toLong << 12) |
      (((imm.toLong >> 1) & 15) << 8) | (((imm.toLong >> 11) & 1) << 7) | 0x63
  def j(imm: Int, rd: Int): Long =
    (((imm.toLong >> 20) & 1) << 31) | (((imm.toLong >> 1) & 1023) << 21) |
      (((imm.toLong >> 11) & 1) << 20) | (((imm.toLong >> 12) & 255) << 12) | (rd.toLong << 7) | 0x6f
  def u(imm: Long, rd: Int, op: Int): Long = (imm & 0xfffff000L) | (rd.toLong << 7) | op

  // One byte per line; IMem assembles little-endian 32-bit instructions.
  def hex(name: String, words: Seq[Long]): String = {
    val directory = Paths.get("target", "test-programs")
    Files.createDirectories(directory)
    val path = directory.resolve(name + ".hex")
    val bytes = words.flatMap(w => (0 until 4).map(n => f"${(w >>> (n * 8)) & 255}%02x"))
    Files.write(path, (bytes.mkString("\n") + "\n").getBytes(StandardCharsets.US_ASCII))
    path.toAbsolutePath.toString.replace('\\', '/')
  }
}
