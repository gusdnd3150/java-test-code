package test.observer;

/**
 * 옵저버 패턴 - Observer 인터페이스
 * Subject(발행자)로부터 알림을 받는 구독자의 공통 계약 정의
 */
public interface Observer {

    /**
     * Subject 상태 변경 시 호출되는 알림 메서드
     * @param event  발생한 이벤트 유형
     * @param data   Subject 가 전달하는 데이터
     */
    void update(String event, Object data);
}