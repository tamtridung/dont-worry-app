import XCTest
@testable import DontWorryiOS

final class YamlParserTests: XCTestCase {
    func testThreadInfoParserReadsRequiredFields() {
        let yaml = """
        - thread-title: Test title
          thread-link: https://example.com/threads/123-Test
          created-by: admin
        """

        let parsed = ThreadInfoYamlParser().parse(yaml)
        XCTAssertEqual(parsed.count, 1)
        XCTAssertEqual(parsed.first?.threadTitle, "Test title")
        XCTAssertEqual(parsed.first?.threadLink, "https://example.com/threads/123-Test")
    }

    func testThreadDetailsParserSupportsResponseAlias() {
        let yaml = """
        - thread-id: "123"
          thread-title: Detail title
          thread-content: Main content
          responses:
            - responder: Alice
              body: Tra loi 1
            - user: Bob
              message: Tra loi 2
        """

        let parsed = ThreadDetailsYamlParser().parse(yaml)
        XCTAssertEqual(parsed.count, 1)
        XCTAssertEqual(parsed.first?.responses.count, 2)
        XCTAssertEqual(parsed.first?.responses.first?.responser, "Alice")
        XCTAssertEqual(parsed.first?.responses.last?.content, "Tra loi 2")
    }

    func testSynonymsParserNormalizesTermsAndEnablesFlag() {
        let yaml = """
        synonyms:
          - canonical: HIV
            terms:
              - hiv
              - hiv/aids
              - H.I.V
        """

        let result = SynonymsYamlParser().parse(yaml)
        XCTAssertTrue(result.enabled)
        XCTAssertEqual(result.groups.count, 1)
        XCTAssertEqual(result.groups.first?.canonical, "hiv")
        XCTAssertTrue(result.groups.first?.terms.contains("hiv") == true)
    }
}
