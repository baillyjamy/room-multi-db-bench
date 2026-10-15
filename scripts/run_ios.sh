#!/usr/bin/env bash
# Runs the benchmark inside the real SwiftUI app (iosApp/) installed in the booted iOS simulator:
# every measurement is a fresh app launch (`simctl launch --terminate-running-process`), the app prints
# one ROOMBENCH line and exits. Build first: see README ("Reproduire").
set -euo pipefail
RUNS=${RUNS:-15}
ENGINE=${ENGINE:-room} # room | sqldelight (S5c is Room-only)
DEVICE=${DEVICE:-booted}
PREFIX=${PREFIX:-} # "large-" (~120 MB) or "light-" (~12 MB) for the calendar suite
if [[ -n $PREFIX ]]; then DEFAULT_SCENARIOS="L1 L2"
elif [[ $ENGINE == room ]]; then DEFAULT_SCENARIOS="S1 S2 S5 S10 S5c"
else DEFAULT_SCENARIOS="S1 S2 S5 S10"; fi
SCENARIOS=${SCENARIOS:-$DEFAULT_SCENARIOS}
SUFFIX=$([[ $ENGINE == room ]] || echo "-$ENGINE")
OUT=${OUT:-results/ios$SUFFIX${PREFIX:+-${PREFIX%-}}.jsonl}
BUNDLE=bench.roomapp
cd "$(dirname "$0")/.."
mkdir -p results && touch "$OUT"; [[ -n "${KEEP:-}" ]] || : > "$OUT"

xcrun simctl install "$DEVICE" build/ios-app/Build/Products/Release-iphonesimulator/iosApp.app

launch() {
  xcrun simctl launch --console-pty --terminate-running-process "$DEVICE" "$BUNDLE" "$1" "$2" 2>/dev/null \
    | tr -d '\r' | sed -n 's/^ROOMBENCH //p' | tee -a "$OUT"
}

arg() { [[ $ENGINE == room ]] && echo "$1" || echo "$ENGINE:$1"; }
for s in $SCENARIOS; do launch "${PREFIX}seed" "$(arg "$s")"; done
OUT=/dev/null launch "${PREFIX}run" "$(arg "${SCENARIOS%% *}")" >/dev/null # warm-up, discarded
for i in $(seq 1 "$RUNS"); do
  for s in $SCENARIOS; do launch "${PREFIX}run" "$(arg "$s")"; done
done
