import Foundation

struct SynonymsRepository {
    private let parser = SynonymsYamlParser()

    func load() -> SynonymsParseResult {
        let yamlText = try? BundleDataLoader.readText(resourcePath: "synonyms.yaml")
        return parser.parse(yamlText)
    }
}
