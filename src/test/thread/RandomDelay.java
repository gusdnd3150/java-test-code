package test.thread;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

// =========================
// 랜덤 지연( yield / sleep ) 유틸
// =========================
public final class RandomDelay {
	private final ThreadLocalRandom rnd = ThreadLocalRandom.current();

	private final int yieldPercent; // 0~100
	private final int sleepPercent; // 0~100 (yield와 별개로 체크)
	private final int minSleepMs; // sleep 최소
	private final int maxSleepMs; // sleep 최대
	private final int injectEveryN; // 매 N번마다만 인젝션 (1이면 매번 시도)

	private final AtomicInteger seq = new AtomicInteger(0);

	public RandomDelay(int yieldPercent, int sleepPercent, int minSleepMs, int maxSleepMs, int injectEveryN) {
		if (yieldPercent < 0 || yieldPercent > 100)
			throw new IllegalArgumentException("yieldPercent 0~100");
		if (sleepPercent < 0 || sleepPercent > 100)
			throw new IllegalArgumentException("sleepPercent 0~100");
		if (minSleepMs < 0)
			throw new IllegalArgumentException("minSleepMs >= 0");
		if (maxSleepMs < minSleepMs)
			throw new IllegalArgumentException("maxSleepMs >= minSleepMs");
		if (injectEveryN <= 0)
			throw new IllegalArgumentException("injectEveryN > 0");

		this.yieldPercent = yieldPercent;
		this.sleepPercent = sleepPercent;
		this.minSleepMs = minSleepMs;
		this.maxSleepMs = maxSleepMs;
		this.injectEveryN = injectEveryN;
	}

	/** 임계구간 전/후/중간 원하는 타이밍에 호출 */
	public void hit() {
		int n = seq.incrementAndGet();
		if (n % injectEveryN != 0)
			return;

		// yield 먼저
		if (yieldPercent > 0 && rnd.nextInt(100) < yieldPercent) {
			Thread.yield();
		}

		// sleep
		if (sleepPercent > 0 && rnd.nextInt(100) < sleepPercent) {
			int ms = (minSleepMs == maxSleepMs) ? minSleepMs : rnd.nextInt(minSleepMs, maxSleepMs + 1);
			try {
				Thread.sleep(ms);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
