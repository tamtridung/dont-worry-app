import SwiftUI

struct ThreadDetailView: View {
    @ObservedObject var viewModel: ThreadDetailViewModel

    var body: some View {
        Group {
            if let notFound = viewModel.notFoundMessage {
                VStack(spacing: 10) {
                    Image(systemName: "doc.text.magnifyingglass")
                        .font(.title2)
                        .foregroundStyle(.secondary)
                    Text(notFound)
                        .font(.body)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 16) {
                        Text(viewModel.title)
                            .font(.title3.weight(.semibold))

                        Text(viewModel.content)
                            .font(.body)

                        if !viewModel.responses.isEmpty {
                            Text("Phản hồi:")
                                .font(.headline)

                            ForEach(Array(viewModel.responses.enumerated()), id: \.offset) { _, response in
                                VStack(alignment: .leading, spacing: 8) {
                                    Text(response.responser)
                                        .font(.subheadline.weight(.semibold))
                                    Text(response.content)
                                        .font(.body)
                                }
                                .padding(12)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(.thinMaterial, in: RoundedRectangle(cornerRadius: 10, style: .continuous))
                            }
                        }

                        if viewModel.canOpenLink, let threadLink = viewModel.threadLink, let url = URL(string: threadLink) {
                            Link(destination: url) {
                                Label("Mở link bài viết", systemImage: "safari")
                                    .font(.callout.weight(.semibold))
                            }
                            .padding(.top, 8)
                        }
                    }
                    .padding()
                }
            }
        }
        .navigationTitle("Chi tiết bài viết")
        .navigationBarTitleDisplayMode(.inline)
    }
}
