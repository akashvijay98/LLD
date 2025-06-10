import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

enum TicketStatus {
    AVAILABLE,
    BLOCKED,
    BOOKED
}
class Booking{
    UUID id;
    LocalDateTime createdAt;

    UUID ticketId;

    UUID userId;

    public Booking( UUID ticketId, UUID userId){
        this.id = UUID.randomUUID();
        this.ticketId = ticketId;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

}

class Ticket {
    UUID id;
    UUID showId;
    int seatNumber;

    double price;
    TicketStatus status;

    Booking bookingId;

    public Ticket( UUID showId, int seatNumber, double price) {
        //this.id = UUID.randomUUID();

        this.id = UUID.fromString("ca79e27f-a494-47f6-84c2-576807c42a82");
        this.showId = showId;
        this.seatNumber = seatNumber;
        this.price = price;
        this.status = TicketStatus.AVAILABLE;
    }
}

class Show {
    UUID id;
    String location;
    LocalDateTime startTime;
    List<Ticket> tickets;

    public Show(String location, LocalDateTime startTime, List<Ticket> tickets) {
        this.id = UUID.randomUUID();
        this.location = location;
        this.startTime = startTime;
        this.tickets = tickets;
    }
}
class  TicketBooking {

    Map<UUID, Show> showDb = new HashMap<>();
    Map<UUID, Ticket> ticketDb = new HashMap<>();
    Map<UUID, Booking> bookingStore = new HashMap<>();

    public void addShow(Show show) {
        showDb.put(show.id, show);
        for (Ticket t : show.tickets) {
            ticketDb.put(t.id, t);
        }
    }



    public String blockTicket(UUID ticketId,  UUID userId) {
        Ticket ticket = ticketDb.get(ticketId);

        if (ticket == null) {
            throw new RuntimeException("ticket not found");
        }


        synchronized (ticket) {
            if (ticket.status != TicketStatus.AVAILABLE) {
                throw new RuntimeException("Ticket not available");
            }

            ticket.status = TicketStatus.BLOCKED;

            return "Ticket is  temporarily locked for user " + userId;
        }
    }

    public String confirmBooking(UUID ticketId, UUID userId) {
        Ticket ticket = ticketDb.get(ticketId);
        if (ticket == null) {
            throw new RuntimeException("Show not found");
        }

        synchronized (ticket) {
            if (ticket.status != TicketStatus.BLOCKED) {
                throw new RuntimeException("Ticket is not in a blockable state");
            }
            UUID bookingId = UUID.randomUUID();
            Booking booking  = new Booking(ticketId, userId);


            bookingStore.put(bookingId, booking);

            return "Ticket confirmed for user " + userId + ": " + ticketDetails(ticket, userId);
        }
    }

    public List<Ticket> showAvailableTickets(UUID showId) {
        Show show = showDb.get(showId);
        if (show == null) return new ArrayList<>();

        return show.tickets.stream()
                .filter(ticket -> ticket.status == TicketStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public List<Ticket> showAllTickets(UUID showId) {
        Show show = showDb.get(showId);
        if (show == null) return new ArrayList<>();

        return show.tickets;
    }

    private String ticketDetails(Ticket ticket, UUID userId) {
        return "User: " + userId + ", Seat: " + ticket.seatNumber +
                ", Event: " +  ", Price: $" + ticket.price;
    }
}

public class TicketBookingApp {
    public static void main(String[] args) throws InterruptedException {
        TicketBooking bookingSystem = new TicketBooking();

        // Create a show with tickets
        List<Ticket> tickets = new ArrayList<>();
        UUID showId = UUID.randomUUID();
        for (int i = 1; i <= 5; i++) {
            tickets.add(new Ticket(showId, i, 150.0));
        }
        Show show = new Show("Madison Square Garden", LocalDateTime.now().plusDays(1), tickets);
        bookingSystem.addShow(show);

        // Create a thread pool to simulate multiple users
        ExecutorService executor = Executors.newFixedThreadPool(5);
        List<UUID> userIds = Arrays.asList(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        for (UUID userId : userIds) {
            int seatToBook = 1; // All try to book seat 1
            executor.submit(() -> {
                try {
                    UUID ticketId = UUID.fromString("ca79e27f-a494-47f6-84c2-576807c42a82");
                    String blockResult = bookingSystem.blockTicket(ticketId, userId);
                    System.out.println(blockResult);
                    Thread.sleep(500); // Simulate delay before confirming
                    String confirmResult = bookingSystem.confirmBooking(show.id,  userId);
                    System.out.println(confirmResult);
                } catch (Exception e) {
                    System.out.println("Error for user " + userId + ": " + e.getMessage());
                }
            });
        }

        executor.shutdown();
       // executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.println("\nFinal Ticket Statuses:");
        bookingSystem.showAvailableTickets(show.id).forEach(t ->
                System.out.println("Seat: " + t.seatNumber + ", Status: " + t.status)
        );
    }
}
