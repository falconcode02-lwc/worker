package io.falconFlow.services.ai.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Minimal WebSocket-based MCP client that sends an initial JSON payload and dispatches
 * incoming text messages to the provided consumer. This is intentionally simple — for
 * production use consider a robust reactive client and better lifecycle management.
 */
public class WebSocketMCPClient {

    private final ObjectMapper mapper = new ObjectMapper();

    public void stream(String url, String model, String prompt, Map<String,Object> meta, Consumer<String> onMessage) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        CountDownLatch latch = new CountDownLatch(1);

        WebSocket.Listener listener = new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                WebSocket.Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                try {
                    onMessage.accept(data.toString());
                } catch (Exception e) {
                    // swallow
                }
                return WebSocket.Listener.super.onText(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                return WebSocket.Listener.super.onBinary(webSocket, data, last);
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                latch.countDown();
                return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                latch.countDown();
            }
        };

        WebSocket ws = client.newWebSocketBuilder().buildAsync(URI.create(url), listener).join();

        // prepare initial payload
        Map<String,Object> body = Map.of(
                "model", model == null ? "" : model,
                "input", prompt == null ? "" : prompt,
                "meta", meta == null ? Map.of() : meta
        );

        String payload = mapper.writeValueAsString(body);
        ws.sendText(payload, true);

        // Wait for close or a short timeout — caller controls when to close via onMessage semantics
        // Here we wait up to 10 minutes for the session to end; in real uses make configurable.
        latch.await(10, TimeUnit.MINUTES);
        try { ws.sendClose(WebSocket.NORMAL_CLOSURE, "done").join(); } catch (Exception ignored) {}
    }
}
