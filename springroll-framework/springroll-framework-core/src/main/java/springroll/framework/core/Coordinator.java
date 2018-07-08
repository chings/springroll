package springroll.framework.core;

import java.util.List;
import java.util.function.Consumer;

public interface Coordinator {

    void provide(String actorPath);
    void unprovide(String actorPath);
    void unprovide();

    void listenProvide(Consumer<String> handler);
    void listenUnprovide(Consumer<String> handler);

    void synchronize(Consumer<List<String>> handler);

}
