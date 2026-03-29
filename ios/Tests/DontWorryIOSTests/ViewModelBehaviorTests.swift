import XCTest
@testable import DontWorryiOS

final class ViewModelBehaviorTests: XCTestCase {
    func testThreadIdentityPrefersThreadIdThenLinkThenTitle() {
        XCTAssertEqual(
            ThreadIdentity.fromParts(threadId: " 123 ", threadLink: "example.com/threads/123", threadTitle: "Title"),
            "id:123"
        )

        XCTAssertEqual(
            ThreadIdentity.fromParts(threadId: nil, threadLink: "EXAMPLE.com/threads/abc", threadTitle: "Title"),
            "link:example.com/threads/abc"
        )

        XCTAssertEqual(
            ThreadIdentity.fromParts(threadId: nil, threadLink: nil, threadTitle: "Đau rát!!"),
            "title:au rat"
        )
    }

    @MainActor
    func testDetailViewModelNormalizesValidLinksAndRejectsInvalid() {
        let viewModel = ThreadDetailViewModel()

        let validItem = SearchListItem(
            id: "id:1",
            title: "Title",
            excerpt: "Excerpt",
            threadLink: "www.diendanhiv.vn/threads/1-abc",
            threadContent: "Content",
            responses: []
        )
        viewModel.load(from: validItem)
        XCTAssertEqual(viewModel.threadLink, "https://www.diendanhiv.vn/threads/1-abc")
        XCTAssertTrue(viewModel.canOpenLink)

        let invalidItem = SearchListItem(
            id: "id:2",
            title: "Title",
            excerpt: "Excerpt",
            threadLink: "javascript:alert(1)",
            threadContent: "Content",
            responses: []
        )
        viewModel.load(from: invalidItem)
        XCTAssertNil(viewModel.threadLink)
        XCTAssertFalse(viewModel.canOpenLink)
    }
}
