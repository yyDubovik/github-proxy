package org.example.githubproxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class GitHubService {
    private final GitHubClient gitHubClient;

    GitHubService(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    List<RepositoryResponse> getUserRepositories(String username) {
        List<GithubRepo> repos = gitHubClient.getUserRepos(username);

        return repos.parallelStream()
                .filter(repo -> !repo.fork())
                .map(repo -> {
                    List<GithubBranch> branches = gitHubClient.getRepoBranches(username, repo.name());

                    List<BranchResponse> branchResponses = branches.stream()
                            .map(b -> new BranchResponse(b.name(), b.commit().sha()))
                            .toList();

                    return new RepositoryResponse(repo.name(), repo.owner().login(), branchResponses);
                })
                .toList();
    }
}
