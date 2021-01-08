package me.shib.lib.github.client.models;

public final class Dependency {

    private String packageName;
    private String requirements;
    private boolean hasDependencies;
    private String packageManager;

    public String getPackageName() {
        return packageName;
    }

    public String getRequirements() {
        return requirements;
    }

    public boolean isHasDependencies() {
        return hasDependencies;
    }

    public String getPackageManager() {
        return packageManager;
    }
}
