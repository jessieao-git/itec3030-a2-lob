package ca.yorku.cmg.lob.stockexchange.tradingagent;

import ca.yorku.cmg.lob.stockexchange.StockExchange;
import ca.yorku.cmg.lob.stockexchange.events.NewsBoard;
import ca.yorku.cmg.lob.trader.Trader;

/**
 * Concrete implementation of {@link AbstractTradingAgentFactory}.
 * <p>
 * Creates a {@link TradingAgent} of the requested {@code type} ("Institutional" or "Retail")
 * equipped with the requested {@code style} strategy ("Conservative" or "Aggressive").
 * </p>
 */
public class TradingAgentFactory extends AbstractTradingAgentFactory {

    /**
     * Creates and returns a fully configured {@link TradingAgent}.
     *
     * @param type  "Institutional" → {@link TradingAgentInstitutional};
     *              "Retail"        → {@link TradingAgentRetail}.
     * @param style "Conservative"  → {@link ConservativeStrategy};
     *              "Aggressive"    → {@link AggressiveStrategy}.
     * @param t     The {@link Trader} associated with the new agent.
     * @param e     The {@link StockExchange} in which the agent will trade.
     * @param n     The {@link NewsBoard} the agent will observe.
     * @return A new {@link TradingAgent} instance, or {@code null} if an unknown
     *         type or style string is supplied.
     */
    @Override
    public TradingAgent createAgent(String type, String style, Trader t, StockExchange e, NewsBoard n) {

        // 1. Build the strategy
        ITradingStrategy strategy;
        switch (style) {
            case "Conservative":
                strategy = new ConservativeStrategy(t, e);
                break;
            case "Aggressive":
                strategy = new AggressiveStrategy(t, e);
                break;
            default:
                System.err.println("TradingAgentFactory: unknown style '" + style + "'");
                return null;
        }

        // 2. Build the agent with the strategy injected
        switch (type) {
            case "Institutional":
                return new TradingAgentInstitutional(t, e, n, strategy);
            case "Retail":
                return new TradingAgentRetail(t, e, n, strategy);
            default:
                System.err.println("TradingAgentFactory: unknown type '" + type + "'");
                return null;
        }
    }
}