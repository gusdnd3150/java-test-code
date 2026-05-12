package test.Utilitys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MapFilter {


//    List<Map<String, Object>> result = MapFilter.of(list)
//    .eq("PROCESS_CD", "CAP_ASSEMBLY")
//    .eq("USE_YN", "Y")
//    .notNull("EQUIP_ID")
//    .contains("EQUIP_NM", "IEB")
//    .toList();
//
//// 단건
//     Optional<Map<String, Object>> one = MapFilter.of(list)
//    .eq("EVENT_ID", "EVT_001")
//    .findFirst();

    private final List<Map<String, Object>> list;
    private final List<Predicate<Map<String, Object>>> predicates = new ArrayList<>();

    private MapFilter(List<Map<String, Object>> list) {
        this.list = list;
    }

    public static MapFilter of(List<Map<String, Object>> list) {
        return new MapFilter(list);
    }

    public MapFilter eq(String key, Object value) {
        predicates.add(row -> value.equals(row.get(key)));
        return this;
    }

    public MapFilter contains(String key, String value) {
        predicates.add(row -> row.get(key) != null && row.get(key).toString().contains(value));
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

    // 단건 추출
    public Optional<Map<String, Object>> findFirst() {
        return list.stream()
            .filter(row -> predicates.stream().allMatch(p -> p.test(row)))
            .findFirst();
    }
}