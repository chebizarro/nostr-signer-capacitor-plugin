// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NostrSignerCapacitorPlugin",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "NostrSignerCapacitorPlugin",
            targets: ["NostrSignerPluginPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "NostrSignerPluginPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NostrSignerPluginPlugin"),
        .testTarget(
            name: "NostrSignerPluginPluginTests",
            dependencies: ["NostrSignerPluginPlugin"],
            path: "ios/Tests/NostrSignerPluginPluginTests")
    ]
)