#!/usr/bin/env bash
# Runs the benchmark in the booted iOS simulator: the Kotlin/Native executable is spawned
# as a real iOS-simulator process (`simctl spawn`), one fresh process per measurement.
set -euo pipefail
RUNS=${RUNS:-15}
SCENARIOS=${SCENARIOS:-"S1 S2 S5 S10 S5c"}
DEVICE=${DEVICE:-booted}
PREFIX=${PREFIX:-} # "large-" for the large-volume suite
OUT=${OUT:-results/ios${PREFIX:+-large}.jsonl}
cd "$(dirname "$0")/.."
mkdir -p results && touch "$OUT"; [[ -n "${KEEP:-}" ]] || : > "$OUT"
BIN=$(ls shared/build/bin/iosSimulatorArm64/releaseExecutable/*.kexe)

launch() { xcrun simctl spawn "$DEVICE" "$BIN" "$1" "$2" | sed -n 's/^ROOMBENCH //p' | tee -a "$OUT"; }

for s in $SCENARIOS; do launch "${PREFIX}seed" "$s"; done
OUT=/dev/null launch "${PREFIX}run" "${SCENARIOS%% *}" >/dev/null # warm-up, discarded
for i in $(seq 1 "$RUNS"); do
  for s in $SCENARIOS; do launch "${PREFIX}run" "$s"; done
done
