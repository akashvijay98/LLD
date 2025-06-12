import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// Enum for transaction type
enum TransactionType {
    DEBIT, CREDIT
}

// Exception for insufficient balance
class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

// Card class with PIN
class Card {
    int cardNumber;
    String userName;
    UUID userId;
    private String pin;

    public Card(int cardNumber, String userName, UUID userId, String pin) {
        this.cardNumber = cardNumber;
        this.userName = userName;
        this.userId = userId;
        this.pin = pin;
    }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public UUID getUserId() {
        return userId;
    }
}

// Bank Account class
class Account {
    UUID accountNumber;
    private double balance;

    public Account(UUID accountNumber, double balance) {
        this.accountNumber = accountNumber;
        this.balance = balance;
    }

    public synchronized void debit(double amount) throws InsufficientFundsException {
        if (balance >= amount) {
            balance -= amount;
        } else {
            throw new InsufficientFundsException("Insufficient balance");
        }
    }

    public synchronized void deposit(double amount) {
        balance += amount;
    }

    public synchronized double getBalance() {
        return balance;
    }
}

// Transaction class
class Transaction {
    private final UUID userId;
    private final double amount;
    private final TransactionType type;
    private final UUID transactionId;

    public Transaction(UUID userId, double amount, TransactionType type) {
        this.transactionId = UUID.randomUUID();
        this.userId = userId;
        this.amount = amount;
        this.type = type;
    }

    public String toString() {
        return type + " of $" + amount + " for user " + userId + " (TXN: " + transactionId + ")";
    }
}

// Cash Inventory
class CashInventory {
    private final Map<Integer, Integer> denominationMap = new TreeMap<>(Collections.reverseOrder());

    public CashInventory() {
        denominationMap.put(100, 10);
        denominationMap.put(50, 10);
        denominationMap.put(20, 20);
    }

    public synchronized boolean dispenseCash(double amount) {
        double remaining = amount;
        Map<Integer, Integer> temp = new HashMap<>();

        for (int denom : denominationMap.keySet()) {
            int needed = (int) (remaining / denom);
            int available = denominationMap.getOrDefault(denom, 0);
            int used = Math.min(needed, available);
            if (used > 0) {
                temp.put(denom, used);
                remaining -= used * denom;
            }
        }

        if (remaining > 0.001) return false;

        for (Map.Entry<Integer, Integer> entry : temp.entrySet()) {
            denominationMap.put(entry.getKey(), denominationMap.get(entry.getKey()) - entry.getValue());
        }
        return true;
    }

    public synchronized void addCash(double amount) {
        int denom = 20;
        int count = (int) (amount / denom);
        denominationMap.put(denom, denominationMap.getOrDefault(denom, 0) + count);
    }

    public double getTotalCash() {
        return denominationMap.entrySet().stream()
                .mapToDouble(e -> e.getKey() * e.getValue()).sum();
    }
}

// Banking service abstraction
interface BankingService {
    boolean authenticate(Card card, String pin);
    void deposit(UUID userId, double amount);
    void debit(UUID userId, double amount) throws InsufficientFundsException;
    double getBalance(UUID userId);
}

// In-memory banking service implementation
class InMemoryBankingService implements BankingService {
    private final Map<UUID, Account> accounts = new ConcurrentHashMap<>();
    private final Map<UUID, List<Transaction>> transactions = new ConcurrentHashMap<>();

    public void register(UUID userId, Account account) {
        accounts.put(userId, account);
    }

    public boolean authenticate(Card card, String pin) {
        return card.validatePin(pin);
    }

    public void deposit(UUID userId, double amount) {
        accounts.get(userId).deposit(amount);
        transactions.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new Transaction(userId, amount, TransactionType.CREDIT));
    }

    public void debit(UUID userId, double amount) throws InsufficientFundsException {
        accounts.get(userId).debit(amount);
        transactions.computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(new Transaction(userId, amount, TransactionType.DEBIT));
    }

    public double getBalance(UUID userId) {
        return accounts.get(userId).getBalance();
    }

    public List<Transaction> getTransactions(UUID userId) {
        return transactions.getOrDefault(userId, new ArrayList<>());
    }
}

// ATM class
class ATM {
    private final BankingService bankingService;
    private final CashInventory cashInventory;
    private final Map<UUID, Card> cards = new HashMap<>();

    public ATM(BankingService bankingService) {
        this.bankingService = bankingService;
        this.cashInventory = new CashInventory();
    }

    public void registerUser(Card card, Account account) {
        cards.put(card.getUserId(), card);
        ((InMemoryBankingService) bankingService).register(card.getUserId(), account);
    }

    public void startSession(Card card, String pin) {
        if (!bankingService.authenticate(card, pin)) {
            System.out.println("Authentication failed.");
            return;
        }

        Scanner sc = new Scanner(System.in);
        UUID userId = card.getUserId();

        while (true) {
            System.out.println("\n1. Balance Inquiry\n2. Deposit\n3. Withdraw\n4. Transactions\n5. Exit");
            int choice = sc.nextInt();

            switch (choice) {
                case 1:
                    System.out.println("Balance: $" + bankingService.getBalance(userId));
                    break;
                case 2:
                    System.out.print("Enter deposit amount: ");
                    double depAmt = sc.nextDouble();
                    bankingService.deposit(userId, depAmt);
                    cashInventory.addCash(depAmt);
                    System.out.println("Deposit successful.");
                    break;
                case 3:
                    System.out.print("Enter withdraw amount: ");
                    double withAmt = sc.nextDouble();
                    try {
                        if (cashInventory.dispenseCash(withAmt)) {
                            bankingService.debit(userId, withAmt);
                            System.out.println("Withdraw successful.");
                        } else {
                            System.out.println("ATM has insufficient cash.");
                        }
                    } catch (InsufficientFundsException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case 4:
                    List<Transaction> txns = ((InMemoryBankingService) bankingService).getTransactions(userId);
                    txns.forEach(System.out::println);
                    break;
                case 5:
                    System.out.println("Session ended. Card ejected.");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}

// MAIN method to test
public class ATMSystem {
    public static void main(String[] args) {
        BankingService bankingService = new InMemoryBankingService();
        ATM atm = new ATM(bankingService);

        UUID userId = UUID.randomUUID();
        Card card = new Card(123456, "Akash", userId, "1234");
        Account account = new Account(UUID.randomUUID(), 1000);

        atm.registerUser(card, account);

        // Start a session
        atm.startSession(card, "1234");
    }
}
