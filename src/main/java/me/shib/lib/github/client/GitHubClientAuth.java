package me.shib.lib.github.client;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

final class GitHubClientAuth implements Interceptor {

    private final String authHeader;

    GitHubClientAuth(String githubToken) {
        this.authHeader = "Bearer " + githubToken;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder requestBuilder = originalRequest.newBuilder()
                .addHeader("Authorization", this.authHeader)
                .method(originalRequest.method(), originalRequest.body());
        String acceptHeader = originalRequest.header("Accept");
        if (acceptHeader != null) {
            requestBuilder.addHeader("Accept", acceptHeader);
        }
        return chain.proceed(requestBuilder.build());
    }
}
