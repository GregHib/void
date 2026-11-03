# Player-owned bots

Players buy up to four bots from a Bot Broker (Lumbridge, Varrock, Falador). A bot is a world bot
(`content/bot`) on a `DummyClient` that mirrors its owner's skills and variables and whose bank *is* the
owner's bank. It only ever does what the owner orders through `::bot` commands; the 634 client's Owned Bots
plugin drives the same commands and reads the state the server shows it in client variables.

Off by default: `bots.owned.enabled=false`. Settings are the `bots.owned.*` block in `game.properties`.

## Commands

```
::bot summon|dismiss|call|follow|stop|hold <slot|all>   ::bot task <slot> <task>   ::bot tasks
::bot goto <slot|all> <x> <y>   ::bot attack <slot|all> <npc index>   ::bot pickup <slot> <x> <y> <item>
::bot equip <slot> <item> [bank]   ::bot unequip <slot> <weapon|shield|...>   ::bot bank <slot>
::bot give|take <slot> <item> [amount|all] [bank]   ::bot item <slot> <inventory slot> <option>   ::bot use <slot> <from> <to>
::bot combat <slot> passive|melee|ranged|mage [spec=item eat=40 flee=25 warn=40 escape=item]   ::bot spec <slot|all> <item>|none
::bot aggression <slot> passive|aggressive   ::bot loot <slot> on|off   ::bot rename <slot> <name>
::bot loadout save <name> [slot]|wear <slot> <name>|list|delete <name>
::bot status   ::bot show <slot>   ::bot help
::owned_bots list|inspect <player>|dismiss <player> <slot>|refund <player> <slot>   (admin)
```

Items from the owner's inventory are handed over in person: the owner walks to the bot and pickpockets it.
Items to or from the bank are the bot's errand: it walks to the nearest bank, uses the booth, does everything
queued in one trip and goes back to its previous order. A stack given or taken without an amount asks how many.
A bot puts on or takes off what it already carries by itself, wherever it is. Loadout presets (sets of worn gear,
shared by all the owner's bots, `owned_loadouts`) are worn by the same errand; what a bot wore when it last left
the world (`owned_bot_<slot>_worn`) is put back on at the next summon. A summoned or called bot appears on a free
tile beside the owner and follows. A held bot keeps its place but fights as a follower would. `goto`, `attack`
and `pickup` are one-off orders (the plugin's "Send here", "Attack with" and "Pick up with" entries): the bot
goes back to following or holding when done (`BotOwnedResume`). A bot only picks up what it can see: an item
the owner dropped stays the owner's alone for its first minute. `spec` tags a bot's special attack weapon: it
switches to it for its special when it carries or wears one, and the tag is inert until it does.

## How it works

- **Roster** (`OwnedRoster`): persistent owner variables `owned_bot_<slot>_*` (`data/bot/owned.vars.toml`).
  A slot is alive from purchase until the bot dies. Dismissing or logging out keeps it; a dead slot is bought
  again. Slots are bought in order and each costs more.
- **Summon** (`OwnedBots.summon`): a fresh `Player` with the owner's display name choice, connected like a
  world bot, then `OwnedBotSetup.apply` copies persistent variables (minus a blacklist), skills, looks, and
  aliases the bot's `bank` inventory instance to the owner's `Inventory`. Nothing a new account is given
  survives (no starter kit, items cleared).
- **Orders** pin the bot (`Bot.pinned`) to one activity: `owned_idle`, a per-bot follow activity built by
  `OwnedProfiles` from the owner's combat profile and knobs, or `owned:<task>`, a copy of the world activity
  named in `data/bot/owned.tasks.toml` with skill maximums removed. `Bot.pinnedSetup` keeps setup resolvers on.
- **Resolvers** (`OwnedResolvers`): owned bots never open the bank interface (its tab counters are owner
  variables) and never shop. They deposit and withdraw by direct transaction beside a bank
  (`BotOwnedDeposit`, `BotOwnedWithdraw`, `BotOwnedEquip`); a tool the bank lacks fails the task with a
  message to the owner.
- **Combat**: `BotFollowOwner` (Follow option, recall when far or stuck), reactive `BotAssistOwner`
  (aggressive: the owner's target or, in single-way combat, the nearest NPC of the same id; passive: only what
  attacks the owner in multi-way), the PvP bots' eat/pray/potion/spec reactives, and `BotEscape` (warn, then
  teleport home with an escape item, a free teleport when `bots.owned.freeEscape` allows one, else run to the
  nearest bank). Escaping fails the frame with `Reason.Escaped`; `BotManager.failListener` reports every hard
  failure to the owner and drops the order.
- **Leaving the world**: `sweep` deposits everything worn and carried into the owner's bank (overflow to the
  owner's inventory, then the floor), then the bot is removed next tick. Death does the same and kills the slot.
  Every `bots.owned.snapshotTicks` the bot's items are written into the owner's save; an unswept snapshot is
  restored to the bank at the owner's next login.
- **Guards**: PvP areas drop the order and recall the bot; random events, bot chat, trades, the web's online
  count and map skip owned bots; owned bots don't take `bot_capacity` slots.

## Client variables

`data/bot/owned.varcs.toml`: `owned_bots_meta` (2000) then 50 ints a slot from 2001. Ints only (the Void
client does not extend varcstrings). Sent only after the plugin's handshake `::bot ui <\u0001><api version>`,
a control character no chat box can type, so a vanilla client never receives ids past its tables. The
plugin also reads `::bot tasks` lines (`[BotTasks] index|id|label`) and `[Bot n]` chat lines.

## Tests

`game/src/test/kotlin/content/bot/owned/OwnedBotsTest.kt`; the 634 client's `OwnedBotsTest` covers the
plugin's reading of the variables and lines.
