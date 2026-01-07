package org.example.githubproxy;

import java.util.List;

record GithubRepo(String name, Owner owner, boolean fork){}
record Owner(String login){}
record GithubBranch(String name, Commit commit){}
record Commit(String sha){}

record RepositoryResponse(String repositoryName, String ownerLogin, List<BranchResponse> branches){}
record BranchResponse(String name, String lastCommitSha){}
record ApiErrorResponse(int status, String message){}