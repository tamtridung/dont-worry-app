import Foundation

@MainActor
final class SearchViewModel: ObservableObject {
    nonisolated private static let maxResults = 100

    @Published var query: String = ""
    @Published var isLoading = false
    @Published var isRefreshingSuggestions = false
    @Published var promptMessage: String?
    @Published var suggestedThreads: [SearchListItem] = []
    @Published var results: [SearchListItem] = []
    @Published var currentPage = 1

    let pageSize = 10

    private var allThreads: [Thread] = []
    private var searchService: SearchService?
    private var hasLoadedInitialData = false

    var totalPages: Int {
        if results.isEmpty { return 0 }
        return ((results.count - 1) / pageSize) + 1
    }

    var currentPageResults: [SearchListItem] {
        guard !results.isEmpty else { return [] }
        let safePage = min(max(currentPage, 1), totalPages)
        let from = (safePage - 1) * pageSize
        let to = min(from + pageSize, results.count)
        return Array(results[from..<to])
    }

    func loadData() {
        if isLoading || hasLoadedInitialData { return }
        hasLoadedInitialData = true
        isLoading = true

        Task.detached(priority: .userInitiated) {
            let threads = ThreadRepository().loadThreads()
            let synonyms = SynonymsRepository().load()
            let service = SearchService(threads: threads, synonymGroups: synonyms.groups)
            let suggested = threads.shuffled().prefix(10).map { Self.toSearchListItem($0) }

            await MainActor.run {
                self.allThreads = threads
                self.searchService = service
                self.suggestedThreads = Array(suggested)
                self.isLoading = false
                if threads.isEmpty {
                    self.hasLoadedInitialData = false
                    self.promptMessage = "Khong tai duoc du lieu tim kiem. Vui long thu dong app va mo lai."
                } else {
                    self.promptMessage = synonyms.enabled
                        ? "Tra cứu các kiến thức thực tế về các bệnh STDs.\nDữ liệu được cập nhật liên tục từ: diendanhiv.vn\nAnh Tuanmecsedec là admin của diễn đàn với hơn 18 năm kinh nghiệm tư vấn có chứng chỉ chuyên môn."
                        : "Tra cứu các kiến thức thực tế về các bệnh STDs.\nDữ liệu được cập nhật liên tục từ: diendanhiv.vn"
                }
            }
        }
    }

    func onQueryChanged(_ newValue: String) {
        guard newValue != query else { return }
        query = newValue
        promptMessage = nil
        currentPage = 1
    }

    func submitSearch() {
        let normalized = query.trimmingCharacters(in: .whitespacesAndNewlines)
        if normalized.isEmpty {
            promptMessage = "Hãy nhập từ khóa để tìm kiếm."
            return
        }

        guard let service = searchService else {
            promptMessage = "Dataset is still loading."
            return
        }

        isLoading = true
        let maxResults = Self.maxResults

        Task.detached(priority: .userInitiated) {
            let mapped = service.search(query: normalized, limit: maxResults).map {
                Self.toSearchListItem($0.thread, excerpt: $0.excerpt)
            }

            await MainActor.run {
                self.results = mapped
                self.currentPage = 1
                self.promptMessage = mapped.isEmpty ? "No results found." : nil
                self.isLoading = false
            }
        }
    }

    func onPageSelected(_ page: Int) {
        guard totalPages > 0 else { return }
        let safe = min(max(page, 1), totalPages)
        if safe != currentPage {
            currentPage = safe
        }
    }

    func refreshSuggestedThreads() {
        guard !allThreads.isEmpty else { return }
        isRefreshingSuggestions = true
        let threads = allThreads

        Task.detached(priority: .utility) {
            let newSuggested = threads.shuffled().prefix(10).map { Self.toSearchListItem($0) }
            await MainActor.run {
                self.suggestedThreads = Array(newSuggested)
                self.isRefreshingSuggestions = false
            }
        }
    }

    nonisolated static func toSearchListItem(_ thread: Thread, excerpt: String? = nil) -> SearchListItem {
        SearchListItem(
            id: ThreadIdentity.fromParts(
                threadId: thread.threadId,
                threadLink: thread.threadLink,
                threadTitle: thread.threadTitle
            ),
            title: thread.threadTitle,
            excerpt: excerpt ?? ExcerptBuilder.fromText(thread.threadContent),
            threadLink: thread.threadLink,
            threadContent: thread.threadContent,
            responses: thread.responses
        )
    }
}
