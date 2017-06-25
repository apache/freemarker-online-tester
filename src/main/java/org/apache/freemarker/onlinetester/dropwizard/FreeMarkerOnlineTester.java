package org.apache.freemarker.onlinetester.dropwizard;


import org.apache.freemarker.onlinetester.healthchecks.FreeMarkerOnlineTesterOverallHealthCheck;
import org.apache.freemarker.onlinetester.resources.ExecuteApiResource;
import org.apache.freemarker.onlinetester.resources.WebPageResource;
import org.apache.freemarker.onlinetester.services.FreeMarkerService;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;

public class FreeMarkerOnlineTester extends Application<FreeMarkerOnlineTesterConfiguration> {

    public static void main(String[] args) throws Exception {
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
        bootstrap.addBundle(new ViewBundle<>());
        bootstrap.addBundle(new AssetsBundle());
    }
}
