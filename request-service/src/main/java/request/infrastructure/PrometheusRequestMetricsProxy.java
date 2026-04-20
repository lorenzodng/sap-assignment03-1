package request.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import request.application.RequestMetrics;

@Adapter
public class PrometheusRequestMetricsProxy implements RequestMetrics {

    private final Histogram requestLatency;
    private final HTTPServer server;

    public PrometheusRequestMetricsProxy(int port) throws Exception {
        requestLatency = Histogram.builder()
                .name("request_orchestration_duration_seconds")
                .help("Time taken to orchestrate a shipment request")
                .classicUpperBounds(0.1, 0.3, 0.5, 1.0, 2.0)
                .register();
        server = HTTPServer.builder().port(port).buildAndStart();
    }

    @Override
    public void observeLatency(double seconds) {
        requestLatency.observe(seconds);
    }

    public void stop() {
        server.close();
    }
}