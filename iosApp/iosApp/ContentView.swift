import UIKit
import SwiftUI
import ComposeApp
import GoogleMaps

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Load the Google maps API key from the AppSecrets.plist file
        let filePath = Bundle.main.path(forResource: "AppSecrets", ofType: "plist")!
        let plist = NSDictionary(contentsOfFile: filePath)!
        if let googleMapsApiKey = plist["GOOGLE_MAPS_IOS_API_KEY"] as? String {
            GMSServices.provideAPIKey(googleMapsApiKey)
        }
        
        return MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea(edges: .all)
            .ignoresSafeArea(.keyboard) // Compose has own keyboard handler
            .preferredColorScheme(.dark)
    }
}



