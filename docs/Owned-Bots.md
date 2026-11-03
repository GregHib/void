# Player-owned bots

Players know them as **companions**: the command is `::companion` (`::bot` and `::recruit` do the same), the
client plugin is "Companions", and chat lines about one begin `[Companion n]`. The code keeps the "owned bot"
names.

Players hire up to four bots from the Adventurers in the Servants' Guild, East Ardougne, where the game's hired
help is hired. Each of the four stands for one slot (in spawn order) and is seen only by players with that slot free: the one
you hire is gone for you until that bot dies, while everyone else still sees it (`NPC.visibleTo`). A newly hired bot carries a
new player's starter items. A bot is a world bot
(`content/bot`) on a `DummyClient` that mirrors its owner's skills and variables and whose bank *is* the
owner's bank. It only ever does what the owner orders through `::companion` commands; the 634 client's Owned Bots
plugin drives the same commands and reads the state the server shows it in client variables.

Off by default: `bots.owned.enabled=false`. Settings are the `bots.owned.*` block in `game.properties`.

## Commands

```
::companion summon|dismiss|call|follow|stop|guard <slot|all>   ::companion task <slot> <task>   ::companion tasks
::companion goto <slot|all> <x> <y>   ::companion attack <slot|all> <npc index>   ::companion pickup <slot> <x> <y> <item>
::companion equip <slot> <item> [bank]   ::companion unequip <slot> <weapon|shield|...>   ::companion bank <slot>
::companion give|take <slot> <item> [amount|all] [bank]   ::companion item <slot> <inventory slot> <option>   ::companion use <slot> <from> <to>
::companion combat <slot> passive|melee|ranged|mage [spec=item eat=75 flee=25 warn=40 escape=item]   ::companion spec <slot|all> <item>|none
::companion aggression <slot> passive|aggressive   ::companion loot <slot> on|off   ::companion rename <slot> <name>
::companion loadout save <name> [slot]|wear <slot> <name>|list|delete <name>
::companion status   ::companion show <slot>   ::companion help
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
goes back to following or guarding when done (`BotOwnedResume`). A bot only picks up what it can see: an item
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
  attacks the owner in multi-way), the PvP bots' eat/pray/potion/spec reactives (protection prayers pick the
  style of the enemy npc's hardest attack from its combat table, dragonfire weighed against the bot's shield
  and antifire, decided once per enemy in `BotCombatContext.threat`; a player enemy is read live) plus a top-up (`BotOwnedHeal`:
  out of a fight, in every state, eat back to full with the food that wastes least; Saradomin brews only while a
  super restore is carried to undo the stat drain, a restore after three brews), and `BotEscape` (while
  fighting only: warn, then teleport home with an escape item, a free teleport when `bots.owned.freeEscape`
  allows one, else run). Escaping fails the frame with `Reason.Escaped`; `BotManager.failListener` reports
  every hard failure to the owner and drops the order.
- **Bank orders** (`BotOwnedErrand.carryOut`): a charged or dosed item the bank is out of or short of
  (`amulet_of_glory_4`, `super_restore_4`) is made up from its other charges or doses, best first, then the
  uncharged one; the equip order accepts the stand-in and the owner is told what went in place of what.
- **Recovery** after a flight (`OwnedBots.recover`, status Recovering): eat what it carries, nearest bank
  (`BotOwnedRecover`: the preset last loaded with `::companion loadout load` goes back on, then food from the
  owner's bank, the preset's or the bank's best), eat to full, prayer at the nearest `altar`-tagged area
  (`BotOwnedPrayAltar`: the Lumbridge, Varrock, Seers' Village, Port Sarim and Ardougne churches, the Ardougne
  and Edgeville monasteries; the trip needs a navigation-graph node inside the area, which only Lumbridge has yet), then back to its orders: a task carries on, following and
  guarding both come back as following (`BotFollowOwner` recalls it to the owner). A recovery that fails
  (no bank reachable) goes straight back to the orders with a message.
- **Leaving the world**: `sweep` deposits everything worn and carried into the owner's bank (overflow to the
  owner's inventory, then the floor), then the bot is removed next tick. Death does the same and kills the slot.
  Every `bots.owned.snapshotTicks` the bot's items are written into the owner's save; an unswept snapshot is
  restored to the bank at the owner's next login.
- **Guards**: PvP areas drop the order and recall the bot; random events, bot chat, trades, the web's online
  count and map skip owned bots; owned bots don't take `bot_capacity` slots.

## Client variables

`data/bot/owned.varcs.toml`: `owned_bots_meta` (2000) then 50 ints a slot from 2001. Ints only (the Void
client does not extend varcstrings). Sent only after the plugin's handshake `::companion ui <\u0001><api version>`,
a control character no chat box can type, so a vanilla client never receives ids past its tables. The
plugin also reads `::companion tasks` lines (`[BotTasks] index|id|label`) and `[Bot n]` chat lines.

## Tests

`game/src/test/kotlin/content/bot/owned/OwnedBotsTest.kt`; the 634 client's `OwnedBotsTest` covers the
plugin's reading of the variables and lines.
