import Foundation

struct SearchService {
    private let index: SearchIndex
    private let synonymExpander: SynonymExpander

    init(threads: [Thread], synonymGroups: [SynonymGroup] = []) {
        self.index = SearchIndex.fromThreads(threads)
        self.synonymExpander = SynonymExpander(groups: synonymGroups)
    }

    func search(query: String, limit: Int = 10) -> [SearchResult] {
        let terms = synonymExpander.expand(queryTerms: QueryTokenizer.tokenizeQuery(query))
        guard !terms.isEmpty else { return [] }

        return index.documents
            .map { document in
                let score = Bm25Scorer.score(
                    queryTerms: terms,
                    termFrequencies: document.terms,
                    docLength: document.length,
                    averageDocLength: index.averageDocLength,
                    docFrequencyProvider: { index.docFrequency(term: $0) },
                    totalDocuments: index.documents.count
                )
                return SearchResult(
                    thread: document.thread,
                    score: score,
                    excerpt: ExcerptBuilder.fromText(document.thread.threadContent)
                )
            }
            .filter { $0.score > 0 }
            .sorted { lhs, rhs in lhs.score > rhs.score }
            .prefix(limit)
            .map { $0 }
    }
}
