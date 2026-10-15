import ProjectDescription

// Generate the Xcode project with `tuist generate --no-open` (the generated project is committed too).
let project = Project(
    name: "iosApp",
    targets: [
        .target(
            name: "iosApp",
            destinations: [.iPhone],
            product: .app,
            bundleId: "bench.roomapp",
            deploymentTargets: .iOS("17.0"),
            infoPlist: .extendingDefault(with: ["UILaunchScreen": [:], "CFBundleDisplayName": "RoomBench"]),
            sources: ["Sources/**"],
            scripts: [
                .pre(
                    script: """
                    cd "$SRCROOT/.."
                    export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home)}"
                    ./gradlew -q :shared:linkReleaseFrameworkIosSimulatorArm64
                    """,
                    name: "Build Kotlin framework (Gradle)",
                    basedOnDependencyAnalysis: false
                ),
            ],
            settings: .settings(base: [
                "FRAMEWORK_SEARCH_PATHS": "$(SRCROOT)/../shared/build/bin/iosSimulatorArm64/releaseFramework",
                "OTHER_LDFLAGS": "-framework Shared -lc++ -lsqlite3",
                "ENABLE_USER_SCRIPT_SANDBOXING": "NO",
                "SWIFT_VERSION": "5.0",
                "ARCHS": "arm64",
                "CODE_SIGN_IDENTITY": "-",
                "CODE_SIGNING_REQUIRED": "NO",
            ])
        ),
    ]
)
