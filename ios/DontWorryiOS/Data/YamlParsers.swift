import Foundation
import Yams

struct ThreadInfoRecord {
    let threadTitle: String
    let threadLink: String?
    let createdBy: String?
}

struct ThreadDetailsRecord {
    let threadId: String?
    let threadTitle: String
    let threadContent: String?
    let responses: [Response]
}

struct SynonymsParseResult {
    let groups: [SynonymGroup]
    let enabled: Bool
    let warning: String?
}

struct ThreadInfoYamlParser {
    func parse(_ yamlText: String) -> [ThreadInfoRecord] {
        guard let loaded = try? Yams.load(yaml: yamlText),
              let items = loaded as? [[String: Any]] else {
            return []
        }

        return items.compactMap { map in
            let title = (map["thread-title"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            guard !title.isEmpty else { return nil }

            let threadLink = (map["thread-link"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            let createdBy = (map["created-by"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)

            return ThreadInfoRecord(
                threadTitle: title,
                threadLink: threadLink?.isEmpty == true ? nil : threadLink,
                createdBy: createdBy?.isEmpty == true ? nil : createdBy
            )
        }
    }
}

struct ThreadDetailsYamlParser {
    func parse(_ yamlText: String) -> [ThreadDetailsRecord] {
        guard let loaded = try? Yams.load(yaml: yamlText),
              let items = loaded as? [[String: Any]] else {
            return []
        }

        return items.compactMap { map in
            let title = (map["thread-title"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            guard !title.isEmpty else { return nil }

            let responsesRaw = map["responses"] as? [[String: Any]] ?? []
            let responses = responsesRaw.compactMap(parseResponse)

            let threadId = (map["thread-id"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)
            let content = (map["thread-content"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines)

            return ThreadDetailsRecord(
                threadId: threadId?.isEmpty == true ? nil : threadId,
                threadTitle: title,
                threadContent: content?.isEmpty == true ? nil : content,
                responses: responses
            )
        }
    }

    private func parseResponse(_ raw: [String: Any]) -> Response? {
        let responser = firstNonBlank(
            raw["responser"],
            raw["responder"],
            raw["user"],
            raw["name"]
        ) ?? "Anonymous"

        guard let content = firstNonBlank(
            raw["response-content"],
            raw["content"],
            raw["response"],
            raw["text"],
            raw["body"],
            raw["message"]
        ) else {
            return nil
        }

        return Response(responser: responser, content: content)
    }

    private func firstNonBlank(_ values: Any?...) -> String? {
        for value in values {
            if let string = value as? String {
                let trimmed = string.trimmingCharacters(in: .whitespacesAndNewlines)
                if !trimmed.isEmpty {
                    return trimmed
                }
            }
        }
        return nil
    }
}

struct SynonymsYamlParser {
    func parse(_ yamlText: String?) -> SynonymsParseResult {
        guard let yamlText, !yamlText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
            return SynonymsParseResult(groups: [], enabled: false, warning: "synonyms file missing")
        }

        guard let loaded = try? Yams.load(yaml: yamlText),
              let root = loaded as? [String: Any] else {
            return SynonymsParseResult(groups: [], enabled: false, warning: "invalid YAML root")
        }

        let items = root["synonyms"] as? [[String: Any]] ?? []
        let groups = items.compactMap(parseGroup)
        return SynonymsParseResult(groups: groups, enabled: !groups.isEmpty, warning: nil)
    }

    private func parseGroup(_ raw: [String: Any]) -> SynonymGroup? {
        let canonicalRaw = (raw["canonical"] as? String)?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
        guard !canonicalRaw.isEmpty else { return nil }

        let termsRaw = raw["terms"] as? [String] ?? []
        var normalizedTerms = Set(termsRaw
            .map { TextNormalizer.normalize($0) }
            .filter { !$0.isEmpty })

        let canonical = TextNormalizer.normalize(canonicalRaw)
        normalizedTerms.insert(canonical)

        guard !normalizedTerms.isEmpty else { return nil }
        return SynonymGroup(canonical: canonical, terms: normalizedTerms)
    }
}
