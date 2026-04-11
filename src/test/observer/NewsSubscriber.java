package test.observer;

/**
 * 옵저버 패턴 - 구체적인 Observer (뉴스 구독자)
 * Subject 로부터 알림을 받아 이벤트 유형에 따라 다르게 처리
 */
public class NewsSubscriber implements Observer {

    private final String subscriberName;

    public NewsSubscriber(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    @Override
    public void update(String event, Object data) {
        switch (event) {
            case "NEWS":
                System.out.println("  -> [" + subscriberName + "] 일반 뉴스 수신: " + data);
                break;
            case "BREAKING":
                System.out.println("  -> [" + subscriberName + "] ★ 속보 수신: " + data + " ★");
                break;
            default:
                System.out.println("  -> [" + subscriberName + "] 알 수 없는 이벤트(" + event + "): " + data);
        }
    }

    public String getName() {
        return subscriberName;
    }
}