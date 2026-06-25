package test.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ArrayList 동시성 참조(ConcurrentModificationException) 테스트
 *
 * 구조
 * 1. ArrayList 생성
 * 2. 가데이터 생성 및 적재
 * 3. 여러 스레드에서 조회(read) / 수정(update) / 삭제(delete) 동시 실행
 * 4. list 자체를 구조적으로 수정(add/remove)하여 CME 유발
 *
 * 동기화 없는 ArrayList 는 다음과 같은 예외/오류를 낼 수 있습니다.
 * - ConcurrentModificationException (조회 중 다른 스레드가 구조 변경)
 * - IndexOutOfBoundsException (삭제로 인해 인덱스가 밀려서 발생)
 */
public class ConcurrencyTest {

    // 1. list 를 ArrayList 로 생성 (동기화 X -> 일부러 스레드 안전하지 않게 둠)
    //private static final List<String> list = new ArrayList<>();
    private static final List<String> list = new CopyOnWriteArrayList<>();

    private static final int INITIAL_SIZE = 2000;
    private static final int THREAD_COUNT = 100;
    private static final int TASK_LOOP = 200;

    private static final AtomicInteger cmeCount = new AtomicInteger(0);
    private static final AtomicInteger otherErrorCount = new AtomicInteger(0);

    public static void main(String[] args) throws InterruptedException {

        // 2. 가데이터 생성 및 적재
        for (int i = 0; i < INITIAL_SIZE; i++) {
            list.add("item-" + i);
        }
        System.out.println("[INIT] 초기 데이터 적재 완료. size=" + list.size());

        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        Random random = new Random();

        // 3. 여러 스레드에서 조회 / 수정 / 삭제 진행
        // - 조회(READER): for-each 로 순회
        executor.submit(() -> readerTask(latch, "READER-1"));
        executor.submit(() -> readerTask(latch, "READER-2"));

        // - 수정(UPDATER): 인덱스 값 set
        executor.submit(() -> updaterTask(latch, "UPDATER-1", random));

        // test
        executor.submit(() -> regrashTask(latch, "REFRASH-1", random));

        // - 삭제(DELETER): 임의 인덱스 remove
        //executor.submit(() -> deleterTask(latch, "DELETER-1", random));

        // 4. list 자체를 구조적으로 수정 (add/remove) -> 다른 스레드 순회 중 CME 유발
        executor.submit(() -> structuralModifyTask(latch, "MODIFIER-add", random, true));
        executor.submit(() -> structuralModifyTask(latch, "MODIFIER-remove", random, false));

        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.println("\n[RESULT] 최종 size=" + list.size());
        System.out.println("[RESULT] ConcurrentModificationException 발생 횟수 = " + cmeCount.get());
        System.out.println("[RESULT] 그 외 예외(IndexOutOfBounds 등) 발생 횟수 = " + otherErrorCount.get());
    }

    // 조회: for-each 순회 중 다른 스레드가 구조를 바꾸면 CME 발생
    private static void readerTask(CountDownLatch latch, String name) {
        try {
            for (int i = 0; i < TASK_LOOP; i++) {
                try {
                    int sum = 0;
                    for (String s : list) {           // 순회 (iterator 내부 사용)
                        sum += s.length();
                    }
                } catch (java.util.ConcurrentModificationException e) {
                    cmeCount.incrementAndGet();
                    System.out.println("[" + name + "] ConcurrentModificationException 발생");
                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("[" + name + "] 기타 예외: " + e);
                }
            }
        } finally {
            latch.countDown();
        }
    }

    // 수정: 인덱스 기반 값 변경 (구조 변경은 아니지만 인덱스 범위 문제 발생 가능)
    private static void updaterTask(CountDownLatch latch, String name, Random random) {
        try {
            for (int i = 0; i < TASK_LOOP; i++) {
                try {
                    int size = list.size();
                    if (size == 0) continue;
                    int idx = random.nextInt(size);
                    list.set(idx, "updated-" + idx + "-" + System.nanoTime());
                } catch (IndexOutOfBoundsException e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("[" + name + "] IndexOutOfBoundsException 발생");
                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("[" + name + "] 기타 예외: " + e);
                }
            }
        } finally {
            latch.countDown();
        }
    }


    // 삭제: 임의 인덱스 remove (구조 변경 -> 순회 중이던 스레드에서 CME 유발 가능)
    private static void regrashTask(CountDownLatch latch, String name, Random random) {
        try {
            List<String> newList = new CopyOnWriteArrayList<>();
            for (int i = 0; i < INITIAL_SIZE; i++) {
                newList.add("item-" + i);
            }

            list.clear();
            list.addAll(newList);
        }catch (Exception e){
        }
    }

    // 삭제: 임의 인덱스 remove (구조 변경 -> 순회 중이던 스레드에서 CME 유발 가능)
    private static void deleterTask(CountDownLatch latch, String name, Random random) {
        try {
            for (int i = 0; i < TASK_LOOP; i++) {
                try {
                    int size = list.size();
                    if (size == 0) continue;
                    int idx = random.nextInt(size);
                    list.remove(idx);
                } catch (IndexOutOfBoundsException e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("[" + name + "] IndexOutOfBoundsException 발생");
                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("[" + name + "] 기타 예외: " + e);
                }
            }
        } finally {
            latch.countDown();
        }
    }

    // list 자체를 구조적으로 수정 (add 또는 remove)
    private static void structuralModifyTask(CountDownLatch latch, String name, Random random, boolean isAdd) {
        try {
            for (int i = 0; i < TASK_LOOP; i++) {
                try {
                    if (isAdd) {
                        list.add("new-" + System.nanoTime());
                    } else {
                        int size = list.size();
                        if (size == 0) continue;
                        list.remove(size - 1);
                    }
                } catch (Exception e) {
                    otherErrorCount.incrementAndGet();
                    System.out.println("[" + name + "] 기타 예외: " + e);
                }
            }
        } finally {
            latch.countDown();
        }
    }
}
