# Single-Cycle Processor in Chisel

A teaching-oriented 32-bit single-cycle processor written in Chisel 3. It implements a **31-instruction subset of RV32I** and includes a CPU core, separate instruction and data memories, debug ports, self-contained tests, and a Verilog generation entry point.

## Supported instructions

| Category | Instructions |
| --- | --- |
| Register arithmetic and logic | ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND |
| Immediate arithmetic and logic | ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI |
| Conditional branches | BEQ, BNE, BLT, BGE, BLTU, BGEU |
| Jumps | JAL, JALR |
| Upper immediates | LUI, AUIPC |
| Memory access | LW, SW |

- The register file contains 32 registers of 32 bits each. Reset clears the registers; `x0` always reads as zero.
- The PC resets to `0x00000000` and advances by four bytes during sequential execution.
- I/S/B/J immediates are sign-extended. Shift operations use the low five bits of the shift amount.
- `JALR` computes its target as `(rs1 + sign_extend(imm)) & ~1` and writes the current PC plus four as the link value.
- `ECALL` (`0x00000073`) asserts `Top.io.exit` as a simulation completion marker. It does not implement trap handling or stop the clock automatically.

This is not a complete RV32I implementation. LB, LH, LBU, LHU, SB, SH, FENCE, EBREAK, CSR operations, privileged instructions, interrupts, exceptions, and compressed instructions are not implemented. Instructions outside the decoder table do not write registers or memory, and the PC advances by four; no illegal-instruction exception is raised. A constant declared in `Instructions.scala` does not imply that the corresponding instruction is implemented.

## Requirements and quick start

The project uses Scala 2.12.12, Chisel 3.4.2, chiseltest 0.3.2, and sbt 1.4.7. CI runs on JDK 11; the tests have also passed locally on JDK 17. The first build requires network access to download dependencies.

```sh
git clone https://github.com/Kimberley-Inti/single-cycle-processor-in-chisel.git
cd single-cycle-processor-in-chisel
sbt clean test
```

Tests use the default Treadle backend in chiseltest. Neither a RISC-V cross-compiler nor Verilator is required to run them.

Run a specific suite with:

```sh
sbt "testOnly CoreTest"
sbt "testOnly TopTest"
```

## Memory and program format

`IMem` and `DMem` are independent **1024-byte** memories. Both assemble 32-bit words in little-endian order. Valid byte addresses are `0x000` through `0x3ff`; every byte of a word access must remain within this range. Use four-byte-aligned addresses for portable word accesses and four-byte-aligned instruction targets. The hardware does not check for out-of-range or misaligned accesses; their results should not be relied upon.

Instruction memory is initialized with `loadMemoryFromFile`. Its hexadecimal text format contains **one byte per line**, starting at address zero. For example, `addi x1, x0, 256` (`0x10000093`) is represented as:

```text
93
00
00
10
```

Data memory is not initialized by reset. Programs must write data before reading it. Address `0x2004` is outside the supported memory range and is not a mapped data base address.

The runnable example is [examples/integration.hex](examples/integration.hex), with its assembly listing in [examples/integration.S](examples/integration.S). It exercises memory access, backward branches, JAL, and JALR with an odd intermediate target. It reaches ECALL at PC `0x34` and writes `0xbeefdead` to data address `0x100`. The top-level tests generate their own program data and verify the checked-in example; they do not depend on an untracked `final.hex`. Files under `src/hex/` are historical samples.

To convert an RV32 ELF linked at address zero, use Bash and a RISC-V objcopy tool:

```sh
bash src/shell/tohex.sh program.elf program.hex
# Select a different toolchain if needed:
OBJCOPY=riscv32-unknown-elf-objcopy bash src/shell/tohex.sh program.elf program.hex
```

The script defaults to `riscv64-unknown-elf-objcopy`, converts the ELF into raw bytes, and enforces the 1024-byte instruction-memory capacity. Compile and link programs using only supported instructions. Instruction and data memories are separate: an ELF data section does not automatically initialize DMem.

## Generate Verilog

```sh
sbt "runMain Core.Generate examples/integration.hex generated"
```

Outputs include `generated/Top.v`, FIRRTL, and annotation files. The output directory defaults to `generated` when the last argument is omitted and is excluded from version control. Generated RTL contains the CPU and its debug interface; obsolete generated files are not stored in the repository.

The generator records the program path in memory-loading annotations. A subsequent RTL simulation must be able to access that path. Synthesis of initialized memories depends on the target FPGA or ASIC toolchain.

## Top-level interface and debugging

| Interface | Purpose |
| --- | --- |
| `clock`, `reset` | Clock and reset |
| `io.exit` | Asserted while the current instruction is ECALL |
| `io.debug_index` / `io.debug_data` | Combinational read of a general-purpose register |
| `io.pr_out` | Current PC, instruction, operands, writeback data, and control signals |
| `io.dmem_en` | Selects the debug memory address and disables memory writes when asserted |
| `io.dmem_addr` / `io.dmem_data` | Debug address and 32-bit memory read data |

Drive all inputs and keep `io.dmem_en` low during normal execution. Debug memory reads are combinational and require no additional clock edge; inspect memory after stopping clock advancement. Debug mode does not pause the CPU. Advancing the clock can change the PC and registers, and a load instruction will read from the debug address. Tests stop advancing the clock when `exit` is observed, then inspect the final state.

## Test coverage

`sbt clean test` runs eight tests across seven suites:

- `CoreTest`: all 31 implemented instructions, using boundary values and reproducible random operands. Each instruction is checked against a software expectation, including signed and unsigned comparisons, shift amounts, branch decisions, link values, JALR target masking, memory addresses, and unsupported encodings without writes.
- `TopTest`: a bounded complete-program run with final register and memory assertions, plus a regression test ensuring debug reads cannot redirect stores.
- `RegTest`, `IfuTest`, `ImmSelTest`, `BrCmpTest`, and `MemTEST`: register reset and x0, PC selection and wraparound, immediate limits, all branch comparisons, little-endian memory access, and write enables.

GitHub Actions performs a clean build, runs all tests, generates RTL, checks the top-level interface, and validates the ELF conversion script. These checks cover implemented behavior but are not a formal proof or complete RISC-V architectural compliance certification. The obsolete `riscv_tests.sh` harness relied on missing templates and absolute paths and is not part of the current workflow.

## Project structure

```text
src/main/scala/
  Core/          Core integration, top-level module, and RTL generator
  Memory/        Byte-addressed instruction and data memories
  alu/           Arithmetic and logic unit
  controller/    Instruction control and branch comparison
  ifu/           Program counter and instruction fetch
  imm/           Immediate generation
  rf/            General-purpose register file
  exit/          ECALL detection
  common/        Control constants and instruction encodings
  id/            Standalone instruction-field extraction
src/test/scala/   Self-contained tests and program-encoding helpers
examples/        Assembly listing and byte-per-line program image
src/shell/       ELF conversion utility
```

## License

See [LICENSE](LICENSE) (The Unlicense).
