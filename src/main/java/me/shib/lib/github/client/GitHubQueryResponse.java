package me.shib.lib.github.client;

import me.shib.lib.github.client.models.Repository;

final class GitHubQueryResponse {

    private Data data;
    private GitHubClientError[] errors;

    Data getData() {
        return data;
    }

    String getErrorInfo() {
        if (errors == null || errors.length == 0) {
            return null;
        }
        StringBuilder errorInfo = new StringBuilder();
        for (GitHubClientError error : errors) {
            errorInfo.append(error.message).append("\n");
        }
        return errorInfo.toString().trim();
    }

    static final class Data {
        private Repository repository;

        Repository getRepository() {
            return repository;
        }
    }

    static final class GitHubClientError {
        private String message;

        @Override
        public String toString() {
            return message;
        }
    }

}
