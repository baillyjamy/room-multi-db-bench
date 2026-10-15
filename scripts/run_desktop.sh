#!/usr/bin/env bash
# Runs the benchmark on the host machine: TARGET=jvm (desktop JVM, bundled SQLite) or
# TARGET=macos (Kotlin/Native macosArm64 executable). One fresh process per measurement.
set -euo pipefail
TARGET=${TARGET:?TARGET=jvm or TARGET=macos}
RUNS=${RUNS:-15}
SCENARIOS=${SCENARIOS:-"S1 S2 S5 S10 S5c"}
PREFIX=${PREFIX:-} # "large-" for the large-volume suite
OUT=${OUT:-results/$TARGET${PREFIX:+-large}.jsonl}
cd "$(dirname "$0")/.."
mkdir -p results && touch "$OUT"; [[ -n "${KEEP:-}" ]] || : > "$OUT"
case $TARGET in
  jvm) BIN=shared/build/install/shared-jvm/bin/shared; export JAVA_OPTS="--enable-native-access=ALL-UNNAMED" ;;
  macos) BIN=$(ls shared/build/bin/macosArm64/releaseExecutable/*.kexe) ;;
esac

launch() { "$BIN" "$1" "$2" | sed -n 's/^ROOMBENCH //p' | tee -a "$OUT"; }

for s in $SCENARIOS; do launch "${PREFIX}seed" "$s"; done
OUT=/dev/null launch "${PREFIX}run" "${SCENARIOS%% *}" >/dev/null # warm-up, discarded
for i in $(seq 1 "$RUNS"); do
  for s in $SCENARIOS; do launch "${PREFIX}run" "$s"; done
done
