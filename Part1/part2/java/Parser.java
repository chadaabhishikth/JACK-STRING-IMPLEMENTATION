import java.io.*;
import java.util.*;

/**
 * Parser.java
 * -----------
 * Step 1 — Reads a .asm file, strips comments and whitespace, and
 * discards empty lines.  Also classifies each instruction into one of:
 *   A_INSTRUCTION   @value
 *   C_INSTRUCTION   dest=comp;jump
 *   L_INSTRUCTION   (LABEL)
 */
public class Parser {

    public enum InstructionType {
        A_INSTRUCTION,   // @value
        C_INSTRUCTION,   // dest=comp;jump
        L_INSTRUCTION    // (SYMBOL)
    }

    // ── State ──────────────────────────────────────────────────────────────
    private final List<String> instructions;  // clean instruction list
    private int currentIndex = -1;
    private String currentInstruction = "";

    // ── Constructor ────────────────────────────────────────────────────────

    /**
     * Opens {@code filePath} and builds the cleaned instruction list.
     */
    public Parser(String filePath) throws IOException {
        instructions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) {

            String rawLine;
            while ((rawLine = reader.readLine()) != null) {

                // Remove inline comments
                int commentIdx = rawLine.indexOf("//");
                if (commentIdx >= 0) {
                    rawLine = rawLine.substring(0, commentIdx);
                }

                // Strip all whitespace (spaces, tabs)
                String clean = rawLine.replaceAll("\\s+", "");

                // Discard empty lines
                if (!clean.isEmpty()) {
                    instructions.add(clean);
                }
            }
        }
    }

    // ── Navigation ─────────────────────────────────────────────────────────

    /** Returns true if there are more instructions to process. */
    public boolean hasMoreInstructions() {
        return currentIndex < instructions.size() - 1;
    }

    /** Advances to the next instruction and updates internal state. */
    public void advance() {
        currentInstruction = instructions.get(++currentIndex);
    }

    /** Resets the parser so Pass 2 can re-iterate from the beginning. */
    public void reset() {
        currentIndex = -1;
        currentInstruction = "";
    }

    // ── Classification ─────────────────────────────────────────────────────

    /** Returns the type of the current instruction. */
    public InstructionType instructionType() {
        if (currentInstruction.startsWith("@")) {
            return InstructionType.A_INSTRUCTION;
        } else if (currentInstruction.startsWith("(") && currentInstruction.endsWith(")")) {
            return InstructionType.L_INSTRUCTION;
        } else {
            return InstructionType.C_INSTRUCTION;
        }
    }

    // ── Field accessors ────────────────────────────────────────────────────

    /**
     * Returns the symbol or decimal of the current A- or L-instruction.
     * Precondition: instructionType() == A_INSTRUCTION or L_INSTRUCTION.
     */
    public String symbol() {
        if (instructionType() == InstructionType.A_INSTRUCTION) {
            return currentInstruction.substring(1);          // strip '@'
        } else {
            // L_INSTRUCTION: strip '(' and ')'
            return currentInstruction.substring(1, currentInstruction.length() - 1);
        }
    }

    /**
     * Returns the dest mnemonic of the current C-instruction.
     * Empty string if dest is absent (e.g. "0;JMP").
     */
    public String dest() {
        if (currentInstruction.contains("=")) {
            return currentInstruction.split("=")[0];
        }
        return "";
    }

    /**
     * Returns the comp mnemonic of the current C-instruction.
     */
    public String comp() {
        String tmp = currentInstruction;
        if (tmp.contains("=")) {
            tmp = tmp.split("=", 2)[1];   // discard dest
        }
        if (tmp.contains(";")) {
            tmp = tmp.split(";", 2)[0];   // discard jump
        }
        return tmp;
    }

    /**
     * Returns the jump mnemonic of the current C-instruction.
     * Empty string if jump is absent (e.g. "D=M").
     */
    public String jump() {
        if (currentInstruction.contains(";")) {
            return currentInstruction.split(";", 2)[1];
        }
        return "";
    }

    /** Exposes the raw cleaned list (needed by Pass 1). */
    public List<String> getInstructions() {
        return Collections.unmodifiableList(instructions);
    }
}
