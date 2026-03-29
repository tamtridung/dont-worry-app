import Foundation

enum QueryTokenizer {
    static func tokenizeQuery(_ query: String) -> [String] {
        let normalized = TextNormalizer.normalize(query)
        guard !normalized.isEmpty else { return [] }
        return normalized.split(separator: " ").map(String.init)
    }

    static func tokenizeForIndex(title: String, content: String?) -> [String] {
        let normalized = TextNormalizer.normalize(title + " " + (content ?? ""))
        guard !normalized.isEmpty else { return [] }
        return normalized.split(separator: " ").map(String.init)
    }
}
