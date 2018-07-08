package springroll.framework.core.util;

import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleMultiValueMap<K, V> extends LinkedMultiValueMap<K, V> {

    public void forEachValue(BiFunction<? super K, ? super V, Boolean> action) {
        for(Entry<K, List<V>> entry : this.entrySet()) {
           K key = entry.getKey();
           for(Iterator<V> it = entry.getValue().iterator(); it.hasNext(); ) {
               V value = it.next();
               if(action.apply(key, value)) it.remove();
           }
        }
    }

    public V getOne(K key, Function<List<V>, Integer> elector) {
        List<V> candinates = get(key);
        if(CollectionUtils.isEmpty(candinates)) return null;
        return candinates.get(elector.apply(candinates));
    }

    public void removeValues(V value) {
        forEachValue((k, v) -> v.equals(value));
    }

}
