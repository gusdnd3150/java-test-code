package test.Utilitys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class MapFilter {

    private final List<Map<String, Object>> list;
    private final List<Predicate<Map<String, Object>>> predicates = new ArrayList<>();

    private MapFilter(List<Map<String, Object>> list) {
        this.list = list;
    }

    public static MapFilter of(List<Map<String, Object>> list) {
        return new MapFilter(list);
    }

    // null 안전하게 수정
    public MapFilter eq(String key, Object value) {
        predicates.add(row -> {
            Object rowVal = row.get(key);
            if (value == null) return rowVal == null;
            return value.equals(rowVal);
        });
        return this;
    }

    public MapFilter contains(String key, String value) {
        predicates.add(row -> {
            Object rowVal = row.get(key);
            if (rowVal == null || value == null) return false;
            return rowVal.toString().contains(value);
        });
        return this;
    }

    public MapFilter notNull(String key) {
        predicates.add(row -> row.get(key) != null);
        return this;
    }

    public MapFilter isNull(String key) {
        predicates.add(row -> row.get(key) == null);
        return this;
    }

    public List<Map<String, Object>> toList() {
        return list.stream()
            .filter(row -> predicates.stream().allMatch(p -> p.test(row)))
            .collect(Collectors.toList());
    }

    public Map<String, Object> findFirst() {
        return list.stream()
            .filter(row -> predicates.stream().allMatch(p -> p.test(row)))
            .findFirst()
                .orElse(null);
    }
}