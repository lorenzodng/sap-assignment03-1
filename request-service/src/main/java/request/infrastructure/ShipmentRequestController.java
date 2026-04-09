package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import request.application.CreateShipmentRequest;
import request.application.ShipmentRequestOrchestrator;
import request.application.ValidateShipmentRequest;
import request.domain.Package;
import request.domain.Position;
import request.domain.Shipment;
import request.domain.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//controller che riceve le richieste dal client
@Adapter
public class ShipmentRequestController {

    private static final Logger log = LoggerFactory.getLogger(ShipmentRequestController.class);
    private final ShipmentRequestOrchestrator orchestrator;

    public ShipmentRequestController(ShipmentRequestOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    //registra la rotta
    public void registerRoutes(Router router) {
        router.post("/shipments").handler(BodyHandler.create()).handler(this::createShipment); //quando arriva una richiesta sulla rotta "/shipments", invoca il metodo
    }

    //crea la richiesta e invoca il broker kafka
    private void createShipment(RoutingContext ctx) {
        var body = ctx.body().asJsonObject();
        var pickup = body.getJsonObject("pickupLocation");
        var delivery = body.getJsonObject("deliveryLocation");
        var pkg = body.getJsonObject("package");

        orchestrator.orchestrateRequest(body.getString("userId"), body.getString("userName"), body.getString("userSurname"), pickup.getDouble("latitude"), pickup.getDouble("longitude"), delivery.getDouble("latitude"), delivery.getDouble("longitude"), body.getString("pickupDate"), body.getString("pickupTime"), body.getInteger("deliveryTimeLimit"), pkg.getDouble("weight"), pkg.getBoolean("fragile"))
                .onSuccess(shipment -> {
                    ctx.response().setStatusCode(201).putHeader("Content-Type", "application/json").end(shipment.getId());
                })
                .onFailure(err -> {
                    if ("VALIDATION_FAILED".equals(err.getMessage())) {
                        ctx.response().setStatusCode(400).end("Invalid request");
                    } else {
                        ctx.response().setStatusCode(500).end("Internal Server Error");
                    }
                });
    }
}