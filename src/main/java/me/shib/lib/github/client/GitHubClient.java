package me.shib.lib.github.client;

import com.google.gson.Gson;
import me.shib.lib.github.client.models.Dependency;
import me.shib.lib.github.client.models.DependencyGraphManifest;
import me.shib.lib.github.client.models.Repository;
import me.shib.lib.github.client.models.VulnerabilityData;
import me.shib.lib.github.client.requests.GetDependenciesRequest;
import me.shib.lib.github.client.requests.GetVulnerabilityAlertsRequest;
import me.shib.lib.github.client.responses.GitHubQueryResponse;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GitHubClient {

    private static final String githubGraphQLEndpoint = "https://api.github.com/graphql";
    private static final int perPageCount = 100;

    private final String githubToken;
    private final OkHttpClient client;
    private final Gson gson;

    public GitHubClient(String githubToken) {
        this.githubToken = githubToken;
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    private GitHubQueryResponse getAdvisories(String owner, String repoName, String after)
            throws IOException {
        GetVulnerabilityAlertsRequest getVulnerabilityAlertsRequest =
                new GetVulnerabilityAlertsRequest(owner, repoName, perPageCount, after);
        RequestBody body = RequestBody.create(getVulnerabilityAlertsRequest.toRequest(),
                MediaType.parse("application/json"));
        Request request = new Request.Builder().post(body).url(githubGraphQLEndpoint)
                .addHeader("Authorization", "Bearer " + githubToken).build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        String responseContent = "";
        if (responseBody != null) {
            responseContent = responseBody.string();
        }
        response.close();
        return gson.fromJson(responseContent, GitHubQueryResponse.class);
    }

    private GitHubQueryResponse getDependencyManifest(String owner, String repoName, String afterDependencies)
            throws IOException {
        GetDependenciesRequest getDependenciesRequest =
                new GetDependenciesRequest(owner, repoName, perPageCount, afterDependencies);
        RequestBody body = RequestBody.create(getDependenciesRequest.toRequest(),
                MediaType.parse("application/json"));
        Request request = new Request.Builder().post(body).url(githubGraphQLEndpoint)
                .addHeader("Authorization", "Bearer " + githubToken)
                .addHeader("Accept", "application/vnd.github.hawkgirl-preview+json")
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        String responseContent = "";
        if (responseBody != null) {
            responseContent = responseBody.string();
        }
        response.close();
        return gson.fromJson(responseContent, GitHubQueryResponse.class);
    }

    public List<VulnerabilityData> getVulnerabilityDataList(String owner, String repoName) throws IOException {
        List<VulnerabilityData> vulnerabilityDataList = new ArrayList<>();
        boolean hasNextPage = true;
        String after = null;
        while (hasNextPage) {
            GitHubQueryResponse advisoryResponse = getAdvisories(owner, repoName, after);
            hasNextPage = false;
            if (advisoryResponse != null) {
                Repository.VulnerabilityAlerts vulnerabilityAlerts = advisoryResponse.getData().getRepository().getVulnerabilityAlerts();
                if (vulnerabilityAlerts != null && vulnerabilityAlerts.getNodes() != null &&
                        vulnerabilityAlerts.getNodes().size() > 0) {
                    vulnerabilityDataList.addAll(vulnerabilityAlerts.getNodes());
                    hasNextPage = vulnerabilityAlerts.getPageInfo().isHasNextPage();
                    after = vulnerabilityAlerts.getPageInfo().getEndCursor();
                }
            }
        }
        return vulnerabilityDataList;
    }

    public Map<DependencyGraphManifest, List<Dependency>> getDependencyGraph(String owner, String repoName) throws IOException {
        Map<String, List<Dependency>> dependenciesMap = new HashMap<>();
        Map<String, DependencyGraphManifest> dependencyGraphManifestMap = new HashMap<>();
        boolean hasNextPage = true;
        String after = null;
        while (hasNextPage) {
            hasNextPage = false;
            GitHubQueryResponse gitHubQueryResponse = getDependencyManifest(owner, repoName, after);
            if (gitHubQueryResponse != null) {
                Repository.DependencyGraphManifests dependencyGraphManifests = gitHubQueryResponse.getData()
                        .getRepository().getDependencyGraphManifests();
                if (dependencyGraphManifests != null && dependencyGraphManifests.getNodes() != null &&
                        dependencyGraphManifests.getNodes().size() > 0) {
                    for (DependencyGraphManifest dependencyGraphManifest : dependencyGraphManifests.getNodes()) {
                        DependencyGraphManifest manifest =
                                new DependencyGraphManifest(dependencyGraphManifest.getBlobPath(),
                                        dependencyGraphManifest.getDependenciesCount(),
                                        dependencyGraphManifest.isExceedsMaxSize(),
                                        dependencyGraphManifest.getFilename());
                        dependencyGraphManifestMap.put(dependencyGraphManifest.getBlobPath(), manifest);
                        DependencyGraphManifest.Dependencies dependencies = dependencyGraphManifest.getDependencies();
                        if (dependencies != null && dependencies.getNodes() != null &&
                                dependencies.getNodes().size() > 0) {
                            List<Dependency> dependencyList = dependenciesMap.get(dependencyGraphManifest.getBlobPath());
                            if (dependencyList == null) {
                                dependencyList = new ArrayList<>();
                            }
                            dependencyList.addAll(dependencies.getNodes());
                            dependenciesMap.put(dependencyGraphManifest.getBlobPath(), dependencyList);
                            if (dependencies.getPageInfo().isHasNextPage()) {
                                hasNextPage = true;
                                after = dependencies.getPageInfo().getEndCursor();
                            }
                        }
                    }
                }
            }
        }
        Map<DependencyGraphManifest, List<Dependency>> dependencyGraph = new HashMap<>();
        for (String blobPath : dependenciesMap.keySet()) {
            dependencyGraph.put(dependencyGraphManifestMap.get(blobPath), dependenciesMap.get(blobPath));
        }
        return dependencyGraph;
    }

}
