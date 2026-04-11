package test.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * 옵저버 패턴 - 구체적인 Subject (뉴스 발행사)
 * Observer 목록을 관리하고, 새 뉴스 발행 시 모든 구독자에게 알림
 */
public class NewsPublisher implements Subject {

    private final String publisherName;
    private final List<Observer> observers = new ArrayList<>();
    private String latestNews;

    public NewsPublisher(String publisherName) {
        this.publisherName = publisherName;
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
        System.out.println("[" + publisherName + "] 구독자 등록 완료. 현재 구독자 수: " + observers.size());
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
        System.out.println("[" + publisherName + "] 구독자 해제 완료. 현재 구독자 수: " + observers.size());
    }

    @Override
    public void notifyObservers(String event, Object data) {
        for (Observer observer : observers) {
            observer.update(event, data);
        }
    }

    /**
     * 새 뉴스를 발행하고 구독자들에게 알림
     * @param news 발행할 뉴스 내용
     */
    public void publishNews(String news) {
        this.latestNews = news;
        System.out.println("\n[" + publisherName + "] 뉴스 발행: " + news);
        notifyObservers("NEWS", news);
    }

    /**
     * 속보를 발행하고 구독자들에게 알림 (이벤트 유형 구분 예시)
     * @param breaking 속보 내용
     */
    public void publishBreaking(String breaking) {
        this.latestNews = breaking;
        System.out.println("\n[" + publisherName + "] 속보 발행: " + breaking);
        notifyObservers("BREAKING", breaking);
    }

    public String getLatestNews() {
        return latestNews;
    }
}