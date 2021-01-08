package me.shib.lib.github.client.requests;

import com.google.gson.Gson;

abstract class GitHubRequest {

    private static final transient Gson gson = new Gson();

    protected String query;

    GitHubRequest(String queryResourceFileName) {
        this.query = ResourceUtil.readResource(queryResourceFileName);
    }

    void replace(String placeholder, Object value) {
        this.query = this.query.replace("${" + placeholder + "}", gson.toJson(value));
    }

    @Override
    public String toString() {
        return query;
    }

    public String toRequest() {
        return gson.toJson(this);
    }

}
