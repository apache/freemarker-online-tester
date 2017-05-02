package org.apache.freemarker.onlinetester.dropwizard;


import com.codahale.metrics.health.HealthCheck;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import org.apache.freemarker.onlinetester.spring.SpringConfiguration;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;


import javax.ws.rs.Path;
import java.util.Map;

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
        AnnotationConfigWebApplicationContext context = initializeSpringContext();


        Map<String, Object> resources = context.getBeansWithAnnotation(Path.class);
        for(Map.Entry<String,Object> entry : resources.entrySet()) {
            environment.jersey().register(entry.getValue());
        }

        Map<String, HealthCheck> healthChecks = context.getBeansOfType(HealthCheck.class);
        for(Map.Entry<String,HealthCheck> entry : healthChecks.entrySet()) {
            environment.healthChecks().register("", entry.getValue());
        }
    }

    private AnnotationConfigWebApplicationContext initializeSpringContext() {
        AnnotationConfigWebApplicationContext springContext = new AnnotationConfigWebApplicationContext();
        springContext.register(SpringConfiguration.class);
        springContext.refresh();
        springContext.start();
        return springContext;
    }

    @Override
    public void initialize(Bootstrap<FreeMarkerOnlineTesterConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
        bootstrap.addBundle(new AssetsBundle());
    }
}
