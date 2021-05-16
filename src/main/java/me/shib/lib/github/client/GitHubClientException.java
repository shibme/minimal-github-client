package me.shib.lib.github.client;

public final class GitHubClientException extends Exception {
    public GitHubClientException(String message) {
        super(message);
    }

    public GitHubClientException(Exception e) {
        super(e);
    }
}
