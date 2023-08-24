package ru.practicum.shareit.common.convert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListConverter {

    public static <K, V> Map<K, V> keyToValue(List<PairToReturn<K, V>> list) {
        Map<K, V> map = new HashMap<>();
        for (PairToReturn<K, V> entry : list) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static <K, V> Map<K, List<V>> keyToValues(List<PairToReturn<K, V>> list) {
        Map<K, List<V>> map = new HashMap<>();
        for (PairToReturn<K, V> entry : list) {
            K key = entry.getKey();
            V value = entry.getValue();
            if (map.containsKey(key)) {
                map.get(key).add(value);
            } else {
                List<V> values = new ArrayList<>();
                values.add(value);
                map.put(key, values);
            }
        }
        return map;
    }
}
