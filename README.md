# Single-cycle Processor in Chisel

使用 Chisel 3 编写的教学用 32 位单周期处理器，实现 **RV32I 的 31 条指令子集**。工程包含 CPU 核心、独立的指令/数据存储器、调试端口、自包含测试和 Verilog 生成入口。

## 已实现的指令

| 类型 | 指令 |
| --- | --- |
| 寄存器运算 | ADD、SUB、SLL、SLT、SLTU、XOR、SRL、SRA、OR、AND |
| 立即数运算 | ADDI、SLTI、SLTIU、XORI、ORI、ANDI、SLLI、SRLI、SRAI |
| 条件分支 | BEQ、BNE、BLT、BGE、BLTU、BGEU |
| 跳转 | JAL、JALR |
| 高位立即数 | LUI、AUIPC |
| 访存 | LW、SW |

- 32 个 32 位通用寄存器，复位时清零；`x0` 始终为零。
- PC 复位值为 `0x00000000`，顺序执行时每周期加 4。
- I/S/B/J 立即数按符号扩展；移位量使用低 5 位。
- `JALR` 的目标为 `(rs1 + sign_extend(imm)) & ~1`，链接值为当前 PC + 4。
- `ECALL`（`0x00000073`）仅使 `Top.io.exit` 拉高，作为测试程序结束标记；它不实现异常处理，也不自动停止时钟。

这不是完整的 RV32I 实现：尚未支持 LB/LH/LBU/LHU、SB/SH、FENCE、EBREAK，以及 CSR、特权指令、中断、异常和压缩指令。解码表之外的指令不写寄存器或内存，PC 继续加 4；不会产生非法指令异常。`Instructions.scala` 中的常量声明不代表对应指令已实现。

## 环境与运行

工程固定使用 Scala 2.12.12、Chisel 3.4.2、chiseltest 0.3.2 和 sbt 1.4.7。CI 使用 JDK 11；修复后的测试也在 JDK 17 上验证通过。首次运行需要联网下载依赖。

```sh
git clone https://github.com/Kimberley-Inti/single-cycle-processor-in-chisel.git
cd single-cycle-processor-in-chisel
sbt clean test
```

测试使用 chiseltest 默认的 Treadle 后端，不要求安装 RISC-V 交叉编译器或 Verilator。

单独运行核心或顶层测试：

```sh
sbt "testOnly CoreTest"
sbt "testOnly TopTest"
```

## 内存与程序格式

`IMem` 与 `DMem` 各为 **1024 字节**，相互独立，均按小端序组合 32 位数据。有效字节地址为 `0x000–0x3ff`；完整的 32 位访问必须全部落在该范围内。可移植的字访问使用 4 字节对齐地址，程序跳转目标也应保持 4 字节对齐。硬件没有越界/未对齐异常检查，不应依赖这些访问的结果。

指令内存通过 `loadMemoryFromFile` 加载十六进制文本：**每行一个字节**，从地址 0 开始。例如 `addi x1, x0, 256`（`0x10000093`）写作：

```text
93
00
00
10
```

数据内存没有复位初始化，程序必须先写后读。地址 `0x2004` 超出本实现的存储器范围，不是映射的数据基址。

可运行示例为 [examples/integration.hex](examples/integration.hex)，对应汇编见 [examples/integration.S](examples/integration.S)。它执行访存、向后分支、JAL 和奇数目标 JALR，最后到达 PC=`0x34` 的 ECALL，并在数据地址 `0x100` 写入 `0xbeefdead`。顶层测试自行生成相同格式的测试程序，不依赖未提交的 `final.hex`。`src/hex/` 中保留的是历史样例。

如已有链接起始地址为 0 的 RV32 ELF，可在 Bash 环境下转换：

```sh
bash src/shell/tohex.sh program.elf program.hex
# 使用其他工具链时：
OBJCOPY=riscv32-unknown-elf-objcopy bash src/shell/tohex.sh program.elf program.hex
```

脚本默认调用 `riscv64-unknown-elf-objcopy`，将 ELF 转为原始字节并检查 1024 字节容量。应在编译/链接时使用本项目支持的指令，并注意此处是分离的指令与数据存储器：ELF 的数据段不会自动初始化 DMem。

## 生成 Verilog

```sh
sbt "runMain Core.Generate examples/integration.hex generated"
```

输出包括 `generated/Top.v`、FIRRTL 和注解文件；生成目录已加入忽略规则。省略最后一个参数时，输出目录同样为 `generated`。生成的 RTL 包含真实的 CPU 和顶层调试接口，仓库不再保留旧的空壳 `Top.v`。

生成器把程序文件路径写入内存加载注解。进行后续 RTL 仿真时，必须保证仿真器能访问该路径；是否能将初始化内存综合到具体 FPGA/ASIC，需要相应工具链支持。

## 顶层接口与调试

| 接口 | 作用 |
| --- | --- |
| `clock`、`reset` | 时钟与复位 |
| `io.exit` | 当前取到 ECALL 时为真 |
| `io.debug_index` / `io.debug_data` | 组合读取指定通用寄存器 |
| `io.pr_out` | 当前 PC、指令、操作数、写回数据及控制信号 |
| `io.dmem_en` | 为真时选择调试内存地址，并禁止内存写入 |
| `io.dmem_addr` / `io.dmem_data` | 调试读取 32 位内存数据 |

正常运行时应将 `io.dmem_en` 置为假，并驱动所有输入。调试内存读取是组合逻辑，无需额外时钟；应在停止推进时钟后读取。调试模式不会暂停 CPU，继续时钟会改变 PC/寄存器，而且加载指令会读取调试地址的数据。测试在观察到 `exit` 后停止推进时钟，再检查最终状态。

## 测试覆盖

`sbt clean test` 运行 7 个测试套件中的 8 个测试：

- `CoreTest`：全部 31 条已实现指令；使用边界值与固定种子随机输入，逐指令核对软件预期，包括有符号/无符号比较、移位量截取、分支方向、链接值、JALR 低位清零、访存地址和非法编码无写入。
- `TopTest`：有周期上限的完整程序执行、最终寄存器/内存状态，以及调试读不得误写内存的回归测试。
- `RegTest`、`IfuTest`、`ImmSelTest`、`BrCmpTest`、`MemTEST`：寄存器复位与 x0、PC 选择与回绕、立即数边界、六类分支比较、小端读写和写使能。

GitHub Actions 执行干净构建、全部测试、RTL 生成和 ELF 转换脚本检查。这些测试覆盖已实现功能，但不等同于形式化证明或完整 RISC-V 架构合规认证。旧的 `riscv_tests.sh` 依赖不存在的模板和绝对路径，已移除。

## 目录

```text
src/main/scala/
  Core/          核心连接、顶层和 RTL 生成入口
  Memory/        字节寻址的指令/数据存储器
  alu/           算术逻辑单元
  controller/    指令控制与分支比较
  ifu/           程序计数器与取指
  imm/           立即数生成
  rf/            通用寄存器堆
  exit/          ECALL 检测
  common/        控制常量与指令编码
  id/            独立的指令字段提取模块
src/test/scala/   自包含测试及程序编码辅助工具
examples/        示例汇编与逐字节程序文件
src/shell/       ELF 转换工具
```

## 许可证

参见 [LICENSE](LICENSE)（The Unlicense）。
