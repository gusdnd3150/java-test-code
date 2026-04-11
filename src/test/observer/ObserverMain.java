package test.observer;

/**
 * 옵저버 패턴 실행 예시
 *
 * 구조:
 *   Subject (인터페이스)
 *     └── NewsPublisher (구체 Subject) — 뉴스 발행, Observer 목록 관리
 *
 *   Observer (인터페이스)
 *     └── NewsSubscriber (구체 Observer) — 알림 수신 및 처리
 *
 * 흐름:
 *   1. NewsPublisher 생성
 *   2. NewsSubscriber 들을 publisher 에 등록 (addObserver)
 *   3. publisher 가 뉴스 발행 → 등록된 모든 subscriber 의 update() 자동 호출
 *   4. 특정 subscriber 구독 해제 후 다시 발행 → 해제된 subscriber 는 수신 안 함
 */
public class ObserverMain {

    public static void main(String[] args) {

        // 발행사(Subject) 생성
        NewsPublisher publisher = new NewsPublisher("한국일보");

        // 구독자(Observer) 생성
        NewsSubscriber alice = new NewsSubscriber("Alice");
        NewsSubscriber bob   = new NewsSubscriber("Bob");
        NewsSubscriber carol = new NewsSubscriber("Carol");

        System.out.println("=== 구독자 등록 ===");
        publisher.addObserver(alice);
        publisher.addObserver(bob);
        publisher.addObserver(carol);

        System.out.println("\n=== 일반 뉴스 발행 ===");
        publisher.publishNews("오늘 날씨: 전국 맑음");

        System.out.println("\n=== 속보 발행 ===");
        publisher.publishBreaking("긴급! 주요 경제 지표 발표");

        System.out.println("\n=== Bob 구독 해제 후 뉴스 발행 ===");
        publisher.removeObserver(bob);
        publisher.publishNews("내일 날씨: 서울 오후 비");

        System.out.println("\n=== 모든 구독자 해제 후 발행 ===");
        publisher.removeObserver(alice);
        publisher.removeObserver(carol);
        publisher.publishNews("이 뉴스는 아무도 수신하지 않습니다.");
    }
}
