package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.Event;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

/**
 * An abstract trading agent that receives news and reacts by submitting ask or bid orders.
 * <p>
 * Trading behaviour is delegated to an {@link ITradingStrategy} (Strategy pattern).
 * The agent also implements {@link INewsObserver} so that it can be pushed events
 * by a {@link NewsBoard} (Observer pattern).
 * </p>
 */
public abstract class TradingAgent implements INewsObserver {

	protected Trader t;
	protected StockExchange exc;
	protected NewsBoard news;
	protected ITradingStrategy strategy;

	/**
	 * Constructor. Registers this agent as an observer of the {@link NewsBoard}.
	 *
	 * @param t   The {@link Trader} associated with this agent.
	 * @param e   The {@link StockExchange} at which the agent trades.
	 * @param n   The {@link NewsBoard} that generates events.
	 * @param s   The {@link ITradingStrategy} that defines how this agent reacts.
	 */
	public TradingAgent(Trader t, StockExchange e, NewsBoard n, ITradingStrategy s) {
		this.t = t;
		this.exc = e;
		this.news = n;
		this.strategy = s;
		// Observer registration at construction time, as required.
		n.registerObserver(this);
	}

	/**
	 * Sets (or replaces) the trading strategy at runtime.
	 *
	 * @param s The new {@link ITradingStrategy}.
	 */
	public void setStrategy(ITradingStrategy s) {
		this.strategy = s;
	}

	/**
	 * Called by the polling (pull) model as time advances.
	 * Kept for backward compatibility with pollingTest().
	 *
	 * @param time The new time.
	 */
	public void timeAdvancedTo(long time) {
		Event e = news.getEventAt(time);
		if (e != null) {
			examineEvent(e);
		}
	}

	/**
	 * Called by the NewsBoard (push/Observer model) when a new event is dispatched.
	 *
	 * @param e The {@link Event} pushed by the NewsBoard.
	 */
	@Override
	public void update(Event e) {
		examineEvent(e);
	}

	/**
	 * Checks whether this agent holds a position in the security mentioned in the event.
	 * If so, delegates reaction to the assigned strategy.
	 *
	 * @param e The event to examine.
	 */
	private void examineEvent(Event e) {
		int positionInSecurity =
				exc.getAccounts().getTraderAccount(t).getPosition(e.getSecrity().getTicker());
		if (positionInSecurity > 0) {
			strategy.actOnEvent(e, positionInSecurity,
					exc.getPrice(e.getSecrity().getTicker()));
		}
	}

	/**
	 * Returns the {@link Trader} associated with this agent.
	 *
	 * @return The trader.
	 */
	public Trader getTrader() {
		return t;
	}
}