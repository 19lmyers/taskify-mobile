import SwiftUI
import TaskifyShared

class iOSShareSheetManager: ObservableObject, ShareSheetManager {
    @Published var sharingEntry: SharingEntry?
    
    func shareList(token: String) {
        let url = URL(string: "https://taskify.chara.dev/join?token=" + token)
        
        let items: [Any] = [url!]
        
        sharingEntry = SharingEntry(items: items)
    }
}

struct SharingEntry: Identifiable {
    let id = UUID()
    let items: [Any]
}

struct ActivityView: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: UIViewControllerRepresentableContext<ActivityView>) -> UIActivityViewController {
        return UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: UIViewControllerRepresentableContext<ActivityView>) {}
}
