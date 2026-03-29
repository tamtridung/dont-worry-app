import SwiftUI

struct SearchView: View {
    @ObservedObject var viewModel: SearchViewModel

    var body: some View {
        NavigationStack {
            VStack(spacing: 12) {
                HStack(spacing: 8) {
                    TextField("Nhap tu khoa", text: Binding(
                        get: { viewModel.query },
                        set: { viewModel.onQueryChanged($0) }
                    ))
                    .textFieldStyle(.roundedBorder)
                    .submitLabel(.search)
                    .onSubmit { viewModel.submitSearch() }

                    Button("Tim") {
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
                        Section("Ket qua") {
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

                        if viewModel.totalPages > 1 {
                            Section("Trang") {
                                HStack {
                                    Button("Truoc") {
                                        viewModel.onPageSelected(viewModel.currentPage - 1)
                                    }
                                    .disabled(viewModel.currentPage <= 1)

                                    Spacer()
                                    Text("\(viewModel.currentPage)/\(viewModel.totalPages)")
                                    Spacer()

                                    Button("Sau") {
                                        viewModel.onPageSelected(viewModel.currentPage + 1)
                                    }
                                    .disabled(viewModel.currentPage >= viewModel.totalPages)
                                }
                            }
                        }
                    }
                    .listStyle(.insetGrouped)
                } else {
                    List {
                        Section {
                            HStack {
                                Text("Goi y chu de")
                                    .font(.headline)
                                Spacer()
                                Button("Lam moi") {
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
            .navigationTitle("Dont Worry")
            .navigationDestination(for: String.self) { itemId in
                ThreadDetailContainerView(itemId: itemId, searchViewModel: viewModel)
            }
            .onAppear {
                viewModel.loadData()
            }
        }
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
