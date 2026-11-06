#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 2 ]]; then
  echo "Usage: $0 <RV32 ELF> <output.hex>" >&2
  exit 2
fi
binary=$(mktemp)
trap 'rm -f "$binary"' EXIT
"${OBJCOPY:-riscv64-unknown-elf-objcopy}" -O binary "$1" "$binary"
if (( $(wc -c < "$binary") > 1024 )); then
  echo "Program exceeds the 1024-byte instruction memory" >&2
  exit 1
fi
od -An -v -tx1 "$binary" | tr -s '[:space:]' '\n' | sed '/^$/d' > "$2"
