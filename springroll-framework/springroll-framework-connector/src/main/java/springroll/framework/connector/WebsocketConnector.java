package springroll.framework.connector;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ws.Message;
import akka.http.javadsl.model.ws.WebSocket;
import akka.http.javadsl.settings.ServerSettings;
import akka.http.javadsl.settings.WebSocketSettings;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebsocketConnector implements InitializingBean, DisposableBean {

    ActorSystem actorSystem;

    public WebsocketConnector(ActorSystem actorSystem) {
        this.actorSystem = actorSystem;
    }

    AtomicInteger pingCounter = new AtomicInteger();

    private ByteString keepAlive() {
        return ByteString.fromString(String.format("debug-%d", pingCounter.incrementAndGet()))
    }

    public Flow<Message, Message, NotUsed> flow() {
        return handleMessagesWithSinkSource(null, null);
    }

    public HttpResponse handle(HttpRequest request) {
        return WebSocket.handleWebSocketRequestWith(request, flow());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ServerSettings defaultSettings = ServerSettings.create(actorSystem);
        WebSocketSettings customWebsocketSettings = defaultSettings.getWebsocketSettings()
                .withPeriodicKeepAliveData(this::keepAlive);
        ServerSettings customServerSettings = defaultSettings.withWebsocketSettings(customWebsocketSettings);
        Http http = Http.get(actorSystem);
        CompletionStage<ServerBinding> serverBindingFuture =
                http.bindAndHandleSync(this::handle,
                        ConnectHttp.toHost("0.0.0.0", 8080),
                        customServerSettings,
                        actorSystem.log(),
                        ActorMaterializer.create(actorSystem));
        serverBindingFuture.toCompletableFuture().get(1, TimeUnit.SECONDS);
    }

    @Override
    public void destroy() throws Exception {

    }

}
