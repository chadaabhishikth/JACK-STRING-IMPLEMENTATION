import java.util.*;

/**
 * Code.java
 * ---------
 * Step 4 (C-instruction part) — translates dest / comp / jump mnemonics
 * into their exact 3- and 7-bit binary counterparts per the Hack spec.
 *
 * All methods are static; no instantiation needed.
 */
public class Code {

    // ── Comp table (7 bits, includes the 'a' bit) ──────────────────────────
    private static final Map<String, String> COMP_TABLE = new LinkedHashMap<>();

    // ── Dest table (3 bits) ────────────────────────────────────────────────
    private static final Map<String, String> DEST_TABLE = new LinkedHashMap<>();

    // ── Jump table (3 bits) ────────────────────────────────────────────────
    private static final Map<String, String> JUMP_TABLE = new LinkedHashMap<>();

    static {
        // ── comp ────────────────────────────────────────────────────────────
        // a = 0 (A register)
        COMP_TABLE.put("0",   "0101010");
        COMP_TABLE.put("1",   "0111111");
        COMP_TABLE.put("-1",  "0111010");
        COMP_TABLE.put("D",   "0001100");
        COMP_TABLE.put("A",   "0110000");
        COMP_TABLE.put("!D",  "0001101");
        COMP_TABLE.put("!A",  "0110001");
        COMP_TABLE.put("-D",  "0001111");
        COMP_TABLE.put("-A",  "0110011");
        COMP_TABLE.put("D+1", "0011111");
        COMP_TABLE.put("A+1", "0110111");
        COMP_TABLE.put("D-1", "0001110");
        COMP_TABLE.put("A-1", "0110010");
        COMP_TABLE.put("D+A", "0000010");
        COMP_TABLE.put("D-A", "0010011");
        COMP_TABLE.put("A-D", "0000111");
        COMP_TABLE.put("D&A", "0000000");
        COMP_TABLE.put("D|A", "0010101");
        // a = 1 (M register)
        COMP_TABLE.put("M",   "1110000");
        COMP_TABLE.put("!M",  "1110001");
        COMP_TABLE.put("-M",  "1110011");
        COMP_TABLE.put("M+1", "1110111");
        COMP_TABLE.put("M-1", "1110010");
        COMP_TABLE.put("D+M", "1000010");
        COMP_TABLE.put("D-M", "1010011");
        COMP_TABLE.put("M-D", "1000111");
        COMP_TABLE.put("D&M", "1000000");
        COMP_TABLE.put("D|M", "1010101");

        // ── dest ────────────────────────────────────────────────────────────
        DEST_TABLE.put("",    "000");
        DEST_TABLE.put("M",   "001");
        DEST_TABLE.put("D",   "010");
        DEST_TABLE.put("MD",  "011");
        DEST_TABLE.put("DM",  "011");   // alias
        DEST_TABLE.put("A",   "100");
        DEST_TABLE.put("AM",  "101");
        DEST_TABLE.put("MA",  "101");   // alias
        DEST_TABLE.put("AD",  "110");
        DEST_TABLE.put("DA",  "110");   // alias
        DEST_TABLE.put("AMD", "111");
        DEST_TABLE.put("ADM", "111");   // aliases
        DEST_TABLE.put("DAM", "111");
        DEST_TABLE.put("DMA", "111");
        DEST_TABLE.put("MAD", "111");
        DEST_TABLE.put("MDA", "111");

        // ── jump ────────────────────────────────────────────────────────────
        JUMP_TABLE.put("",    "000");
        JUMP_TABLE.put("JGT", "001");
        JUMP_TABLE.put("JEQ", "010");
        JUMP_TABLE.put("JGE", "011");
        JUMP_TABLE.put("JLT", "100");
        JUMP_TABLE.put("JNE", "101");
        JUMP_TABLE.put("JLE", "110");
        JUMP_TABLE.put("JMP", "111");
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Returns the 7-bit binary string for {@code mnemonic}.
     * Throws {@link IllegalArgumentException} if unknown.
     */
    public static String comp(String mnemonic) {
        String bits = COMP_TABLE.get(mnemonic);
        if (bits == null) {
            throw new IllegalArgumentException("Unknown comp mnemonic: '" + mnemonic + "'");
        }
        return bits;
    }

    /**
     * Returns the 3-bit binary string for the dest {@code mnemonic}.
     */
    public static String dest(String mnemonic) {
        String bits = DEST_TABLE.get(mnemonic);
        if (bits == null) {
            throw new IllegalArgumentException("Unknown dest mnemonic: '" + mnemonic + "'");
        }
        return bits;
    }

    /**
     * Returns the 3-bit binary string for the jump {@code mnemonic}.
     */
    public static String jump(String mnemonic) {
        String bits = JUMP_TABLE.get(mnemonic);
        if (bits == null) {
            throw new IllegalArgumentException("Unknown jump mnemonic: '" + mnemonic + "'");
        }
        return bits;
    }

    // Private constructor — utility class, not meant to be instantiated
    private Code() {}
}
