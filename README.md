# GlobalMarket
**GlobalMarket** replaces static, admin-defined price lists with a real-time **Automated Market Maker (AMM)** system - the same technology powering modern real-world financial exchanges, but with Minecraft items!

Prices fluctuate organically based on supply and demand. If players buy up all the diamonds, the price naturally skyrockets. If they flood the market with iron, the price drops. 

### How It Works: The Liquidity Pool
Instead of setting a hard price for items, admins seed **Liquidity Pools** (e.g., 100 Diamonds + $5,000). 

The plugin uses the **Constant Product Market Maker** formula to calculate prices on the fly:
- **Spot Price** = Currency Pool / Item Pool (e.g., $5000 / 100 = $50 per Diamond).
- **Price Impact**: Buying items removes them from the pool, making the next one more expensive. Selling items adds them to the pool, making the next one cheaper.

This creates a deeply immersive economy where bulk traders have to consider **slippage**, and market arbitrage becomes a viable playstyle!

### Features
- **Dynamic Pricing:** No static shop menus. Prices update in real-time based on pool liquidity.
- **Constant Product AMM Math:** Ensures the pool can never be completely drained of currency or items.
- **Configurable Trading Fees:** Add a percentage fee to buys and sells. Fees are automatically reinvested into the liquidity pool, growing the market's depth over time.
- **Vault Integration:** Seamlessly hooks into your server's existing economy.
- **YAML-Based Configuration:** Easily create, seed, and manage markets via `markets.yml`.
- **In-Game Market Creation:** Admins can create and seed new markets on the fly without restarting the server.

### Commands
All commands stem from `/market`.

**Player Commands:**
- `/market info <item>` — View the current spot price, liquidity pool sizes, and trading fee.
- `/market buy <item> <amount>` — Purchase items from the market pool. The cost is calculated dynamically.
- `/market sell <item> <amount>` — Sell items to the market pool. The payout is calculated dynamically.

**Admin Commands:** (`globalmarket.admin` permission)
. `/market create <item> <currency_amount> <item_amount>` — Create and seed a new liquidity pool.


### Permissions
- `globalmarket.use` — Allows players to use `/market info`, `/market buy`, and `/market sell`. (Default: true)
- `globalmarket.admin` — Allows admins to use `/market create`. (Default: op)

---

**Note:** This plugin requires [Vault](https://www.spigotmc.org/resources/vault.34315/) and a compatible economy plugin to function, such as LiteEco or EssentialsX.

### License
Copyright (C) 2026 duggavo

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
