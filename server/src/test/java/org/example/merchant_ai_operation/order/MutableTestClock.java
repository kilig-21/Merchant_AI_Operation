package org.example.merchant_ai_operation.order;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

public final class MutableTestClock extends Clock {

    private final AtomicReference<Instant> current;
    private final ZoneId zone;
    private final ThreadLocal<Instant> currentThreadTime = new ThreadLocal<>();

    //构造方法
    public MutableTestClock(Instant initial, ZoneId zone) {
        this(new AtomicReference<>(initial), zone);
    }

    private MutableTestClock(AtomicReference<Instant> current, ZoneId zone) {
        this.current = current;
        this.zone = zone;
    }


    /*
    * 方法
    * */

    public void set(Instant instant) {
        current.set(instant);
    }

    public void advance(Duration duration) {
        current.updateAndGet(currentTime -> currentTime.plus(duration));
    }

    public void setForCurrentThread(Instant instant) {
        currentThreadTime.set(instant);
    }

    public void clearForCurrentThread() {
        currentThreadTime.remove();
    }

    /*
    * 重写方法
    * */

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new MutableTestClock(current, zone);
    }

    @Override
    public Instant instant() {
        Instant threadTime = currentThreadTime.get();
        return threadTime != null ? threadTime : current.get();
    }
}