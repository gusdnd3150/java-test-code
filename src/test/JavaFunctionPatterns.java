package test;

import java.util.*;
import java.util.function.*;

/**
 * Java Function 실무 활용 패턴
 *
 * 1. 전략(Strategy) 패턴   — switch/if 대신 Map<String, Function>
 * 2. 파이프라인             — andThen 체이닝으로 처리 단계 구성
 * 3. 유효성 검사 체인       — Predicate 조합으로 복합 검증
 * 4. 커링(Currying)        — 다인수 함수를 1인수 함수 체인으로 분리
 * 5. 메모이제이션           — computeIfAbsent 로 결과 캐싱
 * 6. Map 함수형 메서드      — compute / merge / replaceAll
 * 7. Optional 연계         — map / filter / ifPresent / orElseGet
 */
public class JavaFunctionPatterns {

    public static void main(String[] args) {
        strategyPattern();
        pipelinePattern();
        validationChain();
        currying();
        memoization();
        mapFunctional();
        optionalFunctional();
    }

    // ── 1. 전략(Strategy) 패턴 ───────────────────────────────────────────────
    // 알고리즘을 Function 으로 주입 → switch/if 블록 제거
    static void strategyPattern() {
        System.out.println("=== 1. 전략 패턴 ===");

        Map<String, Function<Integer, Integer>> discountPolicy = new HashMap<>();
        discountPolicy.put("VIP",    price -> (int)(price * 0.7));  // 30% 할인
        discountPolicy.put("MEMBER", price -> (int)(price * 0.9));  // 10% 할인
        discountPolicy.put("GUEST",  price -> price);                // 할인 없음

        int price = 10_000;
        for (String grade : new String[]{"VIP", "MEMBER", "GUEST"}) {
            int result = discountPolicy.getOrDefault(grade, p -> p).apply(price);
            System.out.printf("%-8s → %,d원%n", grade, result);
        }
        // VIP      → 7,000원
        // MEMBER   → 9,000원
        // GUEST    → 10,000원

        // 새 등급 추가 시 Map 에 한 줄만 추가하면 됨 (OCP 원칙 준수)
        discountPolicy.put("STAFF", price -> (int)(price * 0.5));
        System.out.printf("%-8s → %,d원%n", "STAFF", discountPolicy.get("STAFF").apply(price));
        // STAFF    → 5,000원
    }

    // ── 2. 파이프라인 ────────────────────────────────────────────────────────
    // Function.andThen 으로 처리 단계를 명시적으로 체이닝
    static void pipelinePattern() {
        System.out.println("\n=== 2. 파이프라인 ===");

        Function<String, String>   trim    = String::trim;
        Function<String, String>   toLower = String::toLowerCase;
        Function<String, String[]> split   = s -> s.split("\\s+");
        Function<String[], String> join    = arr -> String.join("-", arr);

        // trim → toLower → split → join 순서로 실행
        Function<String, String> pipeline = trim
            .andThen(toLower)
            .andThen(split)
            .andThen(join);

        System.out.println(pipeline.apply("  Hello World Java  ")); // hello-world-java

        // 단계별 파이프라인 분기: 공통 앞단 + 분기 뒷단
        Function<String, String> normalize = trim.andThen(toLower);
        Function<String, String> slugify   = normalize.andThen(split).andThen(join);
        Function<String, String> initials  = normalize
            .andThen(s -> Arrays.stream(s.split("\\s+"))
                               .map(w -> String.valueOf(w.charAt(0)).toUpperCase())
                               .reduce("", String::concat));

        System.out.println(slugify.apply("  Hello World  "));   // hello-world
        System.out.println(initials.apply("  hello world  "));  // HW
    }

