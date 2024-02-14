import SwiftUI
import FirebaseMessaging
import TaskifyShared

private var DATA_MESSAGE_TYPE = "DATA_MESSAGE_TYPE"
private var DATA_TASK_ID = "DATA_TASK_ID"

private var MESSAGE_TYPE_ASSIGNED = "MESSAGE_TYPE_ASSIGNED"
private var MESSAGE_TYPE_REMINDER = "MESSAGE_TYPE_REMINDER"
private var MESSAGE_TYPE_ACTION = "MESSAGE_TYPE_ACTION"

private var ASSIGNED_CATEGORY_IDENTIFIER = "assigned"

private var REMINDER_CATEGORY_IDENTIFIER = "reminder"
private var COMPLETE_ACTION_IDENTIFIER = "reminder.complete"

private var ACTION_CATEGORY_IDENTIFIER = "action"


func initNotificationCategories() {
    let assignedCategory = UNNotificationCategory(
        identifier: ASSIGNED_CATEGORY_IDENTIFIER,
        actions: [],
        intentIdentifiers: []
    )
    
     let completeAction = UNNotificationAction(identifier: COMPLETE_ACTION_IDENTIFIER, title: "Complete", options: [])
     let reminderCategory = UNNotificationCategory(
        identifier: REMINDER_CATEGORY_IDENTIFIER,
        actions: [completeAction],
        intentIdentifiers: []
    )
    
    let actionCategory = UNNotificationCategory(
        identifier: ACTION_CATEGORY_IDENTIFIER,
        actions: [],
        intentIdentifiers: []
    )

    UNUserNotificationCenter.current().setNotificationCategories([assignedCategory, reminderCategory, actionCategory])

}


extension AppDelegate: UNUserNotificationCenterDelegate {
    func userNotificationCenter(_: UNUserNotificationCenter, willPresent notification: UNNotification) async -> UNNotificationPresentationOptions {
        let userInfo = notification.request.content.userInfo

        let messageType = userInfo[DATA_MESSAGE_TYPE] as? String

        if messageType == MESSAGE_TYPE_REMINDER || messageType == MESSAGE_TYPE_ASSIGNED {
            return [[.banner, .list, .sound]]
        }

        if messageType == MESSAGE_TYPE_ACTION {
            return [[.list]]
        }

        return [[.banner, .list, .sound]]
    }

    func userNotificationCenter(_: UNUserNotificationCenter, didReceive response: UNNotificationResponse) async {
        let userInfo = response.notification.request.content.userInfo

         let messageType = userInfo[DATA_MESSAGE_TYPE] as? String

         if messageType == MESSAGE_TYPE_REMINDER {
            guard let taskId = userInfo[DATA_TASK_ID] as? String else { return }
            
            if response.actionIdentifier == COMPLETE_ACTION_IDENTIFIER {
                rootHolder.root.markTaskAsCompleted(taskId: taskId)
            } else {
                //rootHolder.root.onDeepLink(deepLink: DeepLinkViewTask())
            }
        }
    }
}

extension AppDelegate: MessagingDelegate {
    func messaging(_: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        if fcmToken != nil {
            rootHolder.root.linkFCMToken(token: fcmToken!)
        }
    }
}
