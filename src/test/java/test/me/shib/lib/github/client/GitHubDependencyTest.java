package test.me.shib.lib.github.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shib.lib.github.client.GitHubClient;
import me.shib.lib.github.client.models.Dependency;
import me.shib.lib.github.client.models.DependencyGraphManifest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class GitHubDependencyTest {

    private static final transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final transient GitHubClient gitHubClient;
    private final transient String githubRepoOwner;
    private final transient String githubRepoName;

    public GitHubDependencyTest() {
        String githubToken = System.getenv("GITHUB_TOKEN");
        this.gitHubClient = new GitHubClient(githubToken);
        this.githubRepoOwner = System.getenv("GITHUB_REPO_OWNER");
        this.githubRepoName = System.getenv("GITHUB_REPO_NAME");
    }

    public static void main(String[] args) throws IOException {
        GitHubDependencyTest test = new GitHubDependencyTest();
        test.testAdvisoryAPI();
        test.testDependenciesAPI();
    }

    private void testAdvisoryAPI() throws IOException {
        System.out.println(gson.toJson(gitHubClient.getVulnerabilityDataList(githubRepoOwner, githubRepoName)));
    }

    private void testDependenciesAPI() throws IOException {
        Map<DependencyGraphManifest, List<Dependency>> map = gitHubClient.getDependencyGraph(githubRepoOwner, githubRepoName);
        for (DependencyGraphManifest manifest : map.keySet()) {
            System.out.println(manifest.getBlobPath() + ": " + map.get(manifest).size());
        }
    }

}
