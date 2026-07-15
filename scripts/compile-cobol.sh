#!/usr/bin/env sh
# Compiles the GnuCOBOL billing engine into a native executable.
#
# Not wired into the Maven lifecycle on purpose — a machine without GnuCOBOL
# installed can still run `mvn test`/`mvn package`; CobolBillingCalculator
# falls back to the equivalent Java calculation when the binary is absent.
# Run this explicitly wherever the COBOL path should actually be exercised
# (CI, Docker build, or local dev after `brew install gnucobol`).
#
# Usage: ./scripts/compile-cobol.sh [output-dir]

set -eu

OUT_DIR="${1:-target/cobol}"
SCRIPT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

if ! command -v cobc >/dev/null 2>&1; then
    echo "cobc (GnuCOBOL) not found on PATH. Install it first:" >&2
    echo "  macOS:  brew install gnucobol" >&2
    echo "  Debian: sudo apt-get install -y gnucobol4" >&2
    exit 1
fi

mkdir -p "$OUT_DIR"
cobc -x -free -o "$OUT_DIR/billing_calc" "$SCRIPT_DIR/src/main/cobol/BillingCalc.cbl"
echo "Compiled COBOL billing engine -> $OUT_DIR/billing_calc"
