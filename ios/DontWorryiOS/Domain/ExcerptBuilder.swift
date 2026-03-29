import Foundation

enum ExcerptBuilder {
    static func fromText(_ text: String?, maxLength: Int = 180) -> String {
        guard let text, !text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return "No content preview available."
        }

        let clean = text
            .replacingOccurrences(of: #"\s+"#, with: " ", options: .regularExpression)
            .trimmingCharacters(in: .whitespacesAndNewlines)

        if clean.count <= maxLength {
            return clean
        }

        let index = clean.index(clean.startIndex, offsetBy: maxLength)
        return clean[..<index].trimmingCharacters(in: .whitespacesAndNewlines) + "..."
    }
}
