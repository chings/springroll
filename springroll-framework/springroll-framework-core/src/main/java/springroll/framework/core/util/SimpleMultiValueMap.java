package springroll.framework.core.util;

import org.springframework.util.LinkedMultiValueMap;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class SimpleMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> {

    public void forEachOne(BiConsumer<? super K, ? super V> consumer) {
        for(Entry<K, List<V>> entry : this.entrySet()) {
            K key = entry.getKey();
            for(Iterator<V> it = entry.getValue().iterator(); it.hasNext(); ) {
                V value = it.next();
                consumer.accept(key, value);
            }
        }
    }

    public void findAndRemove(BiPredicate<? super K, ? super V> predicate) {
        for(Entry<K, List<V>> entry : this.entrySet()) {
            K key = entry.getKey();
            for(Iterator<V> it = entry.getValue().iterator(); it.hasNext(); ) {
                V value = it.next();
                if(predicate.test(key, value)) it.remove();
            }
        }
    }

}
