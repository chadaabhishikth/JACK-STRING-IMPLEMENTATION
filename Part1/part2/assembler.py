"""
Hack Assembler -- Nand2Tetris Project
=====================================
Translates Hack assembly (.asm) -> binary machine code (.hack).

Pipeline
--------
  Step 1  : Parse & clean the input file.
  Step 2  : Build the pre-defined symbol table.
  Step 3  : Pass 1  -- resolve (LABEL) pseudo-instructions to ROM addresses.
  Step 4  : Pass 2  -- translate A- and C-instructions to 16-bit binary.
  Step 5  : Write the resulting binary strings to a .hack output file.
"""

import sys
import os

# Re-configure stdout/stderr to UTF-8 so on Windows we can safely print
# any Unicode characters (e.g. arrows in status messages) without a
# UnicodeEncodeError from the default 'charmap' codec.
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")


# ---------------------------------------------------------------------------
# Step 2 — Pre-defined symbol table
# ---------------------------------------------------------------------------

def build_symbol_table() -> dict:
    """Return a dictionary pre-populated with all Hack built-in symbols."""
    table = {}

    # R0 – R15  →  RAM addresses 0 – 15
    for i in range(16):
        table[f"R{i}"] = i

    # Virtual segment pointers
    table["SP"]     = 0
    table["LCL"]    = 1
    table["ARG"]    = 2
    table["THIS"]   = 3
    table["THAT"]   = 4

    # Memory-mapped I/O
    table["SCREEN"] = 16384
    table["KBD"]    = 24576

    return table


# ---------------------------------------------------------------------------
# Step 1 — Parser / Cleaner
# ---------------------------------------------------------------------------

def parse(filepath: str) -> list[str]:
    """
    Open *filepath*, read it line by line, and return a cleaned list of
    instruction strings.

    Cleaning rules
    --------------
    * Strip all leading / trailing whitespace.
    * Remove everything from the first ``//`` onwards (inline comments).
    * Discard lines that are empty after the above processing.
    """
    instructions = []

    with open(filepath, "r", encoding="utf-8", errors="replace") as fh:
        for raw_line in fh:
            # Remove inline comments and surrounding whitespace
            line = raw_line.split("//")[0].strip()

            # Internal whitespace removal (e.g. spaces inside a mnemonic)
            line = "".join(line.split())

            if line:                       # discard empty lines
                instructions.append(line)

    return instructions


# ---------------------------------------------------------------------------
# Step 3 — Pass 1: label (L-instruction) resolution
# ---------------------------------------------------------------------------

def pass1(instructions: list[str], symbol_table: dict) -> None:
    """
    Iterate *instructions* and record every (LABEL) pseudo-instruction in
    *symbol_table* mapping the label name to the ROM address of the *next*
    real instruction.

    L-instructions themselves do NOT advance the ROM counter.
    """
    rom_address = 0

    for line in instructions:
        if line.startswith("(") and line.endswith(")"):
            # L-instruction: extract the label name
            label = line[1:-1]
            symbol_table[label] = rom_address
        else:
            # A-instruction or C-instruction — occupies one ROM word
            rom_address += 1


# ---------------------------------------------------------------------------
# C-instruction lookup tables
# ---------------------------------------------------------------------------

COMP_TABLE = {
    # a=0 (A register used)
    "0":   "0101010",
    "1":   "0111111",
    "-1":  "0111010",
    "D":   "0001100",
    "A":   "0110000",
    "!D":  "0001101",
    "!A":  "0110001",
    "-D":  "0001111",
    "-A":  "0110011",
    "D+1": "0011111",
    "A+1": "0110111",
    "D-1": "0001110",
    "A-1": "0110010",
    "D+A": "0000010",
    "D-A": "0010011",
    "A-D": "0000111",
    "D&A": "0000000",
    "D|A": "0010101",
    # a=1 (M register used — same ALU op, different address bit)
    "M":   "1110000",
    "!M":  "1110001",
    "-M":  "1110011",
    "M+1": "1110111",
    "M-1": "1110010",
    "D+M": "1000010",
    "D-M": "1010011",
    "M-D": "1000111",
    "D&M": "1000000",
    "D|M": "1010101",
}

DEST_TABLE = {
    "":    "000",   # no destination
    "M":   "001",
    "D":   "010",
    "MD":  "011",
    "DM":  "011",   # alias
    "A":   "100",
    "AM":  "101",
    "MA":  "101",   # alias
    "AD":  "110",
    "DA":  "110",   # alias
    "AMD": "111",
    "ADM": "111",   # alias
    "DAM": "111",   # alias
    "DMA": "111",   # alias
    "MAD": "111",   # alias
    "MDA": "111",   # alias
}

JUMP_TABLE = {
    "":    "000",   # no jump
    "JGT": "001",
    "JEQ": "010",
    "JGE": "011",
    "JLT": "100",
    "JNE": "101",
    "JLE": "110",
    "JMP": "111",
}


# ---------------------------------------------------------------------------
# C-instruction parser helper
# ---------------------------------------------------------------------------

