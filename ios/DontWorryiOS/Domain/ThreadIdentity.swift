import Foundation

enum ThreadIdentity {
    static func fromParts(threadId: String?, threadLink: String?, threadTitle: String) -> String {
        if let threadId = threadId?.trimmingCharacters(in: .whitespacesAndNewlines), !threadId.isEmpty {
            return "id:\(threadId)"
        }

        if let threadLink = threadLink?.trimmingCharacters(in: .whitespacesAndNewlines), !threadLink.isEmpty {
            return "link:\(threadLink.lowercased())"
        }

        return "title:\(TextNormalizer.normalize(threadTitle))"
    }
}