    // ── 3. 유효성 검사 체인 ──────────────────────────────────────────────────
    // Predicate 조합으로 복합 조건 구성, 재사용 단위로 분리
    static void validationChain() {
        System.out.println("\n=== 3. 유효성 검사 체인 ===");

        // 재사용 가능한 단위 검증
        Predicate<String> notNull   = s -> s != null;
        Predicate<String> notEmpty  = s -> !s.isEmpty();
        Predicate<String> minLen4   = s -> s.length() >= 4;
        Predicate<String> hasDigit  = s -> s.chars().anyMatch(Character::isDigit);
        Predicate<String> hasUpper  = s -> s.chars().anyMatch(Character::isUpperCase);

        // 조합: 비밀번호 규칙 = 4자 이상 + 숫자 포함 + 대문자 포함
        Predicate<String> isValidPw = notNull.and(notEmpty).and(minLen4)
                                             .and(hasDigit).and(hasUpper);

        String[] candidates = {"abc1", "Abc1", "AB12", "ab", null};
        for (String c : candidates) {
            try {
                System.out.printf("%-6s → %s%n", c, isValidPw.test(c));
            } catch (NullPointerException e) {
                System.out.printf("%-6s → false (null)%n", c);
            }
        }
        // abc1   → false (대문자 없음)
        // Abc1   → true
        // AB12   → true
        // ab     → false (4자 미만)
        // null   → false (null)

        // or 조합: 허용 확장자 검사
        Predicate<String> isJpg = s -> s.endsWith(".jpg");
        Predicate<String> isPng = s -> s.endsWith(".png");
        Predicate<String> isGif = s -> s.endsWith(".gif");
        Predicate<String> isImage = isJpg.or(isPng).or(isGif);

        System.out.println(isImage.test("photo.jpg")); // true
        System.out.println(isImage.test("doc.pdf"));   // false
    }

    // ── 4. 커링(Currying) ────────────────────────────────────────────────────
    // 다인수 함수를 1인수 함수의 체인으로 분리 → 부분 적용(Partial Application)
    static void currying() {
        System.out.println("\n=== 4. 커링 ===");

        // BiFunction → 커리드: add(a, b)  →  addCurried(a)(b)
        Function<Integer, Function<Integer, Integer>> addCurried = a -> b -> a + b;
        System.out.println(addCurried.apply(3).apply(5));   // 8

        // 부분 적용: 첫 번째 인수만 고정 → 새 함수 생성
        Function<Integer, Integer> add5 = addCurried.apply(5);
        System.out.println(add5.apply(10));  // 15
        System.out.println(add5.apply(20));  // 25

        // 실무 예: 접두사 붙이기
        Function<String, Function<String, String>> prefix = pre -> s -> pre + s;
        Function<String, String> addMr = prefix.apply("Mr. ");
        Function<String, String> addDr = prefix.apply("Dr. ");
        System.out.println(addMr.apply("Smith")); // Mr. Smith
        System.out.println(addDr.apply("Kim"));   // Dr. Kim

        // 실무 예: 포맷 문자열 커링
        Function<String, Function<Object, String>> formatter = fmt -> val -> String.format(fmt, val);
        Function<Object, String> toHex     = formatter.apply("0x%X");
        Function<Object, String> toPercent = formatter.apply("%.1f%%");
        System.out.println(toHex.apply(255));      // 0xFF
        System.out.println(toPercent.apply(98.6)); // 98.6%
    }

    // ── 5. 메모이제이션(Memoization) ─────────────────────────────────────────
    // computeIfAbsent 로 결과 캐싱 → 중복 연산 방지
    static void memoization() {
        System.out.println("\n=== 5. 메모이제이션 ===");

        // 단순 캐싱 패턴
        Map<Integer, Integer> cache = new HashMap<>();
        Function<Integer, Integer> expensiveSquare = n -> {
            System.out.println("  [연산 실행] " + n);
            return n * n;
        };
        Function<Integer, Integer> memoized = n -> cache.computeIfAbsent(n, expensiveSquare);

        System.out.println(memoized.apply(5)); // [연산 실행] 5  → 25
        System.out.println(memoized.apply(5)); // 캐시 히트      → 25
        System.out.println(memoized.apply(3)); // [연산 실행] 3  → 9

        // 피보나치 메모이제이션
        Map<Integer, Long> fibCache = new HashMap<>();
        System.out.println("fib(10) = " + fib(10, fibCache)); // 55
        System.out.println("fib(40) = " + fib(40, fibCache)); // 102334155 (캐시 덕분에 빠름)
    }