def parse_c_instruction(instruction: str) -> tuple[str, str, str]:
    """
    Split a C-instruction string into (dest, comp, jump) parts.

    Formats handled
    ---------------
      dest=comp;jump
      dest=comp        (no jump)
      comp;jump        (no dest)
      comp             (no dest, no jump — rare but valid)
    """
    dest = ""
    jump = ""
    comp = instruction

    # Separate dest (everything before '=')
    if "=" in comp:
        dest, comp = comp.split("=", 1)

    # Separate jump (everything after ';')
    if ";" in comp:
        comp, jump = comp.split(";", 1)

    return dest, comp, jump


# ---------------------------------------------------------------------------
# Step 4 — Pass 2: full translation
# ---------------------------------------------------------------------------

def pass2(instructions: list[str], symbol_table: dict) -> list[str]:
    """
    Translate every A- and C-instruction to a 16-bit binary string.

    *symbol_table* is updated in-place as new user variables are discovered.
    Returns a list of 16-character '0'/'1' strings.
    """
    binary_lines   = []
    next_ram_address = 16          # user variables start at RAM[16]

    for line in instructions:

        # ── Skip L-instructions (already handled in Pass 1) ────────────────
        if line.startswith("(") and line.endswith(")"):
            continue

        # ── A-Instruction: @value ──────────────────────────────────────────
        if line.startswith("@"):
            value = line[1:]

            if value.isdigit():
                # Numeric literal — convert directly
                address = int(value)
            elif value in symbol_table:
                # Known symbol (pre-defined or label)
                address = symbol_table[value]
            else:
                # New user-defined variable — allocate next free RAM slot
                symbol_table[value] = next_ram_address
                address = next_ram_address
                next_ram_address += 1

            # 0 + 15-bit binary representation
            binary = "0" + format(address, "015b")
            binary_lines.append(binary)

        # ── C-Instruction: dest=comp;jump ──────────────────────────────────
        else:
            dest, comp, jump = parse_c_instruction(line)

            # Look up each field; raise a clear error if unknown
            if comp not in COMP_TABLE:
                raise ValueError(
                    f"Unknown comp mnemonic: '{comp}' in instruction '{line}'"
                )
            if dest not in DEST_TABLE:
                raise ValueError(
                    f"Unknown dest mnemonic: '{dest}' in instruction '{line}'"
                )
            if jump not in JUMP_TABLE:
                raise ValueError(
                    f"Unknown jump mnemonic: '{jump}' in instruction '{line}'"
                )

            comp_bits = COMP_TABLE[comp]
            dest_bits = DEST_TABLE[dest]
            jump_bits = JUMP_TABLE[jump]

            # C-instruction format: 1 1 1 | comp(7) | dest(3) | jump(3)
            binary = "111" + comp_bits + dest_bits + jump_bits
            binary_lines.append(binary)

    return binary_lines


# ---------------------------------------------------------------------------
# Step 5 — Write .hack output file
# ---------------------------------------------------------------------------

def write_hack(binary_lines: list[str], output_path: str) -> None:
    """Write each 16-bit binary string to *output_path*, one per line."""
    with open(output_path, "w", encoding="utf-8") as fh:
        for line in binary_lines:
            fh.write(line + "\n")

    print(f"[OK] Written {len(binary_lines)} instructions → {output_path}")


# ---------------------------------------------------------------------------
# Orchestrator
# ---------------------------------------------------------------------------

def assemble(asm_path: str) -> str:
    """
    Full assembly pipeline for *asm_path*.

    Returns the path of the generated .hack file.
    """
    if not os.path.isfile(asm_path):
        raise FileNotFoundError(f"Input file not found: {asm_path}")

    # Derive output path:  foo/bar/Add.asm → foo/bar/Add.hack
    base, _   = os.path.splitext(asm_path)
    hack_path = base + ".hack"

    print(f"[>>] Assembling: {asm_path}")

    # Step 1 — clean the source
    instructions = parse(asm_path)

    # Step 2 — build pre-defined symbol table
    symbol_table = build_symbol_table()

    # Step 3 — Pass 1: resolve labels
    pass1(instructions, symbol_table)

    # Step 4 — Pass 2: translate instructions
    binary_lines = pass2(instructions, symbol_table)

    # Step 5 — write output
    write_hack(binary_lines, hack_path)

    return hack_path


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python assembler.py <file.asm> [file2.asm ...]")
        print("       python assembler.py tests/          # assemble all .asm in dir")
        sys.exit(1)

    targets = sys.argv[1:]
    asm_files = []

    for target in targets:
        if os.path.isdir(target):
            # Collect every .asm file inside the directory
            for fname in os.listdir(target):
                if fname.endswith(".asm"):
                    asm_files.append(os.path.join(target, fname))
        elif target.endswith(".asm"):
            asm_files.append(target)
        else:
            print(f"[SKIP] Not an .asm file or directory: {target}")

    if not asm_files:
        print("[ERROR] No .asm files found.")
        sys.exit(1)

    errors = 0
    for asm in asm_files:
        try:
            assemble(asm)
        except Exception as exc:
            print(f"[ERROR] {asm}: {exc}")
            errors += 1

    if errors:
        sys.exit(1)
