import Foundation

struct Response: Codable, Hashable {
    let responser: String
    let content: String
}

struct Thread: Codable, Hashable, Identifiable {
    var id: String {
        if let threadId, !threadId.isEmpty {
            return threadId
        }
        return threadTitle
    }

    let threadId: String?
    let threadTitle: String
    let threadLink: String?
    let threadContent: String?
    let responses: [Response]
}

struct SynonymGroup: Codable, Hashable {
    let canonical: String
    let terms: Set<String>
}

struct SearchResult: Hashable {
    let thread: Thread
    let score: Double
    let excerpt: String
}

struct SearchListItem: Hashable, Identifiable {
    let id: String
    let title: String
    let excerpt: String
    let threadLink: String?
    let threadContent: String?
    let responses: [Response]
}
