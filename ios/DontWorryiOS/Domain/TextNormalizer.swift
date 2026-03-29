import Foundation

enum TextNormalizer {
    static func normalize(_ input: String) -> String {
        let lowercase = input.lowercased()
        let noDiacritics = lowercase.folding(options: [.diacriticInsensitive], locale: Locale(identifier: "en_US_POSIX"))

        return noDiacritics
            .replacingOccurrences(of: "[^a-z0-9\\s]", with: " ", options: .regularExpression)
            .replacingOccurrences(of: #"\s+"#, with: " ", options: .regularExpression)
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }
}
