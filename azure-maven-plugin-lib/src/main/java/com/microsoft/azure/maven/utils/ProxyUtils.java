/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.maven.utils;

import com.azure.core.util.Configuration;
import com.microsoft.azure.common.logging.Log;
import com.microsoft.azure.toolkit.lib.common.utils.TextUtils;
import com.microsoft.azure.toolkit.lib.common.proxy.ProxyManager;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.settings.Proxy;

import java.util.List;
import java.util.Objects;

public class ProxyUtils {
    public static void initProxy(MavenExecutionRequest request) {
        final ProxyManager proxyManager = ProxyManager.getInstance();
        proxyManager.init();
        String source = "system";
        if (!proxyManager.forceUseSystemProxy() && request != null) {
            final List<Proxy> mavenProxies = request.getProxies();
            if (CollectionUtils.isNotEmpty(mavenProxies)) {
                final Proxy mavenProxy = mavenProxies.stream().filter(
                    proxy -> proxy.isActive() && proxy.getPort() > 0 && StringUtils.isNotBlank(proxy.getHost())).findFirst().orElse(null);
                if (mavenProxy != null) {
                    if (StringUtils.isNotBlank(mavenProxy.getUsername()) && StringUtils.isNoneBlank(mavenProxy.getPassword())) {
                        proxyManager.configure(mavenProxy.getHost(), mavenProxy.getPort(),
                                mavenProxy.getUsername(), mavenProxy.getPassword());
                    } else {
                        proxyManager.configure(mavenProxy.getHost(), mavenProxy.getPort());
                    }
                    source = "maven";
                }
            }
        }
        if (source != null && Objects.nonNull(proxyManager.getProxy())) {
            Log.info(String.format("Use %s proxy: %s:%s", source, TextUtils.cyan(proxyManager.getHttpProxyHost()),
                    TextUtils.cyan(Integer.toString(proxyManager.getHttpProxyPort()))));

            Configuration.getGlobalConfiguration().put(Configuration.PROPERTY_HTTP_PROXY,
                    String.format("http://%s:%s", proxyManager.getHttpProxyHost(), proxyManager.getHttpProxyPort()));
        }
    }
}
