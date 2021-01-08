package me.shib.lib.github.client.models;

public final class PageInfo {

    private boolean hasNextPage;
    private boolean hasPreviousPage;
    private String endCursor;
    private String startCursor;

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public String getEndCursor() {
        return endCursor;
    }

    public String getStartCursor() {
        return startCursor;
    }
}
