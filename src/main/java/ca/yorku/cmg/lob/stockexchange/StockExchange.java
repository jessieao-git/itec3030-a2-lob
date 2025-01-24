/**
 * Reads the accounts list from a file and populates the exchange.
 * Uses {@link TradingAgentFactory} to instantiate the correct agent type
 * and strategy based on file data (Abstract Factory pattern).
 *
 * @param path the path to the accounts list file
 */
public void readAccountsListFromFile(String path) {
	TradingAgentFactory factory = new TradingAgentFactory();
	try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		String line;
		boolean isFirstLine = true; // Skip header

		while ((line = br.readLine()) != null) {
			if (isFirstLine) {
				isFirstLine = false;
				continue;
			}
			String[] parts = line.split(",", -1);
			if (parts.length >= 5) {
				String traderTitle  = parts[0].trim();
				String traderType   = parts[1].trim();   // "Retail" | "Institutional"
				String accType      = parts[2].trim();   // "Basic"  | "Pro"
				long   initBalance  = Long.parseLong(parts[3].trim());
				String tradingStyle = parts[4].trim();   // "Conservative" | "Aggressive"

				Trader t;
				if (traderType.equals("Retail")) {
					t = new TraderRetail(traderTitle);
				} else {
					t = new TraderInstitutional(traderTitle);
				}

				if (accType.equals("Basic")) {
					accounts.addAccount(new AccountBasic(t, initBalance));
				} else {
					accounts.addAccount(new AccountPro(t, initBalance));
				}

				// Factory creates the right TradingAgent subtype with the right strategy.
				TradingAgent agent = factory.createAgent(traderType, tradingStyle, t, this, newsDesk);
				if (agent != null) {
					traders.add(agent);
				}

			} else {
				System.err.println("Skipping malformed line (too few attributes): " + line);
			}
		}
	} catch (IOException e) {
		e.printStackTrace();
	}
}