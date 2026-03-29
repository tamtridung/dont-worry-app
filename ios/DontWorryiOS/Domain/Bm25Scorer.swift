import Foundation

enum Bm25Scorer {
    private static let k1 = 1.2
    private static let b = 0.75

    static func score(
        queryTerms: [String],
        termFrequencies: [String: Int],
        docLength: Int,
        averageDocLength: Double,
        docFrequencyProvider: (String) -> Int,
        totalDocuments: Int
    ) -> Double {
        guard !queryTerms.isEmpty, docLength > 0, totalDocuments > 0 else {
            return 0.0
        }

        let safeAvg = averageDocLength <= 0 ? 1.0 : averageDocLength
        var score = 0.0

        for term in queryTerms {
            let tf = termFrequencies[term] ?? 0
            if tf <= 0 { continue }

            let df = docFrequencyProvider(term)
            let idf = log(((Double(totalDocuments - df) + 0.5) / (Double(df) + 0.5)) + 1.0)
            let numerator = Double(tf) * (k1 + 1.0)
            let denominator = Double(tf) + k1 * (1.0 - b + b * (Double(docLength) / safeAvg))
            score += idf * (numerator / denominator)
        }

        return score
    }
}
