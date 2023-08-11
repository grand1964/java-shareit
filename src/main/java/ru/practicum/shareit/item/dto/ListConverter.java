package ru.practicum.shareit.item.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListConverter {

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> pairsToItems(List<Object[]> list) {
        Map<K, V> map = new HashMap<>();
        for (Object[] entry : list) {
            map.put((K) entry[0], (V) entry[1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, List<V>> pairsToMap(List<Object[]> list) {
        Map<K, List<V>> map = new HashMap<>();
        for (Object[] entry : list) {
            K key = (K) entry[0];
            V value = (V) entry[1];
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
