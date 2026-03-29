import Foundation

struct IndexedDocument {
    let thread: Thread
    let terms: [String: Int]
    let length: Int
}

struct SearchIndex {
    let documents: [IndexedDocument]
    private let docFrequencies: [String: Int]
    let averageDocLength: Double

    func docFrequency(term: String) -> Int {
        docFrequencies[term] ?? 0
    }

    static func fromThreads(_ threads: [Thread]) -> SearchIndex {
        let documents = threads.map { thread in
            let tokens = QueryTokenizer.tokenizeForIndex(title: thread.threadTitle, content: thread.threadContent)
            return IndexedDocument(
                thread: thread,
                terms: Dictionary(tokens.map { ($0, 1) }, uniquingKeysWith: +),
                length: tokens.count
            )
        }

        var frequencies: [String: Int] = [:]
        for document in documents {
            for term in document.terms.keys {
                frequencies[term, default: 0] += 1
            }
        }

        let average = documents.isEmpty ? 0.0 : Double(documents.map(\.length).reduce(0, +)) / Double(documents.count)
        return SearchIndex(documents: documents, docFrequencies: frequencies, averageDocLength: average)
    }
}
