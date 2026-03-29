import Foundation

struct SynonymExpander {
    private let canonicalByTerm: [String: String]

    init(groups: [SynonymGroup]) {
        var map: [String: String] = [:]
        for group in groups {
            let canonical = group.canonical
            for term in group.terms {
                if let current = map[term] {
                    if canonical < current {
                        map[term] = canonical
                    }
                } else {
                    map[term] = canonical
                }
            }
        }
        canonicalByTerm = map
    }

    func expand(queryTerms: [String]) -> [String] {
        guard !queryTerms.isEmpty else { return [] }
        return queryTerms.map { canonicalByTerm[$0] ?? $0 }
    }
}
