"""
compare.py -- automated diff tool for .hack file verification
=============================================================

Usage
-----
  python compare.py <generated.hack> <expected.hack>

  Or to batch-compare everything in the tests/ directory:
  python compare.py tests/

Exit codes
----------
  0  -- all comparisons passed (files are identical)
  1  -- at least one mismatch was found
"""

import sys
import os

# Windows terminal safe encoding
if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")


def compare_hack_files(generated: str, expected: str) -> bool:
    """
    Compare *generated* against *expected* line by line.

    Returns True if identical, False (and prints diff) otherwise.
    """
    with open(generated, "r", encoding="utf-8") as f:
        gen_lines = [line.rstrip("\n") for line in f.readlines()]

    with open(expected, "r", encoding="utf-8") as f:
        exp_lines = [line.rstrip("\n") for line in f.readlines()]

    gen_name = os.path.basename(generated)
    exp_name = os.path.basename(expected)

    # Line count mismatch
    if len(gen_lines) != len(exp_lines):
        print(f"[FAIL] {gen_name}: line count differs -- "
              f"got {len(gen_lines)}, expected {len(exp_lines)}")
        return False

    # Line-by-line comparison
    mismatches = []
    for i, (g, e) in enumerate(zip(gen_lines, exp_lines), start=1):
        if g != e:
            mismatches.append((i, e, g))

    if mismatches:
        print(f"[FAIL] {gen_name} vs {exp_name} -- {len(mismatches)} mismatch(es):")
        for line_no, expected_val, got_val in mismatches:
            print(f"  Line {line_no:4d}: expected  {expected_val}")
            print(f"            got       {got_val}")
        return False

    print(f"[ OK] {gen_name} is identical to {exp_name}  ({len(gen_lines)} lines)")
    return True


def batch_compare(directory: str) -> int:
    """
    Find every *_expected.hack inside *directory* and compare it to the
    corresponding <stem>.hack produced by the assembler.

    Returns the number of failures.
    """
    failures = 0
    found    = 0

    for fname in sorted(os.listdir(directory)):
        if not fname.endswith("_expected.hack"):
            continue

        stem      = fname.replace("_expected.hack", "")
        expected  = os.path.join(directory, fname)
        generated = os.path.join(directory, stem + ".hack")

        if not os.path.isfile(generated):
            print(f"[SKIP] {stem}.hack not found -- run assembler first.")
            continue

        found += 1
        if not compare_hack_files(generated, expected):
            failures += 1

    if found == 0:
        print("[WARN] No *_expected.hack files found to compare.")

    return failures


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    if len(sys.argv) == 2 and os.path.isdir(sys.argv[1]):
        failures = batch_compare(sys.argv[1])
        if failures:
            print(f"\n[RESULT] {failures} test(s) FAILED.")
            sys.exit(1)
        else:
            print("\n[RESULT] All comparisons PASSED. [OK]")
            sys.exit(0)

    elif len(sys.argv) == 3:
        generated = sys.argv[1]
        expected  = sys.argv[2]
        ok = compare_hack_files(generated, expected)
        sys.exit(0 if ok else 1)

    else:
        print("Usage:")
        print("  python compare.py <generated.hack> <expected.hack>")
        print("  python compare.py tests/           # batch mode")
        sys.exit(1)
