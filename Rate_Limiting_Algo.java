import java.util.concurrent.TimeUnit;

public class TokenBucket {

    private final long capacity;
    private final double fillRate; // tokens per second
    private double tokens;
    private long lastFill;

    public TokenBucket(long capacity, double fillRate) {
        this.capacity = capacity;
        this.fillRate = fillRate;
        this.tokens = capacity;
        this.lastFill = System.currentTimeMillis();
    }

    /**
     * Attempts to consume one token from the bucket.
     * This method is synchronized to be thread-safe.
     *
     * @return true if a token was consumed, false otherwise.
     */
    public synchronized boolean allow() {
        // Refill the bucket with new tokens that have accrued
        long now = System.currentTimeMillis();
        long timePassed = now - this.lastFill;
        
        if (timePassed > 0) {
            double newTokens = timePassed * (this.fillRate / 1000.0);
            this.tokens = Math.min(this.capacity, this.tokens + newTokens);
            this.lastFill = now;
        }

        // Check if there are enough tokens to allow the request
        if (this.tokens >= 1) {
            this.tokens -= 1;
            return true;
        }

        return false;
    }

    // Example Usage
    public static void main(String[] args) throws InterruptedException {
        // 5 tokens, refill 1 token every 2 seconds (0.5 per second)
        TokenBucket limiter = new TokenBucket(5, 0.5);

        // First 5 requests should be allowed instantly
        System.out.println("--- Burst of 6 requests ---");
        for (int i = 0; i < 6; i++) {
            System.out.println("Request " + (i + 1) + ": " + (limiter.allow() ? "Allowed" : "Denied"));
        }

        // Wait for 4 seconds, which should generate 2 new tokens (4s * 0.5 tokens/s)
        System.out.println("\n--- Waiting for 4 seconds ---");
        TimeUnit.SECONDS.sleep(4);
        
        System.out.println("\n--- Another 3 requests ---");
        for (int i = 0; i < 3; i++) {
             System.out.println("Request " + (i + 1) + ": " + (limiter.allow() ? "Allowed" : "Denied"));
        }
    }
}
