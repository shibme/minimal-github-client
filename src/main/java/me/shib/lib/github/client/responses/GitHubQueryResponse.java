package me.shib.lib.github.client.responses;

import me.shib.lib.github.client.models.Repository;

public final class GitHubQueryResponse {

    private Data data;

    public Data getData() {
        return data;
    }

    public final class Data {
        private Repository repository;

        public Repository getRepository() {
            return repository;
        }
    }
}
