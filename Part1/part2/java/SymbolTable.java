import java.util.*;

/**
 * SymbolTable.java
 * ----------------
 * Step 2 — Maintains the symbol → address mapping.
 *
 * Pre-populated with all Hack built-in symbols:
 *   R0–R15  (0–15)
 *   SP, LCL, ARG, THIS, THAT
 *   SCREEN  (16384)
 *   KBD     (24576)
 */
public class SymbolTable {

    private final Map<String, Integer> table;

    // ── Constructor ────────────────────────────────────────────────────────

    public SymbolTable() {
        table = new HashMap<>();

        // R0 – R15  →  RAM addresses 0 – 15
        for (int i = 0; i <= 15; i++) {
            table.put("R" + i, i);
        }

        // Virtual segment pointers
        table.put("SP",   0);
        table.put("LCL",  1);
        table.put("ARG",  2);
        table.put("THIS", 3);
        table.put("THAT", 4);

        // Memory-mapped I/O
        table.put("SCREEN", 16384);
        table.put("KBD",    24576);
    }

    // ── Mutators / Accessors ────────────────────────────────────────────────

    /** Adds {@code symbol} → {@code address} to the table. */
    public void addEntry(String symbol, int address) {
        table.put(symbol, address);
    }

    /** Returns true if {@code symbol} is already in the table. */
    public boolean contains(String symbol) {
        return table.containsKey(symbol);
    }

    /** Returns the address associated with {@code symbol}. */
    public int getAddress(String symbol) {
        return table.get(symbol);
    }
}
