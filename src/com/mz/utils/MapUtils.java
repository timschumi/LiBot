package com.mz.utils;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

public class MapUtils {

	private MapUtils() {}

	/**
	 * Returns all keys with the given value in a map
	 *
	 * @param map
	 * @param value
	 *
	 * @return a list of keys that hold the given value
	 */
	public static <K, V> List<K> getKeyFromMap(Map<K, V> map, V value) {
		return map.entrySet()
		    .stream()
		    .filter(e -> Objects.equals(e.getValue(), value))
		    .map(Entry::getKey)
		    .collect(Collectors.toList());
	}

}
