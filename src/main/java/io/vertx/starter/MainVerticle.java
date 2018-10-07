package io.vertx.starter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class MainVerticle extends AbstractVerticle {
	
	List<Adjective> adjectives=new ArrayList<Adjective>();

  @Override
  public void start(Future<Void> fut) {
	  
	  createSomeData();
	  Router router = Router.router(vertx);

      // Bind "/" to our hello message - so we are still compatible.
      router.route("/").handler(routingContext -> {
          HttpServerResponse response = routingContext.response();
          response
              .putHeader("content-type", "text/html")
              .end("<h1>Hello from my first Vert.x 3 application</h1>");
      });
      // Serve static resources from the /assets directory
      
      
      router.get("/api/adjective").handler(this::getAdjective);
      router.route("/api/adjective*").handler(BodyHandler.create());
	  router.get("/health").handler(rc -> rc.response().end("status.:.UP"));
      

      ConfigRetriever retriever = ConfigRetriever.create(vertx);
      retriever.getConfig(
          config -> {
              if (config.failed()) {
                  fut.fail(config.cause());
              } else {
                  // Create the HTTP server and pass the "accept" method to the request handler.
                  vertx
                      .createHttpServer()
                      .requestHandler(router::accept)
                      .listen(
                          // Retrieve the port from the configuration,
                          // default to 8080.
                          config.result().getInteger("HTTP_PORT", 8080),
                          result -> {
                              if (result.succeeded()) {
                                  fut.complete();
                              } else {
                                  fut.fail(result.cause());
                              }
                          }
                      );
              }
          }
      );
    
    
  }
  
  private void createSomeData()
  {
	  Future<String> future = Future.future();
	  
	  
	  
	 /* ConfigStoreOptions file = new ConfigStoreOptions()
			  .setType("file")
			  .setFormat("txt")
			  .setConfig(new JsonObject().put("path", "path-to-file.properties"));
	  
	  InputStream confFile = getClass().getResourceAsStream("/local_file.json");*/
	  System.out.println("started: ");
	  
	  vertx.fileSystem().readFile("adjectives.txt", handler -> {
			if (handler.result().length()>0) {
				
				Buffer buffer= handler.result();
				
				
				RecordParser recordParser = RecordParser.newDelimited("\n", buffer1 -> {
					  // Do something per line, this gets called per line successfully
					   
					if(handler.result().length()>0)
					{
						
						
						
						System.out.println("Read line: " +buffer1.toString());
						
						Adjective adj=new Adjective(buffer1.toString());
						
						adjectives.add(adj);
						
					}
					
					});
				
				
				recordParser.handle(handler.result());
				
				future.complete("read success");
				
				
				
			} else {
				System.out.println("Error while reading from file: " + handler.cause().getMessage());
				future.fail(handler.cause());
			}
		});
  }
  
  private void getAdjective(RoutingContext routingContext) {
	  
	  
	 

		// Retrieve a FileSystem object from vertx instance and call the
		// non-blocking readFile method
	  try {
      Adjective adj=adjectives.get(new Random().nextInt(adjectives.size()));
      if (adj == null) {
          // Not found
          routingContext.response().setStatusCode(404).end();
      } else {
      routingContext.response()
      .setStatusCode(200)
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(Json.encodePrettily(adj));
      }
	  }catch (NumberFormatException e) {
          routingContext.response().setStatusCode(400).end();
      }
  }

}
