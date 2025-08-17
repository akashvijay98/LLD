public enum TransactionStatus {
    PENDING,
    SUCCESS,
    FAILED
}

import java.util.Date;

public class Transaction {
    private String transactionId;
    private double amount;
    private TransactionStatus status;
    private Date createdAt;

    public Transaction(String transactionId, double amount) {
        this.transactionId = transactionId;
        this.amount = amount;
        this.status = TransactionStatus.PENDING;
        this.createdAt = new Date();
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public TransactionStatus getStatus() {
        return this.status;
    }

    // Getters (optional)
}


public abstract class Payment {
    public abstract boolean initiatePayment(double amount);
    public abstract boolean verifyPayment();
    public abstract void rollback();
}


public interface PaymentStrategy {
    
   boolean initiatePayment(double amount);
    boolean verifyPayment();
    void rollback();
}

public class CreditCardPayment implements PaymentStrategy {
    @Override
    public boolean initiatePayment(double amount) {
        System.out.println("Credit Card payment initiated: " + amount);
        return true;
    }

    @Override
    public boolean verifyPayment() {
        System.out.println("Verifying Credit Card payment");
        return true;
    }

    @Override
    public void rollback() {
        System.out.println("Rolling back Credit Card payment");
    }
}

public class RetryPolicy {
    private static final int MAX_RETRIES = 3;

    public boolean retryPayment(PaymentStrategy payment, double amount) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            waitForNextAttempt(i);
            if (payment.initiatePayment(amount) && payment.verifyPayment()) {
                return true;
            }
        }
        return false;
    }

    public void waitForNextAttempt(int attempt) {
        try {
            Thread.sleep((attempt + 1) * 1000L); // 1s, 2s, 3s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}


import java.util.HashMap;
import java.util.Map;

public class PaymentProcessor {
    private Map<String, Transaction> transactions = new HashMap<>();
    private RetryPolicy retryPolicy = new RetryPolicy();

    public void processPayment(String idempotencyKey, PaymentStrategy paymentMethod, double amount) {
        if (transactions.containsKey(idempotencyKey)) {
            System.out.println("Duplicate transaction detected");
            return;
        }

        Transaction txn = new Transaction(idempotencyKey, amount);
        transactions.put(idempotencyKey, txn);

        boolean success = retryPolicy.retryPayment(paymentMethod, amount);
        if (success) {
            txn.setStatus(TransactionStatus.SUCCESS);
            System.out.println("Transaction succeeded");
        } else {
            txn.setStatus(TransactionStatus.FAILED);
            paymentMethod.rollback();
            System.out.println("Transaction failed after retries");
        }
    }
}

