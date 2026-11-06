# Reference listing for integration.hex (assemble as RV32I without compression).
# Link the text section at address 0. Data memory is separate from code memory.
.option norvc
.section .text
.globl _start
_start:
  addi x1, x0, 256
  lui  x2, 0xbeefe
  addi x2, x2, -339
  sw   x2, 0(x1)
  lw   x3, 0(x1)
  addi x4, x0, 3
loop:
  addi x4, x4, -1
  bne  x4, x0, loop
  jal  x5, after_skip
  addi x6, x0, 99
after_skip:
  addi x7, x0, 53
  jalr x8, -4(x7)  # (53 - 4) & ~1 = 48
  auipc x9, 0
  ecall
