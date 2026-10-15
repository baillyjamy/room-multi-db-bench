import Foundation
import Shared
import SwiftUI

/// Launched by `xcrun simctl launch --console-pty <device> bench.roomapp <mode> <scenario>`:
/// runs one measurement inside a real app process, prints one `ROOMBENCH {json}` line, then exits.
/// Launched without arguments (e.g. from Xcode), it shows buttons to run a measurement manually.
@main
struct BenchApp: App {
    var body: some Scene {
        WindowGroup { ContentView() }
    }
}

enum Bench {
    static let dir: String = {
        let base = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask)[0]
        return base.appendingPathComponent("roombench").path
    }()

    static func run(_ mode: String, _ scenario: String) -> String {
        AppBench.shared.run(mode: mode, scenario: scenario, dir: dir)
    }
}

struct ContentView: View {
    @State private var output = "Prêt"
    @State private var busy = false
    private let args = Array(ProcessInfo.processInfo.arguments.dropFirst())

    var body: some View {
        VStack(spacing: 16) {
            Text("Room multi-DB bench").font(.headline)
            ForEach(["S1", "S2", "L1", "L2"], id: \.self) { s in
                Button("Run \(s)") { manual(s) }.disabled(busy)
            }
            ScrollView { Text(output).font(.system(.footnote, design: .monospaced)).textSelection(.enabled) }
        }
        .padding()
        .task { automated() }
    }

    private func automated() {
        guard args.count >= 2 else { return }
        busy = true
        output = "\(args[0]) \(args[1])…"
        Thread.detachNewThread {
            let result = Bench.run(args[0], args[1])
            print("ROOMBENCH \(result)")
            fflush(stdout)
            exit(0)
        }
    }

    private func manual(_ scenario: String) {
        busy = true
        let large = scenario.hasPrefix("L")
        Thread.detachNewThread {
            let seed = Bench.run(large ? "large-seed" : "seed", scenario)
            let run = Bench.run(large ? "large-run" : "run", scenario)
            DispatchQueue.main.async { output = seed + "\n\n" + run; busy = false }
        }
    }
}
