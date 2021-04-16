/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.lib.auth.core.cache;

import com.azure.core.management.AzureEnvironment;
import com.azure.identity.SharedTokenCacheCredential;
import com.azure.identity.SharedTokenCacheCredentialBuilder;
import com.azure.identity.TokenCachePersistenceOptions;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.auth.Account;
import com.microsoft.azure.toolkit.lib.auth.AzureCloud;
import com.microsoft.azure.toolkit.lib.auth.RefreshTokenTokenCredentialManager;
import com.microsoft.azure.toolkit.lib.auth.TokenCredentialManager;
import com.microsoft.azure.toolkit.lib.auth.model.AuthType;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class CachedTokenAccount extends Account {
    private String clientId;
    private String tenantId;
    private String username;

    @Override
    public AuthType getAuthType() {
        return AuthType.CACHE;
    }

    @Override
    protected String getClientId() {
        return clientId;
    }

    @Override
    protected Mono<Boolean> preLoginCheck() {
        return Mono.just(true);
    }

    @Override
    protected Mono<TokenCredentialManager> createTokenCredentialManager() {
        SharedTokenCacheCredentialBuilder builder = new SharedTokenCacheCredentialBuilder();
        AzureEnvironment env = Azure.az(AzureCloud.class).getOrDefault();
        SharedTokenCacheCredential credential = builder
                .tokenCachePersistenceOptions(new TokenCachePersistenceOptions().setName(Azure.az().config().getTokenCacheName()))
                .username(username).tenantId(tenantId).clientId(clientId).build();
        return RefreshTokenTokenCredentialManager.createTokenCredentialManager(env, getClientId(), credential);
    }
}
