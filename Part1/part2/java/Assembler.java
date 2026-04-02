import java.io.*;
import java.nio.file.*;

/**
 * Assembler.java
 * --------------
 * Main entry point — orchestrates the full 5-step Hack assembly pipeline.
 *
 * Usage
 * -----
 *   javac *.java
 *   java Assembler <file.asm>            Single file
 *   java Assembler <dir/>                Assemble all .asm files in directory
 *   java Assembler f1.asm f2.asm ...     Multiple files
 *
 * Pipeline
 * --------
 *   Step 1  Parser     : clean the source, strip comments & whitespace
 *   Step 2  SymbolTable: pre-populate built-in symbols
 *   Step 3  Pass 1     : scan labels (L-instructions) -> ROM addresses
 *   Step 4  Pass 2     : translate every A / C instruction -> 16-bit binary
 *   Step 5  Output     : write binary strings to .hack file
 */
public class Assembler {

    // ── Step 3: Pass 1 — Label Resolution ─────────────────────────────────

    /**
     * Scans the instruction list for (LABEL) pseudo-instructions and records
     * each label -> ROM address mapping in {@code symbolTable}.
     * L-instructions do NOT advance the ROM counter.
     */
    private static void pass1(Parser parser, SymbolTable symbolTable) {
        int romAddress = 0;

        for (String line : parser.getInstructions()) {

            if (line.startsWith("(") && line.endsWith(")")) {
                // L-instruction: extract label and record current ROM address
                String label = line.substring(1, line.length() - 1);
                symbolTable.addEntry(label, romAddress);
            } else {
                // A- or C-instruction: occupies one ROM word
                romAddress++;
            }
        }
    }

    // ── Step 4: Pass 2 — Translation ──────────────────────────────────────

    /**
     * Iterates through all A- and C-instructions, resolves symbols, and
     * returns a list of 16-character binary strings.
     */
    private static StringBuilder pass2(Parser parser, SymbolTable symbolTable) {
        int nextRamAddress = 16;      // user variables start at RAM[16]
        StringBuilder output = new StringBuilder();

        // Re-iterate from the start
        parser.reset();

        while (parser.hasMoreInstructions()) {
            parser.advance();

            switch (parser.instructionType()) {

                // ── L-instructions: skip (already handled in Pass 1) ───────
                case L_INSTRUCTION:
                    break;

                // ── A-instruction: @value ───────────────────────────────────
                case A_INSTRUCTION: {
                    String symbol = parser.symbol();
                    int address;

                    if (symbol.matches("\\d+")) {
                        // Numeric literal
                        address = Integer.parseInt(symbol);
                    } else if (symbolTable.contains(symbol)) {
                        // Known symbol (predefined or label from Pass 1)
                        address = symbolTable.getAddress(symbol);
                    } else {
                        // New user-defined variable
                        symbolTable.addEntry(symbol, nextRamAddress);
                        address = nextRamAddress;
                        nextRamAddress++;
                    }

                    // 0 + 15-bit binary representation
                    String binary = "0" + to15Bit(address);
                    output.append(binary).append("\n");
                    break;
                }

                // ── C-instruction: dest=comp;jump ───────────────────────────
                case C_INSTRUCTION: {
                    String compBits = Code.comp(parser.comp());
                    String destBits = Code.dest(parser.dest());
                    String jumpBits = Code.jump(parser.jump());

                    // C-instruction format: 1 1 1 compBits destBits jumpBits
                    String binary = "111" + compBits + destBits + jumpBits;
                    output.append(binary).append("\n");
                    break;
                }
            }
        }

        return output;
    }

    // ── Step 5: Write .hack output ─────────────────────────────────────────

    private static void writeHack(String content, String hackPath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(hackPath), "UTF-8"))) {
            writer.write(content);
        }

        // Count lines (= instructions written)
        long lineCount = content.chars().filter(c -> c == '\n').count();
        System.out.println("[OK] Written " + lineCount + " instructions -> " + hackPath);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Converts {@code value} to a zero-padded 15-bit binary string.
     */
    private static String to15Bit(int value) {
        return String.format("%15s", Integer.toBinaryString(value)).replace(' ', '0');
    }

    /**
     * Derives the output .hack path from the .asm input path.
     * e.g. tests/Add.asm  ->  tests/Add.hack
     */
    private static String hackPath(String asmPath) {
        if (asmPath.endsWith(".asm")) {
            return asmPath.substring(0, asmPath.length() - 4) + ".hack";
        }
        return asmPath + ".hack";
    }

    // ── Orchestrator ────────────────────────────────────────────────────────

    /**
     * Full pipeline for a single .asm file.
     */
    private static void assemble(String asmPath) throws IOException {
        System.out.println("[>>] Assembling: " + asmPath);

        // Step 1 — Parse & clean
        Parser parser = new Parser(asmPath);

        // Step 2 — Build symbol table
        SymbolTable symbolTable = new SymbolTable();

        // Step 3 — Pass 1: label resolution
        pass1(parser, symbolTable);

        // Step 4 — Pass 2: full translation
        StringBuilder binaryOutput = pass2(parser, symbolTable);

        // Step 5 — Write to .hack
        writeHack(binaryOutput.toString(), hackPath(asmPath));
    }

    // ── Main ────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("Usage: java Assembler <file.asm> [file2.asm ...]");
            System.out.println("       java Assembler <directory/>");
            System.exit(1);
        }

        // Collect .asm targets
        java.util.List<String> asmFiles = new java.util.ArrayList<>();

        for (String arg : args) {
            File target = new File(arg);

            if (target.isDirectory()) {
                // Assemble every .asm file in the directory
                File[] files = target.listFiles(
                        (dir, name) -> name.toLowerCase().endsWith(".asm"));
                if (files != null) {
                    for (File f : files) {
                        asmFiles.add(f.getPath());
                    }
                }
            } else if (arg.endsWith(".asm")) {
                asmFiles.add(arg);
            } else {
                System.out.println("[SKIP] Not an .asm file or directory: " + arg);
            }
        }

        if (asmFiles.isEmpty()) {
            System.out.println("[ERROR] No .asm files found.");
            System.exit(1);
        }

        int errors = 0;
        for (String asm : asmFiles) {
            try {
                assemble(asm);
            } catch (Exception e) {
                System.out.println("[ERROR] " + asm + ": " + e.getMessage());
                errors++;
            }
        }

        if (errors > 0) {
            System.exit(1);
        }
    }
}
