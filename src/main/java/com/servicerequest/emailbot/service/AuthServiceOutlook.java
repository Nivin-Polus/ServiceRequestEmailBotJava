package com.servicerequest.emailbot.service;

import com.microsoft.aad.msal4j.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class AuthServiceOutlook {
    private static final Dotenv dotenv = Dotenv.load();

    private static final String CLIENT_ID = dotenv.get("OUTLOOK_CLIENT_ID");
    private static final String TENANT_ID = dotenv.get("OUTLOOK_TENANT_ID", "common");
    private static final String TOKEN_FILE = "tokens.json";
    private static final Set<String> SCOPES = new HashSet<>(Arrays.asList(
            "User.Read", "Mail.Read", "Mail.Send"
    ));

    private PublicClientApplication app;

    public AuthServiceOutlook() throws Exception {
        app = PublicClientApplication.builder(CLIENT_ID)
                .authority("https://login.microsoftonline.com/" + TENANT_ID)
                .build();

        loadCache();
    }

    /** Load token cache from file */
    private void loadCache() {
        try {
            if (Files.exists(Paths.get(TOKEN_FILE))) {
                String data = Files.readString(Paths.get(TOKEN_FILE));
                if (!data.isBlank()) {
                    app.tokenCache().deserialize(data);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠️ Failed to load token cache: " + e.getMessage());
        }
    }

    /** Save token cache to file */
    private void saveCache() {
        try {
            String serialized = app.tokenCache().serialize();
            Files.writeString(Paths.get(TOKEN_FILE), serialized);
        } catch (IOException e) {
            System.out.println("⚠️ Failed to save token cache: " + e.getMessage());
        }
    }

    /** Get access token using silent flow or device flow */
    public String getAccessToken() throws Exception {
        IAuthenticationResult result = null;

        // Try silent token first
        List<IAccount> accounts = app.getAccounts().join();
        if (!accounts.isEmpty()) {
            SilentParameters silentParams = SilentParameters.builder(SCOPES, accounts.get(0)).build();
            try {
                result = app.acquireTokenSilently(silentParams).join();
            } catch (Exception e) {
                System.out.println("⚠️ Silent token acquisition failed: " + e.getMessage());
            }
        }

        // If silent failed → start device flow
        if (result == null) {
            DeviceCodeFlowParameters parameters = DeviceCodeFlowParameters.builder(
                    SCOPES,
                    (DeviceCode deviceCode) -> {
                        System.out.println("Device flow response from Azure: " + deviceCode.message());
                        System.out.println("\n✅ Go to " + deviceCode.verificationUri() + " and enter the code: " + deviceCode.userCode() + "\n");
                    }
            ).build();

            CompletableFuture<IAuthenticationResult> future = app.acquireToken(parameters);
            result = future.join();
        }

        // Save updated token cache
        saveCache();

        if (result == null || result.accessToken() == null) {
            throw new RuntimeException("❌ Failed to obtain access token");
        }

        return result.accessToken();
    }
}
