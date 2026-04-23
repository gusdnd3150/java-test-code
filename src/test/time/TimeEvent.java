package test.time;

import test.Utilitys.HMap;
import test.Utilitys.JdbcTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeEvent {

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    HMap<String,Object> lastBarcode = new HMap<>(); // 실시간 공정별 하나


    private final Deque<Long> timestamps = new ArrayDeque<>(); // 하나만 관리할때
    private final Map<String, Deque<Long>> signalMap = new ConcurrentHashMap<>();// 타입별로 독립적인 슬라이딩 윈도우 관리
    private static final long WINDOW_MS = 500L; //
    private static final int THRESHOLD = 3;       // 3번

    // 3초 안에 특정 신호가 3번 오는지 확인하는 로직
    public TimeEvent() throws Exception {
        while (true) {
            String mesProdSeq = params("cnt");
            if("1".equals(mesProdSeq)){
                boolean test = onSignal();
                if (test)System.out.println(String.format("TimeEvent1 :: %s", test));
            }
        }
    }

    public String params(String type) throws IOException {
        System.out.println(type.toUpperCase() + "=");
        return br.readLine();
    }

    public boolean onSignal(String type) {
        long now = System.currentTimeMillis();

        // 타입별 Deque 없으면 자동 생성
        Deque<Long> timestamps = signalMap.computeIfAbsent(type, k -> new ArrayDeque<>());

        synchronized (timestamps) {
            timestamps.addLast(now);

            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
                timestamps.pollFirst();
            }

            boolean result = timestamps.size() >= THRESHOLD;
            if (result) timestamps.clear();
            return result;
        }
    }

    public synchronized boolean onSignal() {
        long now = System.currentTimeMillis();

        // 1. 현재 시각 추가
        timestamps.addLast(now);

        // 2. 3초 이전 타임스탬프 제거 (슬라이딩 윈도우)
        while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MS) {
            timestamps.pollFirst();
        }

        // 3. 윈도우 내 신호 수 확인
        boolean rslt = timestamps.size() >= THRESHOLD;
        if(rslt){
            timestamps.clear();
        }
        return rslt;
    }

}
