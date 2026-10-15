#!/usr/bin/env bash
# Same protocol as run_ios.sh, but inside the real SwiftUI app (iosApp/): installed in the booted simulator,
# every measurement is a fresh app launch (`simctl launch --terminate-running-process`), the app prints
# one ROOMBENCH line and exits. Build first: see README ("Reproduire").
set -euo pipefail
RUNS=${RUNS:-15}
SCENARIOS=${SCENARIOS:-"S1 S2 S5 S10 S5c"}
DEVICE=${DEVICE:-booted}
PREFIX=${PREFIX:-} # "large-" for the large-volume suite
OUT=${OUT:-results/ios-app${PREFIX:+-large}.jsonl}
BUNDLE=bench.roomapp
cd "$(dirname "$0")/.."
mkdir -p results && touch "$OUT"; [[ -n "${KEEP:-}" ]] || : > "$OUT"

xcrun simctl install "$DEVICE" build/ios-app/Build/Products/Release-iphonesimulator/iosApp.app

launch() {
  xcrun simctl launch --console-pty --terminate-running-process "$DEVICE" "$BUNDLE" "$1" "$2" 2>/dev/null \
    | tr -d '\r' | sed -n 's/^ROOMBENCH //p' | tee -a "$OUT"
}

for s in $SCENARIOS; do launch "${PREFIX}seed" "$s"; done
OUT=/dev/null launch "${PREFIX}run" "${SCENARIOS%% *}" >/dev/null # warm-up, discarded
for i in $(seq 1 "$RUNS"); do
  for s in $SCENARIOS; do launch "${PREFIX}run" "$s"; done
done
