import Foundation

@MainActor
final class ThreadDetailViewModel: ObservableObject {
    @Published var title = ""
    @Published var content = ""
    @Published var responses: [Response] = []
    @Published var threadLink: String?
    @Published var canOpenLink = false
    @Published var notFoundMessage: String?

    func load(from item: SearchListItem?) {
        guard let item else {
            notFoundMessage = "Thread not found."
            title = ""
            content = ""
            responses = []
            threadLink = nil
            canOpenLink = false
            return
        }

        title = item.title
        content = item.threadContent ?? "No content available."
        responses = item.responses
        threadLink = normalizeThreadLink(item.threadLink)
        canOpenLink = threadLink != nil
        notFoundMessage = nil
    }

    private func normalizeThreadLink(_ rawLink: String?) -> String? {
        guard let raw = rawLink?.trimmingCharacters(in: .whitespacesAndNewlines), !raw.isEmpty else {
            return nil
        }

        let link = raw.lowercased().hasPrefix("http://") || raw.lowercased().hasPrefix("https://")
            ? raw
            : "https://\(raw)"

        guard let url = URL(string: link), let scheme = url.scheme?.lowercased(), ["http", "https"].contains(scheme), url.host != nil else {
            return nil
        }
        return url.absoluteString
    }
}
