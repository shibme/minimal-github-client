package me.shib.lib.github.client;

import com.google.gson.Gson;
import me.shib.lib.github.client.models.Dependency;
import me.shib.lib.github.client.models.DependencyGraphManifest;
import me.shib.lib.github.client.models.Repository;
import me.shib.lib.github.client.models.VulnerabilityData;
import me.shib.lib.github.client.requests.GetDependenciesRequest;
import me.shib.lib.github.client.requests.GetVulnerabilityAlertsRequest;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GitHubClient {

    private static final String GITHUB_HTTP_LOGGING = System.getenv("GITHUB_HTTP_LOGGING");
    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");
    private static final String githubGraphQLEndpoint = "https://api.github.com/graphql";
    private static final transient Map<String, GitHubClient> gitHubClientMap = new HashMap<>();
    private static final int perPageCount = 100;

    private final OkHttpClient client;
    private final Gson gson;

    private GitHubClient(String githubToken) {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new GitHubClientAuth(githubToken));
        boolean httpLogging = GITHUB_HTTP_LOGGING != null &&
                GITHUB_HTTP_LOGGING.equalsIgnoreCase("TRUE");
        if (httpLogging) {
            okHttpClientBuilder.addInterceptor(new HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        this.client = okHttpClientBuilder.build();
        this.gson = new Gson();
    }

    public static synchronized GitHubClient getInstance(String githubToken) throws GitHubClientException {
        if (githubToken == null || githubToken.isEmpty()) {
            throw new GitHubClientException("Please provide a valid GitHub Token");
        }
        GitHubClient gitHubClient = gitHubClientMap.get(githubToken);
        if (gitHubClient == null) {
            gitHubClient = new GitHubClient(githubToken);
            gitHubClientMap.put(githubToken, gitHubClient);
        }
        return gitHubClient;
    }

    public static GitHubClient getInstance() throws GitHubClientException {
        return GitHubClient.getInstance(GITHUB_TOKEN);
    }

    private GitHubQueryResponse getGitHubQueryResponse(Response response) throws GitHubClientException {
        try {
            String responseBody = null;
            if (response.body() != null) {
                responseBody = response.body().string();
            }
            if (response.code() >= 200 && response.code() <= 250) {
                GitHubQueryResponse queryResponse = gson.fromJson(responseBody, GitHubQueryResponse.class);
                if (queryResponse.getErrorInfo() != null) {
                    response.close();
                    throw new GitHubClientException(queryResponse.getErrorInfo());
                }
                response.close();
                return queryResponse;
            } else {
                GitHubQueryResponse.GitHubClientError error = gson.fromJson(responseBody,
                        GitHubQueryResponse.GitHubClientError.class);
                response.close();
                throw new GitHubClientException(error.toString());
            }
        } catch (IOException e) {
            response.close();
            throw new GitHubClientException(e);
        }
    }

    private GitHubQueryResponse getAdvisories(String owner, String repoName, String after)
            throws GitHubClientException {
        try {
            GetVulnerabilityAlertsRequest getVulnerabilityAlertsRequest =
                    new GetVulnerabilityAlertsRequest(owner, repoName, perPageCount, after);
            RequestBody body = RequestBody.create(getVulnerabilityAlertsRequest.toRequest(),
                    MediaType.parse("application/json"));
            Request request = new Request.Builder().post(body).url(githubGraphQLEndpoint).build();
            return getGitHubQueryResponse(client.newCall(request).execute());
        } catch (IOException e) {
            throw new GitHubClientException(e);
        }
    }

    private GitHubQueryResponse getDependencyManifest(String owner, String repoName, String afterDependencies)
            throws GitHubClientException {
        try {
            GetDependenciesRequest getDependenciesRequest =
                    new GetDependenciesRequest(owner, repoName, perPageCount, afterDependencies);
            RequestBody body = RequestBody.create(getDependenciesRequest.toRequest(),
                    MediaType.parse("application/json"));
            Request request = new Request.Builder().post(body).url(githubGraphQLEndpoint)
                    .addHeader("Accept", "application/vnd.github.hawkgirl-preview+json")
                    .build();
            return getGitHubQueryResponse(client.newCall(request).execute());
        } catch (IOException e) {
            throw new GitHubClientException(e);
        }
    }

    public List<VulnerabilityData> getVulnerabilityDataList(String owner, String repoName) throws GitHubClientException {
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

    public Map<String, Integer> getDependencyCountByManifest(String owner, String repoName) throws GitHubClientException {
        Map<String, Integer> countByManifest = new HashMap<>();
        GitHubQueryResponse gitHubQueryResponse = getDependencyManifest(owner, repoName, null);
        Repository.DependencyGraphManifests dependencyGraphManifests = gitHubQueryResponse.getData()
                .getRepository().getDependencyGraphManifests();
        if (dependencyGraphManifests != null) {
            for (DependencyGraphManifest manifest : dependencyGraphManifests.getNodes()) {
                countByManifest.put(manifest.getBlobPath(), manifest.getDependenciesCount());
            }
        }
        return countByManifest;
    }

    public int getDependencyCount(String owner, String repoName) throws GitHubClientException {
        return getDependencyCountByManifest(owner, repoName).values().stream().mapToInt(Integer::intValue).sum();
    }

    public Map<DependencyGraphManifest, List<Dependency>> getDependencyGraph(String owner, String repoName) throws GitHubClientException {
        Map<String, List<Dependency>> dependenciesMap = new HashMap<>();
        Map<String, DependencyGraphManifest> dependencyGraphManifestMap = new HashMap<>();
        boolean hasNextPage = true;
        String after = null;
        while (hasNextPage) {
            hasNextPage = false;
            GitHubQueryResponse gitHubQueryResponse = getDependencyManifest(owner, repoName, after);
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
        Map<DependencyGraphManifest, List<Dependency>> dependencyGraph = new HashMap<>();
        for (String blobPath : dependenciesMap.keySet()) {
            dependencyGraph.put(dependencyGraphManifestMap.get(blobPath), dependenciesMap.get(blobPath));
        }
        return dependencyGraph;
    }

}
