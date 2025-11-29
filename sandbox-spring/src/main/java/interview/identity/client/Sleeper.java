package interview.identity.client;

import java.time.Duration;

@FunctionalInterface
public interface Sleeper {
    void sleep(Duration duration) throws InterruptedException;

    static Sleeper system() {
        return d -> Thread.sleep(Math.max(0L, d.toMillis()));
    }
}
