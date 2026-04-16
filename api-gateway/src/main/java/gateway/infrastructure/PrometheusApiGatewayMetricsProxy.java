package gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import gateway.application.ApiGatewayMetrics;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;

//raccoglie le metriche di richiesta spedizione e tracking
@Adapter
public class PrometheusApiGatewayMetricsProxy implements ApiGatewayMetrics {

    private final Counter totalRequests;
    private final HTTPServer server;

    public PrometheusApiGatewayMetricsProxy(int port) throws Exception {
        totalRequests = Counter.builder().name("gateway_shipments_requests_total").help("Total number of REST requests received").labelNames("endpoint", "method", "status").register(); //metriche del numero di richieste di creazione spedizione e numero di richieste di tacking spedizione - status distingue quelle "buone" da quelle "cattive"
        server = HTTPServer.builder().port(port).buildAndStart(); //espone le metriche su una porta dedicata
    }

    //incrementa la metrica
    @Override
    public void incrementRequest(String path, String method, int statusCode) {
        totalRequests.labelValues(path, method, String.valueOf(statusCode)).inc(); //i parametri sono le informazioni mostrate (path per il microservizio e method per il tipo di richiesta http)
    }

    //ferma il server e libera la porta
    public void stop() {
        server.close();
    }

}