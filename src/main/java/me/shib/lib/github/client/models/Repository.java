package me.shib.lib.github.client.models;

import java.util.List;

public final class Repository {

    private String id;
    private VulnerabilityAlerts vulnerabilityAlerts;
    private DependencyGraphManifests dependencyGraphManifests;

    public String getId() {
        return id;
    }

    public VulnerabilityAlerts getVulnerabilityAlerts() {
        return vulnerabilityAlerts;
    }

    public DependencyGraphManifests getDependencyGraphManifests() {
        return dependencyGraphManifests;
    }

    public static class VulnerabilityAlerts extends NodeList {
        private List<VulnerabilityData> nodes;

        public List<VulnerabilityData> getNodes() {
            return nodes;
        }
    }

    public static class DependencyGraphManifests extends NodeList {
        private List<DependencyGraphManifest> nodes;

        public List<DependencyGraphManifest> getNodes() {
            return nodes;
        }
    }
}
