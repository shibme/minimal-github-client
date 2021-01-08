package me.shib.lib.github.client.models;

import java.util.List;

public final class DependencyGraphManifest {

    private final String blobPath;
    private final int dependenciesCount;
    private final boolean exceedsMaxSize;
    private final String filename;
    private Dependencies dependencies;

    public DependencyGraphManifest(String blobPath, int dependenciesCount, boolean exceedsMaxSize, String filename) {
        this.blobPath = blobPath;
        this.dependenciesCount = dependenciesCount;
        this.exceedsMaxSize = exceedsMaxSize;
        this.filename = filename;
    }

    public String getBlobPath() {
        return blobPath;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    public int getDependenciesCount() {
        return dependenciesCount;
    }

    public boolean isExceedsMaxSize() {
        return exceedsMaxSize;
    }

    public String getFilename() {
        return filename;
    }

    public static class Dependencies extends NodeList {
        private List<Dependency> nodes;

        public List<Dependency> getNodes() {
            return nodes;
        }
    }

}
