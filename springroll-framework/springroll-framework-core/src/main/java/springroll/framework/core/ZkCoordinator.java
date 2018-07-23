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
import java.util.*;
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

    Map<String, String> providedNodes = new HashMap<>();
    List<BiConsumer<String, Object[]>> provideListeners = new ArrayList<>();
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
                case INITIALIZED:
                    cache.notify();
                    break;
                case NODE_ADDED:
                case NODE_UPDATED:
                    String[] tuple = split(new String(childData.getData(), DEFAULT_CHARSET));
                    for(BiConsumer<String, Object[]> provideListener : provideListeners) {
                        provideListener.accept(tuple[0], Arrays.copyOfRange(tuple, 1, tuple.length));
                    }
                    break;
                case NODE_REMOVED:
                    tuple = split(new String(childData.getData(), DEFAULT_CHARSET));
                    for(Consumer<String> unprovideListener : unprovideListeners) {
                        unprovideListener.accept(tuple[0]);
                    }
                    break;
            }
        });
        cache.start();
        synchronized(cache) {
            while(cache.getCurrentChildren(rootPath) == null) {
                cache.wait();
            }
        }
    }

    void ensure(String nodePath) {
        String[] nodeNames = nodePath.split("/");
        String path = "";
        for(String nodeName : nodeNames) {
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
    public void provide(String actorPath, Object... data) {
        try {
            byte[] payload = join(actorPath, data).getBytes(DEFAULT_CHARSET);
            String nodePath = providedNodes.get(actorPath);
            if(nodePath != null) {
                client.setData().forPath(nodePath, payload);
                return;
            }
            nodePath = client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(rootPath + "/" + nodeNamePrefix, payload);
            providedNodes.put(actorPath, nodePath);
        } catch(Exception x) {
            log.error("Ugh! {}", x.getMessage(), x);
        }
    }

    @Override
    public void unprovide(String actorPath) {
        String nodePath = providedNodes.remove(actorPath);
        if(StringUtils.isEmpty(nodePath)) return;
        try {
            client.delete().forPath(nodePath);
        } catch(Exception x) {
            log.error("Ugh! {}", x.getMessage(), x);
        }
    }

    @Override
    public void unprovide() {
        for(String nodePath : providedNodes.values()) {
            try {
                client.delete().forPath(nodePath);
            } catch(Exception x) {
                log.error("Ugh! {}", x.getMessage(), x);
            }
        }
        providedNodes.clear();
    }

    @Override
    public void listenProvide(BiConsumer<String, Object[]> listener) {
        provideListeners.add(listener);
    }

    @Override
    public void unlistenProvide(BiConsumer<String, Object[]> listener) {
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
    public void synchronize(BiConsumer<String, Object[]> provideListener) {
        Map<String, ChildData> rootNodes = cache.getCurrentChildren(rootPath);
        for(ChildData childData : rootNodes.values()) {
            String[] tuple = split(new String(childData.getData(), DEFAULT_CHARSET));
            provideListener.accept(tuple[0], Arrays.copyOfRange(tuple, 1, tuple.length));
        }
    }

    @Override
    public void destroy() {
        unprovide();
        cache.close();
        client.close();
    }

    static String join(String actorPath, Object... data) {
        StringBuilder result = new StringBuilder(actorPath);
        for(Object datum : data) {
            result.append('\n');
            result.append(datum);
        }
        return result.toString();
    }

    static String[] split(String data) {
        return data.split("\n");
    }

}
