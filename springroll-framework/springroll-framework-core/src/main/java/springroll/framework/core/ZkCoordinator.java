package springroll.framework.core;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ZkCoordinator implements Coordinator, InitializingBean, DisposableBean {
    private static Logger log = LoggerFactory.getLogger(ZkCoordinator.class);

    public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    String connectionString = "localhost:2181";
    String rootPath = "/springroll/actors";
    String nodeNamePrefix = "A";

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    CuratorFramework client;
    TreeCache cache;

    Map<String, String> providedActorPaths = new HashMap<>();
    List<BiConsumer<String, String>> provideListeners = new ArrayList<>();
    List<Consumer<String>> unprovideListeners = new ArrayList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        client.start();
        ensure(rootPath);
        cache = TreeCache.newBuilder(client, rootPath).setCacheData(true).build();
        cache.getListenable().addListener((client, event) -> {
            log.debug("incoming event: {}", event);
            ChildData childData = event.getData();
            if(childData == null) return;
            if(!childData.getPath().startsWith(rootPath + "/" + nodeNamePrefix)) return;
            switch(event.getType()) {
                case NODE_ADDED:
                    String[] tuple = parse(new String(childData.getData(), DEFAULT_CHARSET));
                    for(BiConsumer<String, String> provideListener : provideListeners) {
                        provideListener.accept(tuple[0], tuple[1]);
                    }
                    break;
                case NODE_REMOVED:
                    tuple = parse(new String(childData.getData(), DEFAULT_CHARSET));
                    for(Consumer<String> unprovideListener : unprovideListeners) {
                        unprovideListener.accept(tuple[0]);
                    }
                    break;
            }
        });
        cache.start();
    }

    void ensure(String nodePath) {
        String[] nodeNames = nodePath.split("/");
        String path = "";
        for (String nodeName : nodeNames) {
            if(StringUtils.isEmpty(nodeName)) continue;
            path += "/" + nodeName;
            try {
                if(client.checkExists().forPath(path) == null) {
                    client.create().withMode(CreateMode.PERSISTENT).forPath(path);
                }
            } catch(Exception x) {
                log.error("Ugh! {}", x.getMessage(), x);
            }
        }
    }

    @Override
    public void provide(String actorPath, String actorClassName) {
        try {
            String data = build(actorPath, actorClassName);
            String nodePath = client.create()
                    .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                    .forPath(rootPath + "/" + nodeNamePrefix, data.getBytes(DEFAULT_CHARSET));
            providedActorPaths.put(actorPath, nodePath);
        } catch(Exception x) {
            log.error("Ugh! {}", x.getMessage(), x);
        }
    }

    @Override
    public void unprovide(String actorPath) {
        String nodePath = providedActorPaths.remove(actorPath);
        if(StringUtils.isEmpty(nodePath)) return;
        try {
            client.delete().forPath(nodePath);
        } catch(Exception x) {
            log.error("Ugh! {}", x.getMessage(), x);
        }
    }

    @Override
    public void unprovide() {
        for(String nodePath : providedActorPaths.values()) {
            try {
                client.delete().forPath(nodePath);
            } catch(Exception x) {
                log.error("Ugh! {}", x.getMessage(), x);
            }
        }
        providedActorPaths.clear();
    }

    @Override
    public void listenProvide(BiConsumer<String, String> listener) {
        provideListeners.add(listener);
    }

    @Override
    public void unlistenProvide(BiConsumer<String, String> listener) {
        provideListeners.remove(listener);
    }

    @Override
    public void listenUnprovide(Consumer<String> listener) {
        unprovideListeners.add(listener);
    }

    @Override
    public void unlistenUnprovide(Consumer<String> listener) {
        unprovideListeners.remove(listener);
    }

    @Override
    public void synchronize() {
        for(ChildData childData : cache.getCurrentChildren(rootPath).values()) {
            String[] tuple = parse(new String(childData.getData(), DEFAULT_CHARSET));
            for(BiConsumer<String, String> provideListener : provideListeners) {
                provideListener.accept(tuple[0], tuple[1]);
            }
        }
    }

    @Override
    public void destroy() {
        unprovide();
        cache.close();
        client.close();
    }

    String build(String... data) {
        StringBuilder result = new StringBuilder();
        for(String datum : data) {
            if(result.length() > 0) result.append('\n');
            result.append(datum);
        }
        return result.toString();
    }

    String[] parse(String data) {
        return data.split("\n");
    }

}
