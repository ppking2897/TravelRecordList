import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        IosPlatformModuleKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}