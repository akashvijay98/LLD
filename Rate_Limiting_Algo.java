// Token Bucket rate limiter

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


// Sliding Window Rate Limiter
import java.util.Deque;
import java.util.LinkedList;

public class SlidingWindowLogRateLimiter {

    private final int maxRequests;      // Max requests allowed in the window
    private final long windowInterval;  // The window size in milliseconds
    
    // This queue stores the timestamps of requests in the current window.
    private final Deque<Long> requestTimestamps;

    public SlidingWindowLogRateLimiter(int maxRequests, long windowIntervalInMillis) {
        this.maxRequests = maxRequests;
        this.windowInterval = windowIntervalInMillis;
        this.requestTimestamps = new LinkedList<>();
    }

    /**
     * Determines if a request should be allowed.
     * This method is synchronized to ensure thread safety.
     * @return true if the request is allowed, false otherwise.
     */
    public synchronized boolean allow() {
        long currentTime = System.currentTimeMillis();

        // 1. Slide the window: Remove all timestamps that are outside the current window.
        while (!requestTimestamps.isEmpty() && currentTime - requestTimestamps.peekFirst() >= windowInterval) {
            requestTimestamps.pollFirst();
        }
        
        // 2. Check the limit: If the number of requests in the window is less than the max, allow it.
        if (requestTimestamps.size() < maxRequests) {
            requestTimestamps.addLast(currentTime);
            return true;
        }
        
        // 3. Deny the request if the limit has been reached.
        return false;
    }
}



// Fixed Window rate limiter 

public class FixedWindowRateLimiter {

    private final int maxRequests;       // Max requests allowed in the window
    private final long windowInterval;   // The window size in milliseconds
    
    private int requestCount;            // Counter for requests in the current window
    private long windowStartTime;        // The start time of the current window

    /**
     * Constructor for the rate limiter.
     * @param maxRequests The maximum number of requests allowed in a window.
     * @param windowIntervalInMillis The time window in milliseconds.
     */
    public FixedWindowRateLimiter(int maxRequests, long windowIntervalInMillis) {
        this.maxRequests = maxRequests;
        this.windowInterval = windowIntervalInMillis;
        this.requestCount = 0;
        this.windowStartTime = System.currentTimeMillis();
    }

    /**
     * Determines if a request should be allowed.
     * This method is synchronized to ensure thread safety.
     * @return true if the request is allowed, false otherwise.
     */
    public synchronized boolean allow() {
        long currentTime = System.currentTimeMillis();

        // Check if the window has expired
        if (currentTime - windowStartTime > windowInterval) {
            // If it has, start a new window from the current time
            this.windowStartTime = currentTime;
            this.requestCount = 1; // This is the first request in the new window
            return true;
        }

        // If still within the window, check the count
        if (requestCount < maxRequests) {
            this.requestCount++;
            return true;
        }

        // If the count has been reached, deny the request
        return false;
    }
}
