/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */


package com.microsoft.azure.toolkit.lib.auth.core.maven;

import com.azure.core.management.AzureEnvironment;
import com.azure.identity.implementation.util.IdentityConstants;
import com.google.common.base.MoreObjects;
import com.microsoft.azure.toolkit.lib.auth.core.IAccountEntityBuilder;
import com.microsoft.azure.toolkit.lib.auth.core.common.CommonAccountEntityBuilder;
import com.microsoft.azure.toolkit.lib.auth.exception.LoginFailureException;
import com.microsoft.azure.toolkit.lib.auth.model.AccountEntity;
import com.microsoft.azure.toolkit.lib.auth.model.AuthMethod;
import com.microsoft.azure.toolkit.lib.auth.util.AzureEnvironmentUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Arrays;

public class MavenLoginAccountEntityBuilder implements IAccountEntityBuilder {
    @Override
    public AccountEntity build() {
        AccountEntity accountEntity = CommonAccountEntityBuilder.createAccountEntity(AuthMethod.AZURE_SECRET_FILE);
        if (!MavenLoginHelper.existsAzureSecretFile()) {
            return accountEntity;
        }
        try {
            AzureCredential credentials = MavenLoginHelper.readAzureCredentials(MavenLoginHelper.getAzureSecretFile());
            String envString = credentials.getEnvironment();
            AzureEnvironment env = MoreObjects.firstNonNull(AzureEnvironmentUtils.stringToAzureEnvironment(envString), AzureEnvironment.AZURE);
            accountEntity.setEnvironment(env);

            if (StringUtils.isBlank(credentials.getRefreshToken())) {
                throw new LoginFailureException("Missing required 'refresh_token' from file:" + MavenLoginHelper.getAzureSecretFile());
            }

            accountEntity.setSelectedSubscriptionIds(Arrays.asList(credentials.getDefaultSubscription()));
            if (credentials.getUserInfo() != null) {
                accountEntity.setEmail(credentials.getUserInfo().getDisplayableId());
            }

            accountEntity.setCredentialBuilder(CommonAccountEntityBuilder.fromRefreshToken(env, IdentityConstants.DEVELOPER_SINGLE_SIGN_ON_ID,
                    credentials.getRefreshToken()));
            accountEntity.setAuthenticated(true);

        } catch (IOException | LoginFailureException ex) {
            accountEntity.setError(ex);
        }
        return accountEntity;
    }
}