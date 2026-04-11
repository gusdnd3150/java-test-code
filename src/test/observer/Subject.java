package test.observer;

/**
 * 옵저버 패턴 - Subject 인터페이스
 * Observer 를 등록/해제하고 알림을 보내는 발행자의 공통 계약 정의
 */
public interface Subject {

    /** Observer 등록 */
    void addObserver(Observer observer);

    /** Observer 해제 */
    void removeObserver(Observer observer);

    /** 등록된 모든 Observer 에게 알림 전송 */
    void notifyObservers(String event, Object data);
}