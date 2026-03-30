import SwiftUI

struct SearchView: View {
    @ObservedObject var viewModel: SearchViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                HStack(spacing: 8) {
                    TextField("Nhập từ khoá", text: Binding(
                        get: { viewModel.query },
                        set: { viewModel.onQueryChanged($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    .submitLabel(.search)
                    .onSubmit { viewModel.submitSearch() }

                    Button("Tìm bài viết!") {
                        viewModel.submitSearch()
                    }
                    .buttonStyle(.borderedProminent)
                }
                .padding(.horizontal)
                .padding(.top, 8)

                if viewModel.isLoading {
                    ProgressView("Dang tai du lieu...")
                        .padding(.top, 24)
                }

                if let message = viewModel.promptMessage, !message.isEmpty {
                    Text(message)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.leading)
                        .padding(.horizontal)
                }

                if !viewModel.currentPageResults.isEmpty {
                    List {
                        Section("Kết quả:") {
                            ForEach(viewModel.currentPageResults) { item in
                                NavigationLink(value: item.id) {
                                    VStack(alignment: .leading, spacing: 4) {
                                        Text(item.title)
                                            .font(.headline)
                                        Text(item.excerpt)
                                            .font(.subheadline)
                                            .foregroundStyle(.secondary)
                                            .lineLimit(3)
                                    }
                                    .padding(.vertical, 4)
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)

                    if viewModel.totalPages > 1 {
                        HStack(spacing: 12) {
                            Button("<<") {
                                viewModel.onPageSelected(viewModel.currentPage - 1)
                            }
                            .buttonStyle(.bordered)
                            .disabled(viewModel.currentPage <= 1)

                            Spacer()
                            Text("\(viewModel.currentPage)/\(viewModel.totalPages)")
                                .font(.callout.weight(.semibold))
                            Spacer()

                            Button(">>") {
                                viewModel.onPageSelected(viewModel.currentPage + 1)
                            }
                            .buttonStyle(.bordered)
                            .disabled(viewModel.currentPage >= viewModel.totalPages)
                        }
                        .padding(.horizontal)
                        .padding(.bottom, 8)
                    }
                } else {
                    List {
                        Section {
                            HStack {
                                Text("Gợi ý bài viết:")
                                    .font(.headline)
                                Spacer()
                                Button("Làm mới") {
                                    viewModel.refreshSuggestedThreads()
                                }
                                .disabled(viewModel.isRefreshingSuggestions)
                            }
                        }

                        ForEach(viewModel.suggestedThreads) { item in
                            NavigationLink(value: item.id) {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(item.title)
                                        .font(.headline)
                                    Text(item.excerpt)
                                        .font(.subheadline)
                                        .foregroundStyle(.secondary)
                                        .lineLimit(2)
                                }
                                .padding(.vertical, 4)
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                }
            }
            .navigationTitle("Đừng lo, hỏi đi!")
            .navigationDestination(for: String.self) { itemId in
                ThreadDetailContainerView(itemId: itemId, searchViewModel: viewModel)
            }
            .safeAreaInset(edge: .bottom, spacing: 0) {
                DonateFooterView()
            }
            .onAppear {
                viewModel.loadData()
            }
        }
    }
}

private struct DonateFooterView: View {
    var body: some View {
        VStack(spacing: 0) {
            Divider()

            Text("Tặng mình 1 ly trà đá! MoMo: 0849960203")
                .font(.footnote.weight(.medium))
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
                .padding(.horizontal, 16)
                .padding(.top, 9)
                .padding(.bottom, 1)
        }
        .background(.thinMaterial)
    }
}

private struct ThreadDetailContainerView: View {
    let itemId: String
    @ObservedObject var searchViewModel: SearchViewModel
    @StateObject private var detailViewModel = ThreadDetailViewModel()

    var body: some View {
        ThreadDetailView(viewModel: detailViewModel)
            .task(id: itemId) {
                let item = (searchViewModel.results + searchViewModel.suggestedThreads)
                    .first { $0.id == itemId }
                await MainActor.run {
                    detailViewModel.load(from: item)
                }
            }
    }
}