    private static long fib(int n, Map<Integer, Long> memo) {
        if (n <= 1) return n;
        return memo.computeIfAbsent(n, k -> fib(k - 1, memo) + fib(k - 2, memo));
    }

    // ── 6. Map 함수형 메서드 ─────────────────────────────────────────────────
    // compute / computeIfAbsent / computeIfPresent / merge / replaceAll
    static void mapFunctional() {
        System.out.println("\n=== 6. Map 함수형 메서드 ===");

        Map<String, Integer> scores = new HashMap<>();
        scores.put("Alice", 80);
        scores.put("Bob",   70);

        // computeIfAbsent: 키 없을 때만 계산 후 삽입
        scores.computeIfAbsent("Charlie", k -> k.length() * 10); // "Charlie".length()*10 = 70
        System.out.println("Charlie: " + scores.get("Charlie")); // 70

        // computeIfPresent: 키 있을 때만 업데이트
        scores.computeIfPresent("Alice", (k, v) -> v + 10);
        System.out.println("Alice: " + scores.get("Alice")); // 90

        // compute: 항상 실행 (없으면 삽입, 있으면 업데이트, null 반환 시 삭제)
        scores.compute("Bob", (k, v) -> v == null ? 0 : v + 20);
        System.out.println("Bob: " + scores.get("Bob")); // 90

        // merge: 키 없으면 값 삽입, 있으면 병합 함수 실행
        scores.merge("Dave", 60, Integer::sum); // 없으므로 60 삽입
        scores.merge("Bob",  10, Integer::sum); // 90 + 10 = 100
        System.out.println("Dave: " + scores.get("Dave")); // 60
        System.out.println("Bob:  " + scores.get("Bob"));  // 100

        // replaceAll: 모든 값 일괄 변환
        scores.replaceAll((k, v) -> v + 5);  // 전원 보너스 +5
        scores.forEach((k, v) -> System.out.printf("  %-8s %d%n", k, v));

        // 빈도 카운터 관용 패턴 (merge 활용)
        String[] words = {"apple", "banana", "apple", "cherry", "banana", "apple"};
        Map<String, Integer> freq = new HashMap<>();
        for (String w : words) {
            freq.merge(w, 1, Integer::sum);
        }
        System.out.println(freq); // {apple=3, banana=2, cherry=1}
    }

    // ── 7. Optional 연계 ─────────────────────────────────────────────────────
    // map(Function) / filter(Predicate) / ifPresent(Consumer) / orElseGet(Supplier)
    static void optionalFunctional() {
        System.out.println("\n=== 7. Optional 연계 ===");

        Optional<String> opt = Optional.of("  hello world  ");

        // map: 값 변환 (Function)
        String upper = opt
            .map(String::trim)
            .map(String::toUpperCase)
            .orElse("EMPTY");
        System.out.println(upper); // HELLO WORLD

        // filter: 조건 불만족 시 empty 로 전환 (Predicate)
        Optional<String> filtered = opt
            .map(String::trim)
            .filter(s -> s.length() > 5);
        System.out.println(filtered.isPresent()); // true

        // ifPresent: 값이 있을 때만 실행 (Consumer)
        opt.map(String::trim)
           .ifPresent(s -> System.out.println("값: " + s)); // 값: hello world

        // orElseGet: 값 없을 때 Supplier 로 기본값 생성 (지연 평가)
        Optional<String> empty = Optional.empty();
        String fallback = empty.orElseGet(() -> "default-" + System.currentTimeMillis());
        System.out.println(fallback); // default-...

        // flatMap: Optional 을 반환하는 Function 연결 (중첩 Optional 방지)
        Optional<String> result = Optional.of("42")
            .flatMap(s -> {
                try { return Optional.of(Integer.parseInt(s)); }
                catch (NumberFormatException e) { return Optional.empty(); }
            })
            .map(n -> "파싱 결과: " + n);
        System.out.println(result.orElse("파싱 실패")); // 파싱 결과: 42
    }
}