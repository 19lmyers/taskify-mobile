import FirebaseCrashlytics
import SwiftUI
import TaskifyShared
import UIKit

struct ComposeView: UIViewControllerRepresentable {
    var root: RootComponent

    func makeUIViewController(context _: Context) -> UIViewController {
        EntryPointKt.mainViewController(rootComponent: root)
    }

    func updateUIViewController(_: UIViewController, context _: Context) {
        // stub
    }
}

struct ContentView: View {
    @ObservedObject var rootHolder: RootHolder

    var body: some View {
        ComposeView(root: rootHolder.root)
            .ignoresSafeArea(.all)
    }
}
