import XCTest
@testable import DontWorryiOS

final class SearchCoreTests: XCTestCase {
    func testTextNormalizerRemovesDiacriticsAndPunctuation() {
        let normalized = TextNormalizer.normalize("Đau rát, chảy-máu?!")
        XCTAssertEqual(normalized, "au rat chay mau")
    }

    func testQueryTokenizerSplitsWhitespace() {
        let terms = QueryTokenizer.tokenizeQuery("  HIV   xét   nghiệm ")
        XCTAssertEqual(terms, ["hiv", "xet", "nghiem"])
    }

    func testBm25HigherForMatchingDocument() {
        let threads = [
            Thread(threadId: "1", threadTitle: "HIV test", threadLink: nil, threadContent: "xet nghiem hiv som", responses: []),
            Thread(threadId: "2", threadTitle: "No match", threadLink: nil, threadContent: "da lieu thong thuong", responses: [])
        ]

        let service = SearchService(threads: threads)
        let results = service.search(query: "xet nghiem hiv", limit: 10)

        XCTAssertEqual(results.first?.thread.threadId, "1")
        XCTAssertEqual(results.count, 1)
    }
}
