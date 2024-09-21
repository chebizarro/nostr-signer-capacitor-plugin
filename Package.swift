// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "NostrSignerCapacitorPlugin",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "NostrSignerCapacitorPlugin",
            targets: ["NostrSignerPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "NostrSignerPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NostrSignerPlugin"),
        .testTarget(
            name: "NostrSignerPluginPluginTests",
            dependencies: ["NostrSignerPlugin"],
            path: "ios/Tests/NostrSignerPluginTests")
    ]
)