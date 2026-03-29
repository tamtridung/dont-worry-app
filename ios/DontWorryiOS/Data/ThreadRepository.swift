import Foundation

struct ThreadRepository {
    private let infoParser = ThreadInfoYamlParser()
    private let detailsParser = ThreadDetailsYamlParser()
    private let fileManager = FileManager.default

    func loadThreads() -> [Thread] {
        if let cached = readThreadsFromCache(), !cached.isEmpty {
            return cached
        }

        let infoItems = (try? BundleDataLoader.readText(resourcePath: "thread-info.yaml"))
            .map(infoParser.parse) ?? []
        let detailItems = (try? BundleDataLoader.readText(resourcePath: "thread-details.yaml"))
            .map(detailsParser.parse) ?? []

        let infoByThreadId: [String: ThreadInfoRecord] = Dictionary(uniqueKeysWithValues: infoItems.compactMap { info in
            guard let id = extractThreadId(info.threadLink) else { return nil }
            return (id, info)
        })

        var infoByNormalizedTitle: [String: ThreadInfoRecord] = [:]
        for item in infoItems {
            let normalizedTitle = TextNormalizer.normalize(item.threadTitle)
            if infoByNormalizedTitle[normalizedTitle] == nil {
                infoByNormalizedTitle[normalizedTitle] = item
            }
        }

        let threads = detailItems.map { detail in
            let info = detail.threadId.flatMap { infoByThreadId[$0] }
                ?? infoByNormalizedTitle[TextNormalizer.normalize(detail.threadTitle)]
            let responses = detail.responses.filter { !$0.content.isEmpty }

            return Thread(
                threadId: detail.threadId,
                threadTitle: detail.threadTitle,
                threadLink: info?.threadLink,
                threadContent: detail.threadContent,
                responses: responses
            )
        }

        if !threads.isEmpty {
            writeThreadsToCache(threads)
        }

        return threads
    }

    private func cacheFileURL() -> URL? {
        guard let cachesDir = fileManager.urls(for: .cachesDirectory, in: .userDomainMask).first else {
            return nil
        }
        return cachesDir.appendingPathComponent("thread-cache-\(packageUpdateToken()).json")
    }

    private func readThreadsFromCache() -> [Thread]? {
        guard let url = cacheFileURL(), fileManager.fileExists(atPath: url.path) else {
            return nil
        }

        guard let data = try? Data(contentsOf: url) else {
            return nil
        }

        return try? JSONDecoder().decode([Thread].self, from: data)
    }

    private func writeThreadsToCache(_ threads: [Thread]) {
        guard let url = cacheFileURL(),
              let data = try? JSONEncoder().encode(threads) else {
            return
        }

        try? data.write(to: url, options: .atomic)
    }

    private func extractThreadId(_ link: String?) -> String? {
        let trimmed = (link ?? "").trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmed.isEmpty else { return nil }

        guard let range = trimmed.range(of: "/threads/") else { return nil }
        let after = trimmed[range.upperBound...]
        let prefix = after.split(separator: "-").first
        let id = prefix.map(String.init)?.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let id, !id.isEmpty else { return nil }
        return id
    }

    private func packageUpdateToken() -> String {
        let version = Bundle.main.object(forInfoDictionaryKey: "CFBundleVersion") as? String ?? "0"
        return version
    }
}
