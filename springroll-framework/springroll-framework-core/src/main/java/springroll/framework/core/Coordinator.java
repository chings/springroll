package springroll.framework.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Coordinator {

    void provide(String actorPath, String actorClassName);

    void unprovide(String actorPath);
    void unprovide();

    void listenProvide(BiConsumer<String, String> listener);
    void unlistenProvide(BiConsumer<String, String> listener);

    void listenUnprovide(Consumer<String> listener);
    void unlistenUnprovide(Consumer<String> listener);

    void synchronize(BiConsumer<String, String> listener);

}
