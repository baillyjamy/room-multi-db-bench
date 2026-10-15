#!/usr/bin/env bash
# Runs the benchmark on the host machine: TARGET=jvm (desktop JVM, bundled SQLite) or
# TARGET=macos (Kotlin/Native macosArm64 executable). One fresh process per measurement.
set -euo pipefail
TARGET=${TARGET:?TARGET=jvm or TARGET=macos}
RUNS=${RUNS:-15}
ENGINE=${ENGINE:-room} # room | sqldelight (S5c is Room-only)
PREFIX=${PREFIX:-} # "large-" (~120 MB) or "light-" (~12 MB) for the calendar suite
if [[ -n $PREFIX ]]; then DEFAULT_SCENARIOS="L1 L2"
elif [[ $ENGINE == room ]]; then DEFAULT_SCENARIOS="S1 S2 S5 S10 S5c"
else DEFAULT_SCENARIOS="S1 S2 S5 S10"; fi
SCENARIOS=${SCENARIOS:-$DEFAULT_SCENARIOS}
SUFFIX=$([[ $ENGINE == room ]] || echo "-$ENGINE")
OUT=${OUT:-results/$TARGET$SUFFIX${PREFIX:+-${PREFIX%-}}.jsonl}
cd "$(dirname "$0")/.."
mkdir -p results && touch "$OUT"; [[ -n "${KEEP:-}" ]] || : > "$OUT"
case $TARGET in
  jvm) BIN=shared/build/install/shared-jvm/bin/shared; export JAVA_OPTS="--enable-native-access=ALL-UNNAMED" ;;
  macos) BIN=$(ls shared/build/bin/macosArm64/releaseExecutable/*.kexe) ;;
esac

launch() { "$BIN" "$1" "$2" | sed -n 's/^ROOMBENCH //p' | tee -a "$OUT"; }

arg() { [[ $ENGINE == room ]] && echo "$1" || echo "$ENGINE:$1"; }
for s in $SCENARIOS; do launch "${PREFIX}seed" "$(arg "$s")"; done
OUT=/dev/null launch "${PREFIX}run" "$(arg "${SCENARIOS%% *}")" >/dev/null # warm-up, discarded
for i in $(seq 1 "$RUNS"); do
  for s in $SCENARIOS; do launch "${PREFIX}run" "$(arg "$s")"; done
done
