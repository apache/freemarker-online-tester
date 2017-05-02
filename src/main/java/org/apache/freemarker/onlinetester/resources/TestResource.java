package org.apache.freemarker.onlinetester.resources;

import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test")
@Component
public class TestResource {
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String blankForm() {
        return "Hello";
    }
}
