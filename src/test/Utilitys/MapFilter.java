package test.Utilitys;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// 반드시 참조용으로만 사용할것 (외부에서 원본 객체의 수정x)
public class MapFilter {

    private final List<Map<String, Object>> list;
    private final List<Predicate<Map<String, Object>>> predicates = new ArrayList<>();

    private MapFilter(List<Map<String, Object>> list) {
        this.list = Optional.ofNullable(list)
                .map(ArrayList::new)   // shallow copy
                .orElseGet(ArrayList::new);
    }

    public static MapFilter of(List<Map<String, Object>> list) {
        return new MapFilter(list);
    }

    public MapFilter notNull(String key) {
        predicates.add(row -> row.containsKey(key) && row.get(key) != null);
        return this;
    }

    public MapFilter isNull(String key) {
        predicates.add(row -> !row.containsKey(key) || row.get(key) == null);
        return this;
    }

    public MapFilter eq(String key, String value) {
        predicates.add(row ->
                Objects.equals(row.get(key), value)
        );
        return this;
    }

    public MapFilter eq(String key, Object value) {
        predicates.add(row ->
                Objects.equals(row.get(key), value)
        );
        return this;
    }


    // not equals
    public MapFilter ne(String key, Object value) {
        predicates.add(row ->
                !Objects.equals(row.get(key), value)
        );
        return this;
    }

    // contains
    public MapFilter contains(String key, String value) {
        predicates.add(row -> {
            Object rowVal = row.get(key);

            if (rowVal == null || value == null) {
                return false;
            }

            return rowVal.toString().contains(value);
        });

        return this;
    }

    // startsWith
    public MapFilter startsWith(String key, String value) {
        predicates.add(row -> {
            Object rowVal = row.get(key);

            if (rowVal == null || value == null) {
                return false;
            }

            return rowVal.toString().startsWith(value);
        });

        return this;
    }

    // endsWith
    public MapFilter endsWith(String key, String value) {
        predicates.add(row -> {
            Object rowVal = row.get(key);

            if (rowVal == null || value == null) {
                return false;
            }

            return rowVal.toString().endsWith(value);
        });

        return this;
    }


    public List<Map<String, Object>> toList() {
        return list.stream()
                .filter(this::matches)
                .collect(Collectors.toList())
                ;
    }

    public Map<String, Object> findFirst() {
        return list.stream()
                .filter(this::matches)
                .findFirst()
                .orElse(null);
    }

    private boolean matches(Map<String, Object> row) {
        for (Predicate<Map<String, Object>> predicate : predicates) {
            if (!predicate.test(row)) {
                return false;
            }
        }
        return true;
    }
}