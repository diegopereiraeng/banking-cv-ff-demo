package io.harness.cvdemo.config;

import io.harness.cvdemo.App;
import io.harness.cvdemo.config.beans.Config;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("config")
@Produces(MediaType.APPLICATION_JSON)
public class ConfigResource {

  private ConfigService service = new ConfigService();

  @GET
  public Response getConfig() {
    return Response.ok(App.behaviorGenerator.getConfig()).build();
  }

  @POST
  public Response setConfig(Config config)  {
    return Response.ok(service.setConfig(config)).build();
  }

  @POST
  @Path("start")
  public Response start()  {
    App.behaviorGenerator.startAll();
    return Response.ok(App.behaviorGenerator.getConfig()).build();
  }

  @POST
  @Path("stop")
  public Response stop() {
    App.behaviorGenerator.stopAll();
    return Response.ok(App.behaviorGenerator.getConfig()).build();
  }

  @POST
  @Path("reset")
  public Response reset()  {
    return Response.ok(service.setConfig(App.defaultConfig)).build();
  }
}
