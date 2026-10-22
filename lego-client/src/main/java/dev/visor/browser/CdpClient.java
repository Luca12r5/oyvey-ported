package dev.visor.browser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

/**
 * A small Chrome DevTools Protocol client: JSON-RPC over one WebSocket, with "flat" sessions
 * (page commands carry a {@code sessionId}). Events are delivered on the socket thread.
 */
final class CdpClient implements WebSocket.Listener {
	record Event(String method, JsonObject params, @Nullable String sessionId) {
	}

	/** A command the browser answered with an error. */
	static final class CdpException extends RuntimeException {
		CdpException(String method, String message) {
			super(method + ": " + message);
		}
	}

	private static final HttpClient HTTP = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(5))
		.executor(Executors.newCachedThreadPool(runnable -> {
			Thread thread = new Thread(runnable, "Visor Browser IO");
			thread.setDaemon(true);
			return thread;
		}))
		.build();

	private final Consumer<Event> events;
	private final Runnable onClosed;
	private final AtomicInteger ids = new AtomicInteger();
	private final Map<Integer, Pending> pending = new ConcurrentHashMap<>();
	private final StringBuilder incoming = new StringBuilder();
	private @Nullable WebSocket socket;
	private CompletableFuture<?> sendChain = CompletableFuture.completedFuture(null);
	private volatile boolean closed;

	private record Pending(String method, CompletableFuture<JsonObject> future) {
	}

	private CdpClient(Consumer<Event> events, Runnable onClosed) {
		this.events = events;
		this.onClosed = onClosed;
	}

	static CdpClient connect(URI uri, Consumer<Event> events, Runnable onClosed) throws Exception {
		CdpClient client = new CdpClient(events, onClosed);
		client.socket = HTTP.newWebSocketBuilder()
			.connectTimeout(Duration.ofSeconds(5))
			.buildAsync(uri, client)
			.get(10, TimeUnit.SECONDS);
		return client;
	}

	boolean isOpen() {
		return !this.closed;
	}

	/** Sends a command; the future completes with the command's {@code result} object. */
	synchronized CompletableFuture<JsonObject> send(String method, @Nullable JsonObject params, @Nullable String sessionId) {
		CompletableFuture<JsonObject> future = new CompletableFuture<>();
		WebSocket ws = this.socket;
		if (this.closed || ws == null) {
			future.completeExceptionally(new CdpException(method, "not connected"));
			return future;
		}
		int id = this.ids.incrementAndGet();
		JsonObject message = new JsonObject();
		message.addProperty("id", id);
		message.addProperty("method", method);
		message.add("params", params != null ? params : new JsonObject());
		if (sessionId != null) {
			message.addProperty("sessionId", sessionId);
		}
		this.pending.put(id, new Pending(method, future));
		String text = message.toString();
		// A WebSocket accepts one outgoing message at a time: chain the sends.
		this.sendChain = this.sendChain
			.exceptionally(error -> null)
			.thenCompose(ignored -> ws.sendText(text, true))
			.whenComplete((result, error) -> {
				if (error != null) {
					Pending failed = this.pending.remove(id);
					if (failed != null) {
						failed.future().completeExceptionally(error);
					}
				}
			});
		return future;
	}

	void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
		WebSocket ws = this.socket;
		if (ws != null) {
			ws.abort();
		}
		this.failPending();
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		webSocket.request(1);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		this.incoming.append(data);
		if (last) {
			String text = this.incoming.toString();
			this.incoming.setLength(0);
			try {
				this.dispatch(JsonParser.parseString(text).getAsJsonObject());
			} catch (RuntimeException e) {
				dev.visor.VisorClient.LOGGER.debug("Bad DevTools message", e);
			}
		}
		webSocket.request(1);
		return null;
	}

	private void dispatch(JsonObject message) {
		if (message.has("id")) {
			Pending done = this.pending.remove(message.get("id").getAsInt());
			if (done == null) {
				return;
			}
			if (message.has("error")) {
				JsonObject error = message.getAsJsonObject("error");
				JsonElement text = error.get("message");
				done.future().completeExceptionally(new CdpException(done.method(), text != null ? text.getAsString() : error.toString()));
			} else {
				JsonElement result = message.get("result");
				done.future().complete(result != null && result.isJsonObject() ? result.getAsJsonObject() : new JsonObject());
			}
			return;
		}
		JsonElement method = message.get("method");
		if (method == null) {
			return;
		}
		JsonElement params = message.get("params");
		JsonElement session = message.get("sessionId");
		this.events.accept(new Event(
			method.getAsString(),
			params != null && params.isJsonObject() ? params.getAsJsonObject() : new JsonObject(),
			session != null ? session.getAsString() : null
		));
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		this.shutDown();
		return null;
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		this.shutDown();
	}

	private void shutDown() {
		boolean wasOpen = !this.closed;
		this.closed = true;
		this.failPending();
		if (wasOpen) {
			this.onClosed.run();
		}
	}

	private void failPending() {
		for (Pending entry : this.pending.values()) {
			entry.future().completeExceptionally(new CdpException(entry.method(), "connection closed"));
		}
		this.pending.clear();
	}
}
