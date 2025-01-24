package ca.yorku.cmg.lob.stockexchange.events;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import ca.yorku.cmg.lob.security.Security;
import ca.yorku.cmg.lob.security.SecurityList;
import ca.yorku.cmg.lob.stockexchange.tradingagent.INewsObserver;

/**
 * A NewsBoard object generates and shares financial/economic events that affect
 * specific securities.
 * <p>
 * It now implements {@link INewsSubject} so that registered {@link INewsObserver}
 * objects (i.e. {@link ca.yorku.cmg.lob.stockexchange.tradingagent.TradingAgent}s)
 * are notified via a push model instead of polling.
 * </p>
 */
public class NewsBoard implements INewsSubject {

    // Events are queued ordered by time
    PriorityQueue<Event> eventQueue = new PriorityQueue<>(
            (e1, e2) -> Long.compare(e1.getTime(), e2.getTime()));

    SecurityList securities;

    // Observer list — all agents that have registered to receive events
    private List<INewsObserver> observers = new ArrayList<>();

    public NewsBoard(SecurityList x) {
        this.securities = x;
    }

    // -------------------------------------------------------------------------
    // INewsSubject implementation (Observer pattern)
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerObserver(INewsObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeObserver(INewsObserver observer) {
        observers.remove(observer);
    }

    /**
     * {@inheritDoc}
     * Pushes the event to every registered observer.
     */
    @Override
    public void notifyObservers(Event e) {
        for (INewsObserver observer : observers) {
            observer.update(e);
        }
    }

    // -------------------------------------------------------------------------
    // Allowed event values
    // -------------------------------------------------------------------------

    private static final Set<String> VALID_EVENTS = new HashSet<>(
            Arrays.asList("Good", "Bad"));

    // -------------------------------------------------------------------------
    // File loading
    // -------------------------------------------------------------------------

    /**
     * Load events from file.
     * Format: [Time, Relevant Ticker, EventType], where EventType is "Good" or "Bad".
     *
     * @param filePath The path of the file.
     */
    public void loadEvents(String filePath) {
        String line;
        String delimiter = ",";
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            while ((line = br.readLine()) != null) {
                String[] values = line.split(delimiter);
                if (values.length != 3) {
                    System.err.println("Invalid line format: " + line);
                    continue;
                }
                String time  = values[0].trim();
                String ticker = values[1].trim();
                String event  = values[2].trim();

                if (!VALID_EVENTS.contains(event)) {
                    System.err.println("Invalid event value: " + event + " in line: " + line);
                    continue;
                }

                Security s = securities.getSecurityByTicker(ticker);
                if (s == null) {
                    System.err.println("Unknown ticker: " + ticker + " in line: " + line);
                    continue;
                }

                Event eventObj;
                switch (event) {
                    case "Good":
                        eventObj = new GoodNews(Long.parseLong(time), s);
                        break;
                    case "Bad":
                        eventObj = new BadNews(Long.parseLong(time), s);
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected event value: " + event);
                }
                eventQueue.add(eventObj);
            }
        } catch (IOException e) {
            System.err.println("Error reading the file: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Pull model (kept for backward compatibility with pollingTest)
    // -------------------------------------------------------------------------

    /**
     * Returns the event that happened at time {@code time}, or {@code null} if none.
     *
     * @param time The time (in days) to query.
     * @return The matching event, or {@code null}.
     */
    public Event getEventAt(long time) {
        PriorityQueue<Event> clonedQueue = new PriorityQueue<>(eventQueue);
        while (!clonedQueue.isEmpty()) {
            long next = clonedQueue.peek().getTime();
            if (time > next) {
                clonedQueue.poll();
            } else if (time < next) {
                return null;
            } else { // time == next
                return clonedQueue.poll();
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Push model
    // -------------------------------------------------------------------------

    /**
     * Runs through the entire event queue and pushes each event to all registered
     * observers in chronological order (Observer / push model).
     * <p>
     * The queue is drained as events are dispatched. After this call the queue
     * will be empty.
     * </p>
     */
    public void runEventsList() {
        while (!eventQueue.isEmpty()) {
            Event e = eventQueue.poll();
            notifyObservers(e);
        }
    }
}