package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.orderbook.Ask;
import ca.yorku.cmg.lob.orderbook.Bid;
import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.BadNews;
import ca.yorku.cmg.lob.stockexchange.events.Event;
import ca.yorku.cmg.lob.stockexchange.events.GoodNews;
import ca.yorku.cmg.lob.trader.Trader;
import ca.yorku.cmg.lob.tradestandards.IOrder;

/**
 * A conservative trading strategy: reacts cautiously to news with small order quantities.
 * <ul>
 *   <li>GoodNews: bids 20% of position at 5% above current price.</li>
 *   <li>BadNews:  asks 20% of position at 5% below current price.</li>
 * </ul>
 */
public class TradingConservative implements ITradingStrategy {

	private Trader trader;
	private StockExchange exchange;

	/**
	 * Constructor — strategy needs trader and exchange to submit orders.
	 * @param t The trader on whose behalf orders are submitted.
	 * @param e The exchange to which orders are submitted.
	 */
	public TradingAgentConservative(Trader t, StockExchange e) {
		this.trader = t;
		this.exchange = e;
	}

	@Override
	public void actOnEvent(Event e, int pos, int price) {
		IOrder newOrder = null;

		if (e instanceof GoodNews) {
			newOrder = new Bid(trader, e.getSecrity(),
					(int) Math.round(price * 1.05),
					(int) Math.round(pos * 0.2),
					e.getTime());
		} else if (e instanceof BadNews) {
			newOrder = new Ask(trader, e.getSecrity(),
					(int) Math.round(price * 0.95),
					(int) Math.round(pos * 0.2),
					e.getTime());
		} else {
			System.out.println("ConservativeStrategy: Unknown event type");
		}

		if (newOrder != null) {
			exchange.submitOrder(newOrder, e.getTime());
		}
	}
}