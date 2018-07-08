package springroll.framework.coordinator.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;
import springroll.framework.core.Coordinator;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ZkCoordinator implements Coordinator, InitializingBean, DisposableBean {
    private static Logger log = LoggerFactory.getLogger(ZkCoordinator.class);

    public static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    String connectionString = "localhost:2181";
    String rootPath = "/springroll/actors";

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    CuratorFramework client;
    TreeCache cache;

    Map<String, String> providedActorPaths = new HashMap<>();
    Map<TreeCacheEvent.Type, Consumer<String>> handlers = new HashMap<>();

    static void ensureCreate(CuratorFramework client, String path) {
        String[] nodeNames = path.split("/");
        String p = "";
        for (String nodeName : nodeNames) {
            if(StringUtils.isEmpty(nodeName)) continue;
            p += "/" + nodeName;
            try {
                if(client.checkExists().forPath(p) == null) {
                    client.create().withMode(CreateMode.PERSISTENT).forPath(p);
                }
            } catch(Exception x) {
                log.error("Ugh! {}", x.getMessage(), x);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExponentialBackoffRetry retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.newClient(connectionString, retryPolicy);
        client.getUnhandledErrorListenable().addListener((message, x) -> log.error(message, x));
        client.getConnectionStateListenable().addListener((client, newState) -> log.info("Connection state changed: {}", newState));
        client.start();
        ensureCreate(client, rootPath);
        cache = TreeCache.newBuilder(client, rootPath).setCacheData(true).build();
        cache.getListenable().addListener((client, event) -> {
            log.debug("incoming event: {}", event);
            Consumer<String> handler = handlers.get(event.getType());
            if(handler != null) handler.accept(new String(event.getData().getData(), DEFAULT_CHARSET));
        });
        cache.start();
    }

    @Override
    public void provide(String actorPath) {
        try {
            String nodeName = client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(rootPath + "/A", actorPath.getBytes(DEFAULT_CHARSET));
            providedActorPaths.put(actorPath, nodeName);
        } catch(Exception x) {
            log.error("Ugh! {}", x.getMessage(), x);
        }
    }

    @Override
    public void unprovide(String actorPath) {
        String nodeName = providedActorPaths.remove(actorPath);
        if(StringUtils.isEmpty(nodeName)) return;
        try {
            client.delete().forPath(nodeName);
        } catch(Exception x) {
            log.error("Ugh! {}", x.getMessage(), x);
        }
    }

    @Override
    public void unprovide() {
        for(String nodeName : providedActorPaths.values()) {
            try {
                client.delete().forPath(nodeName);
            } catch(Exception x) {
                log.error("Ugh! {}", x.getMessage(), x);
            }
        }
        providedActorPaths.clear();
    }

    @Override
    public void listenProvide(Consumer<String> handler) {
        handlers.put(TreeCacheEvent.Type.NODE_ADDED, handler);
    }

    @Override
    public void listenUnprovide(Consumer<String> handler) {
        handlers.put(TreeCacheEvent.Type.NODE_REMOVED, handler);
    }

    @Override
    public void synchronize(Consumer<List<String>> handler) {
        List<String> actorPaths = new ArrayList<>();
        Map<String, ChildData> children = cache.getCurrentChildren(rootPath);
        if(children == null) return;
        for(ChildData childData : children.values()) {
            actorPaths.add(new String(childData.getData(), DEFAULT_CHARSET));
        }
        handler.accept(actorPaths);
    }

    @Override
    public void destroy() throws Exception {
        cache.close();
        client.close();
    }

}
