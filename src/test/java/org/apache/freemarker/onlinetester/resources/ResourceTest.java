package org.apache.freemarker.onlinetester.resources;

import org.apache.freemarker.onlinetester.services.FreeMarkerService;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;

import io.dropwizard.testing.junit.ResourceTestRule;

public abstract class ResourceTest {

    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addResource(new ExecuteApiResource(new FreeMarkerService.Builder().build()))
            .build();

}
