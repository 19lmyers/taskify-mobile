import FirebaseMessaging
import TaskifyShared

class iOSMessagingToken : MessagingToken {
    
    func get() async -> String? {
        do {
            return try await Messaging.messaging().token()
        } catch {
            return nil
        }
    }
    
}
