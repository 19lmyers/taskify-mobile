import SwiftUI
import FirebaseCore
import FirebaseMessaging
import TaskifyShared

private var PATH_JOIN_WORKSPACE = "/join"
private var QUERY_INVITE_TOKEN = "token"

var rootHolder: RootHolder = .init()

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor private var appDelegate: AppDelegate

    @ObservedObject var shareSheet = iOSShareSheetManager()
    
    init() {
        UserDefaults.standard.register(defaults: ["NSApplicationCrashOnExceptions": true])

        FirebaseApp.configure()
        
        EntryPointKt.setupKoin(loader: iOSModelLoader(), shareSheet: shareSheet, token: iOSMessagingToken())
    }
    
	var body: some Scene {
		WindowGroup {
            ContentView(rootHolder: rootHolder)
                .ignoresSafeArea(.all)
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didBecomeActiveNotification)) { _ in
                    LifecycleRegistryExtKt.resume(rootHolder.lifecycle)
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification)) { _ in
                    LifecycleRegistryExtKt.pause(rootHolder.lifecycle)
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.didEnterBackgroundNotification)) { _ in
                    LifecycleRegistryExtKt.stop(rootHolder.lifecycle)
                }
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willTerminateNotification)) { _ in
                    LifecycleRegistryExtKt.destroy(rootHolder.lifecycle)
                }
                .onOpenURL { url in
                    guard let components = NSURLComponents(url: url, resolvingAgainstBaseURL: true),
                          let path = components.path,
                          let params = components.queryItems
                    else {
                        return
                    }

                    switch path {
                    case PATH_JOIN_WORKSPACE:
                        if let token = params.first(where: { $0.name == QUERY_INVITE_TOKEN })?.value {
                            rootHolder.root.onDeepLink(deepLink: DeepLinkJoinWorkspace(token: token))
                        }
                    default:
                        break
                    }
                }.sheet(item: $shareSheet.sharingEntry) { sharingEntry in
                    ActivityView(items: sharingEntry.items)
                }
		}
	}
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions _: [UIApplication.LaunchOptionsKey: Any]?) -> Bool
    {
        Messaging.messaging().delegate = self

        UNUserNotificationCenter.current().delegate = self

        application.registerForRemoteNotifications()

        initNotificationCategories()

        return true
    }

    func application(
        _: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions
    ) -> UISceneConfiguration {
        if let shortcutItem = options.shortcutItem {
            if shortcutItem.type == "CreateTaskAction" {
                rootHolder.root.onDeepLink(deepLink: DeepLinkCreateTask.shared)
            }
        }

        let configuration = UISceneConfiguration(
            name: connectingSceneSession.configuration.name,
            sessionRole: connectingSceneSession.role
        )
        configuration.delegateClass = SceneDelegate.self
        return configuration
    }

    func application(_: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
}

class SceneDelegate: NSObject, UIWindowSceneDelegate {
    func windowScene(_: UIWindowScene, performActionFor shortcutItem: UIApplicationShortcutItem, completionHandler: @escaping (Bool) -> Void) {
        if shortcutItem.type == "CreateTaskAction" {
            rootHolder.root.onDeepLink(deepLink: DeepLinkCreateTask.shared)
        }

        completionHandler(true)
    }
}


class RootHolder: ObservableObject {
    let lifecycle: LifecycleRegistry

    var root: RootComponent

    init() {
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()

        root = DefaultRootComponent(
            componentContext: DefaultComponentContext(lifecycle: lifecycle),
            deepLink: DeepLinkNone.shared
        )

        LifecycleRegistryExtKt.create(lifecycle)
    }

    deinit {
        // Destroy the root component before it is deallocated
        LifecycleRegistryExtKt.destroy(lifecycle)
    }
}
