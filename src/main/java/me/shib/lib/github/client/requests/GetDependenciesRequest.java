package me.shib.lib.github.client.requests;

public final class GetDependenciesRequest extends GitHubRequest {

    private static final transient String queryResourceFileName = "github-dependency-query.txt";

    public GetDependenciesRequest(String owner, String repoName, int firstDependencies, String afterDependencies) {
        super(queryResourceFileName);
        replace("owner", owner);
        replace("repoName", repoName);
        replace("dependencies.first", firstDependencies);
        replace("dependencies.after", afterDependencies);
    }

}
