package springroll.framework.core;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Coordinator {

    void provide(String actorPath);
    void unprovide(String actorPath);
    void unprovideAll(String host);

    void synchronize(Consumer<List<String>> handler);
    void listenProvide(Consumer<String> handler);
    void listenUnProvide(Consumer<String> handler);
    void listenUnProvideAll(Consumer<String> handler);

}
