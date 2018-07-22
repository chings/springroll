package springroll.framework.core;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Coordinator {

    void provide(String actorPath, Object... data);
    void unprovide(String actorPath);
    void unprovide();

    void listenProvide(BiConsumer<String, Object[]> listener);
    void unlistenProvide(BiConsumer<String, Object[]> listener);

    void listenUnprovide(Consumer<String> listener);
    void unlistenUnprovide(Consumer<String> listener);

    void synchronize(BiConsumer<String, Object[]> listener);

}
