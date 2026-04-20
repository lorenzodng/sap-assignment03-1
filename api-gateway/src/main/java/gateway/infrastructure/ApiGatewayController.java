package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayMetrics;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

@Adapter
public class ApiGatewayController {

    private final WebClient client;
    private final String requestManagementUrl;
    private final String deliveryManagementUrl;
    private final ApiGatewayMetrics metrics;

    public ApiGatewayController(Vertx vertx, String requestManagementUrl, String deliveryManagementUrl, ApiGatewayMetrics metrics) {
        this.client = WebClient.create(vertx);
        this.requestManagementUrl = requestManagementUrl;
        this.deliveryManagementUrl = deliveryManagementUrl;
        this.metrics = metrics;
    }

    public void registerRoutes(Router router) {
        router.post("/shipments").handler(BodyHandler.create()).handler(this::createShipment);
        router.get("/shipments/:id/status").handler(this::getShipmentStatus);
        router.get("/shipments/:id/position").handler(this::getDronePosition);
        router.get("/shipments/:id/remaining-time").handler(this::getRemainingTime);
    }

    private void createShipment(RoutingContext ctx) {
        client.postAbs(requestManagementUrl + "/shipments").putHeader("Content-Type", "application/json").sendBuffer(Buffer.buffer(ctx.body().asString()))
                .onSuccess(response -> {
                    metrics.incrementRequest("/shipments", "POST", response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
                })
                .onFailure(err -> {
                    metrics.incrementRequest("/shipments", "POST", 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }

    private void getShipmentStatus(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryManagementUrl + "/shipments/" + id + "/status").send()
                .onSuccess(response -> {
                    metrics.incrementRequest("/shipments/:id/status", "GET", response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
                })
                .onFailure(err -> {
                    metrics.incrementRequest("/shipments/:id/status", "GET", 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }

    private void getDronePosition(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryManagementUrl + "/shipments/" + id + "/position").send()
                .onSuccess(response -> {
                    metrics.incrementRequest("/shipments/:id/position", "GET", response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
                })
                .onFailure(err -> {
                    metrics.incrementRequest("/shipments/:id/position", "GET", 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }

    private void getRemainingTime(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryManagementUrl + "/shipments/" + id + "/remaining-time").send()
                .onSuccess(response -> {
                    metrics.incrementRequest("/shipments/:id/remaining-time", "GET", response.statusCode());
                    ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
                })
                .onFailure(err -> {
                    metrics.incrementRequest("/shipments/:id/remaining-time", "GET", 500);
                    ctx.response().setStatusCode(500).end("Error forwarding request");
                });
    }
}