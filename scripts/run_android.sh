#!/usr/bin/env bash
# Runs the benchmark on the connected Android device/emulator.
# Every measurement is a fresh process (`am start -S`), scenarios are interleaved round-robin.
set -euo pipefail
RUNS=${RUNS:-15}
SCENARIOS=${SCENARIOS:-"S1 S2 S5 S10 S5c"}
ADB=${ADB:-$ANDROID_HOME/platform-tools/adb}
PREFIX=${PREFIX:-} # "large-" for the large-volume suite
OUT=${OUT:-results/android${PREFIX:+-large}.jsonl}
cd "$(dirname "$0")/.."
mkdir -p results && touch "$OUT"; [[ -n "${KEEP:-}" ]] || : > "$OUT"

$ADB install -r androidApp/build/outputs/apk/release/androidApp-release.apk >/dev/null

launch() { # mode scenario
  $ADB logcat -c
  $ADB shell am start -S -W -n bench.app/.MainActivity --es mode "$1" --es scenario "$2" >/dev/null
  for _ in $(seq 1 "${POLLS:-120}"); do
    line=$($ADB logcat -d -s ROOMBENCH:I -v raw | grep '^{' || true)
    if [[ -n "$line" ]]; then echo "$line" | tee -a "$OUT"; return; fi
    sleep 0.5
  done
  echo "timeout $1 $2" >&2; exit 1
}

for s in $SCENARIOS; do launch "${PREFIX}seed" "$s"; done
OUT=/dev/null launch "${PREFIX}run" "${SCENARIOS%% *}" >/dev/null # warm-up (dex/OS caches), discarded
for i in $(seq 1 "$RUNS"); do
  for s in $SCENARIOS; do launch "${PREFIX}run" "$s"; done
done
