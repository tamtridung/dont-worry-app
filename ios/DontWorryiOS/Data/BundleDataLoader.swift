import Foundation

enum BundleDataLoader {
    static func readText(resourcePath: String) throws -> String {
        let parts = resourcePath.split(separator: "/").map(String.init)
        guard let fileNameWithExt = parts.last else {
            throw NSError(domain: "BundleDataLoader", code: 1)
        }

        let subdirectory: String?
        if parts.count > 1 {
            subdirectory = parts.dropLast().joined(separator: "/")
        } else {
            subdirectory = nil
        }

        let name = (fileNameWithExt as NSString).deletingPathExtension
        let ext = (fileNameWithExt as NSString).pathExtension

        if let url = Bundle.main.url(forResource: name, withExtension: ext, subdirectory: subdirectory) {
            return try String(contentsOf: url, encoding: .utf8)
        }

        // Xcode may flatten copied resources into bundle root depending on build phase config.
        if let url = Bundle.main.url(forResource: name, withExtension: ext) {
            return try String(contentsOf: url, encoding: .utf8)
        }

        guard let url = fallbackSearchURL(fileNameWithExt: fileNameWithExt) else {
            throw NSError(domain: "BundleDataLoader", code: 2)
        }

        return try String(contentsOf: url, encoding: .utf8)
    }

    private static func fallbackSearchURL(fileNameWithExt: String) -> URL? {
        let bundleURL = Bundle.main.bundleURL
        guard let enumerator = FileManager.default.enumerator(
            at: bundleURL,
            includingPropertiesForKeys: nil,
            options: [.skipsHiddenFiles]
        ) else {
            return nil
        }

        for case let url as URL in enumerator {
            if url.lastPathComponent == fileNameWithExt {
                return url
            }
        }
        return nil
    }
}
