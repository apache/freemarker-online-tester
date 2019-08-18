/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.freemarker.onlinetester.dropwizard;

import java.util.Map;

import org.apache.freemarker.onlinetester.healthchecks.FreeMarkerOnlineTesterOverallHealthCheck;
import org.apache.freemarker.onlinetester.resources.ExecuteApiResource;
import org.apache.freemarker.onlinetester.resources.WebPageResource;
import org.apache.freemarker.onlinetester.services.FreeMarkerService;

import com.google.common.collect.ImmutableMap;

import io.dropwizard.Application;
import io.dropwizard.bundles.assets.ConfiguredAssetsBundle;
import io.dropwizard.bundles.redirect.RedirectBundle;
import io.dropwizard.bundles.redirect.UriRedirect;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.sslreload.SslReloadBundle;
import io.dropwizard.views.ViewBundle;

public class FreeMarkerOnlineTester extends Application<FreeMarkerOnlineTesterConfiguration> {

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("At least 2 command line arguments are needed. Typical arguments: " +
                    "server <path-of-freemarker.online.yml>");
            System.exit(1);
        }
        new FreeMarkerOnlineTester().run(args);
    }

    @Override
    public String getName() {
        return "freemarker-online";
    }

    @Override
    public void run(FreeMarkerOnlineTesterConfiguration configuration, Environment environment) throws Exception {
        FreeMarkerService service = new FreeMarkerService.Builder().build();
        environment.jersey().register(new ExecuteApiResource(service));
        environment.jersey().register(new WebPageResource());
        environment.healthChecks().register("overall", new FreeMarkerOnlineTesterOverallHealthCheck());
    }

    @Override
    public void initialize(Bootstrap<FreeMarkerOnlineTesterConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<FreeMarkerOnlineTesterConfiguration>() {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(FreeMarkerOnlineTesterConfiguration config) {
                return config.getViewRendererConfiguration();
            }        	
        });
        bootstrap.addBundle(new SslReloadBundle());
        bootstrap.addBundle(new ConfiguredAssetsBundle(
        		ImmutableMap.of(
        				"/assets/", "/assets/", // css, js, images...
        				"/override-me/", "/.well-known/acme-challenge/" // Map to a file outside the jar in the yml!
        				)));
        bootstrap.addBundle(new RedirectBundle(
                new UriRedirect(
                        "http://freemarker-online.kenshoo.com(?::\\d+)?(/.*)$",
                        "https://try.freemarker.apache.org$1"),
                new UriRedirect(
                        "http://try.freemarker.org(?::\\d+)?(/.*)$",
                        "https://try.freemarker.apache.org$1"),
                new UriRedirect(
                        "http://try.freemarker.apache.org((:\\d+)?/(?!\\.well-known/acme-challenge/).*)$",
                        "https://try.freemarker.apache.org$1")
        ));
    }
}
