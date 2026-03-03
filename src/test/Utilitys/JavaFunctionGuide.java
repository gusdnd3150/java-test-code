//package test;
//
//import java.util.function.*;
//
///**
// * Java Function 인터페이스 기본 가이드
// *
// * 인터페이스          입력    출력      주요 메서드
// * ──────────────────────────────────────────────
// * Function<T,R>      T       R         apply()
// * Consumer<T>        T       void      accept()
// * Supplier<T>        없음    T         get()
// * Predicate<T>       T       boolean   test()
// * UnaryOperator<T>   T       T         apply()   ← Function<T,T> 특수화
// * BinaryOperator<T>  T,T     T         apply()   ← BiFunction<T,T,T> 특수화
// * BiFunction<T,U,R>  T,U     R         apply()
// * BiConsumer<T,U>    T,U     void      accept()
// * BiPredicate<T,U>   T,U     boolean   test()
// */
//public class JavaFunctionGuide {
//
//    public static void main(String[] args) {
//        functionExamples();
//        consumerExamples();
//        supplierExamples();
//        predicateExamples();
//        operatorExamples();
//        biFunctionExamples();
//    }
//
//    // ── Function<T, R> ───────────────────────────────────────────────────────
//    // 입력 T → 출력 R 로 변환
//    static void functionExamples() {
//        System.out.println("=== Function<T, R> ===");
//
//        // 기본: String → Integer
//        Function<String, Integer> strToLen = s -> s.length();
//        System.out.println(strToLen.apply("hello"));    // 5
//
//        // 메서드 참조
//        Function<String, String> toUpper = String::toUpperCase;
//        System.out.println(toUpper.apply("hello"));     // HELLO
//
//        // 숫자 변환
//        Function<Integer, String> intToHex = n -> "0x" + Integer.toHexString(n);
//        System.out.println(intToHex.apply(255));        // 0xff
//
//        // andThen: f → g 순서로 실행 (f 결과를 g 에 전달)
//        Function<String, Integer>  strToInt  = Integer::parseInt;
//        Function<Integer, Integer> doubleIt  = n -> n * 2;
//        Function<String, Integer>  combined  = strToInt.andThen(doubleIt);
//        System.out.println(combined.apply("5"));        // 10
//
//        // compose: g → f 순서 (andThen 반대 — 인수로 받은 함수 먼저 실행)
//        Function<Integer, Integer> addOne        = n -> n + 1;
//        Function<Integer, Integer> multiplyThree = n -> n * 3;
//        // compose: multiplyThree( addOne(5) ) = multiplyThree(6) = 18
//        Function<Integer, Integer> addThenMultiply = multiplyThree.compose(addOne);
//        System.out.println(addThenMultiply.apply(5));   // 18
//    }
//
//    // ── Consumer<T> ──────────────────────────────────────────────────────────
//    // 입력 T → 반환값 없음 (출력·저장 등 부수효과 목적)
//    static void consumerExamples() {
//        System.out.println("\n=== Consumer<T> ===");
//
//        // 기본
//        Consumer<String> printer = s -> System.out.println(">> " + s);
//        printer.accept("hello");            // >> hello
//
//        // 메서드 참조
//        Consumer<String> sysOut = System.out::println;
//        sysOut.accept("world");             // world
//
//        // andThen: 두 Consumer 순서대로 연결
//        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
//        Consumer<String> printLen   = s -> System.out.println("len=" + s.length());
//        Consumer<String> both       = printUpper.andThen(printLen);
//        both.accept("hello");
//        // HELLO
//        // len=5
//    }
//
//    // ── Supplier<T> ──────────────────────────────────────────────────────────
//    // 입력 없음 → 출력 T (지연 생성, 값 공급)
//    static void supplierExamples() {
//        System.out.println("\n=== Supplier<T> ===");
//
//        // 기본: 고정값 공급
//        Supplier<String> greeting = () -> "Hello, World!";
//        System.out.println(greeting.get());             // Hello, World!
//
//        // 인스턴스 생성 공급 (팩토리 역할)
//        Supplier<StringBuilder> sbFactory = StringBuilder::new;
//        StringBuilder sb = sbFactory.get();
//        sb.append("test");
//        System.out.println(sb);                         // test
//
//        // 지연 평가 패턴: 조건 충족 시에만 무거운 연산 실행
//        boolean flag = false;
//        Supplier<String> expensive = () -> { /* 무거운 연산 */ return "heavy result"; };
//        String result = flag ? expensive.get() : "default";
//        System.out.println(result);                     // default (expensive.get() 미호출)
//    }
//
//    // ── Predicate<T> ─────────────────────────────────────────────────────────
//    // 입력 T → boolean (조건 판별)
//    static void predicateExamples() {
//        System.out.println("\n=== Predicate<T> ===");
//
//        Predicate<String>  isEmpty    = String::isEmpty;
//        Predicate<Integer> isEven     = n -> n % 2 == 0;
//        Predicate<Integer> isPositive = n -> n > 0;
//
//        System.out.println(isEmpty.test(""));       // true
//        System.out.println(isEven.test(4));         // true
//        System.out.println(isPositive.test(-1));    // false
//
//        // and: 둘 다 true
//        Predicate<Integer> isEvenAndPositive = isEven.and(isPositive);
//        System.out.println(isEvenAndPositive.test(4));   // true
//        System.out.println(isEvenAndPositive.test(-4));  // false
//
//        // or: 하나라도 true
//        Predicate<Integer> isEvenOrPositive = isEven.or(isPositive);
//        System.out.println(isEvenOrPositive.test(3));    // true  (양수)
//        System.out.println(isEvenOrPositive.test(-4));   // true  (짝수)
//        System.out.println(isEvenOrPositive.test(-3));   // false (홀수이면서 음수)
//
//        // negate: 반전
//        Predicate<Integer> isOdd = isEven.negate();
//        System.out.println(isOdd.test(3));               // true
//
//        // Predicate.not() (Java 11+) — 메서드 참조에 negate 적용 시 유용
//        Predicate<String> isNotEmpty = Predicate.not(String::isEmpty);
//        System.out.println(isNotEmpty.test("hi"));       // true
//    }
//
//    // ── UnaryOperator / BinaryOperator ───────────────────────────────────────
//    // UnaryOperator<T>  : 입출력 타입이 같은 Function<T,T>
//    // BinaryOperator<T> : 입출력 타입이 같은 BiFunction<T,T,T>
//    static void operatorExamples() {
//        System.out.println("\n=== Operator ===");
//
//        // UnaryOperator
//        UnaryOperator<String>  trim   = String::trim;
//        UnaryOperator<Integer> abs    = Math::abs;
//
//        System.out.println(trim.apply("  hi  "));   // "hi"
//        System.out.println(abs.apply(-5));           // 5
//
//        // andThen 체이닝 (반환 타입이 Function 이므로 변수 타입 주의)
//        Function<String, String> trimAndUpper = trim.andThen(String::toUpperCase);
//        System.out.println(trimAndUpper.apply("  hello  ")); // HELLO
//
//        // BinaryOperator
//        BinaryOperator<Integer> add    = (a, b) -> a + b;
//        BinaryOperator<String>  concat = (a, b) -> a + b;
//
//        System.out.println(add.apply(3, 5));              // 8
//        System.out.println(concat.apply("Hello, ", "World")); // Hello, World
//
//        // maxBy / minBy 내장 팩토리
//        BinaryOperator<Integer> maxBy = BinaryOperator.maxBy(Integer::compareTo);
//        BinaryOperator<Integer> minBy = BinaryOperator.minBy(Integer::compareTo);
//        System.out.println(maxBy.apply(10, 20)); // 20
//        System.out.println(minBy.apply(10, 20)); // 10
//    }
//
//    // ── BiFunction / BiConsumer / BiPredicate ────────────────────────────────
//    // Bi* : 입력 2개 버전
//    static void biFunctionExamples() {
//        System.out.println("\n=== Bi* ===");
//
//        // BiFunction<T, U, R>
//        BiFunction<String, Integer, String> repeat = (s, n) -> s.repeat(n);
//        System.out.println(repeat.apply("ab", 3));  // ababab
//
//        BiFunction<Integer, Integer, String> sumStr = (a, b) -> "합계: " + (a + b);
//        System.out.println(sumStr.apply(3, 5));     // 합계: 8
//
//        // andThen: BiFunction 결과를 Function 에 연결
//        BiFunction<Integer, Integer, Integer> multiply = (a, b) -> a * b;
//        Function<Integer, String> toStr = n -> "결과: " + n;
//        BiFunction<Integer, Integer, String> multiplyToStr = multiply.andThen(toStr);
//        System.out.println(multiplyToStr.apply(4, 5)); // 결과: 20
//
//        // BiConsumer<T, U>
//        BiConsumer<String, Integer> printRepeat = (s, n) -> System.out.println(s.repeat(n));
//        printRepeat.accept("hi", 3);    // hihihi
//
//        // BiConsumer andThen
//        BiConsumer<String, Integer> log1 = (s, n) -> System.out.println("입력: " + s + ", " + n);
//        BiConsumer<String, Integer> log2 = (s, n) -> System.out.println("처리 완료");
//        log1.andThen(log2).accept("test", 5);
//        // 입력: test, 5
//        // 처리 완료
//
//        // BiPredicate<T, U>
//        BiPredicate<String, String> startsWith = (s, prefix) -> s.startsWith(prefix);
//        System.out.println(startsWith.test("hello", "he")); // true
//        System.out.println(startsWith.test("hello", "wo")); // false
//    }
//}