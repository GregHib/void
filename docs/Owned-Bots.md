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
::companion summon|dismiss|call|follow|stop|guard|recover <slot|all>   ::companion task <slot> <task>   ::companion tasks
::companion goto <slot|all> <x> <y>   ::companion attack <slot|all> <npc index>   ::companion pickup <slot> <x> <y> <item>
::companion equip <slot> <item> [bank]   ::companion unequip <slot> <weapon|shield|...>   ::companion bank <slot>
::companion give|take <slot> <item> [amount|all] [bank]   ::companion item <slot> <inventory slot> <option>   ::companion use <slot> <from> <to>
::companion combat <slot> passive|melee|ranged|mage [spec=item eat=75 flee=25 warn=40 escape=item]   ::companion spec <slot|all> <item>|none
::companion spell <slot|all> <spell>|none   ::companion aggression <slot> passive|aggressive   ::companion loot <slot> on|off   ::companion rename <slot> <name>
::companion loadout save <name> [slot]|wear <slot> <name>|list|delete <name>   ::companion pay <slot> [coins|all]
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

**Wages** (`OwnedWages`): tasks are paid for, nothing else is. The owner pays coins from their pack into a slot's
coffer with `::companion pay` (`owned_bot_<slot>_coffer`, saved, kept through dismissal and death; no amount asks
for one). Each item a task gathers or makes (any of its activity's `produces` items) costs
`bots.owned.wage.percent` (default 20, 0 = free) of its price, `bots.owned.wage.price` = `trade` (the Grand
Exchange market price, `ExchangeHistory.marketPrice`) or `alch` (high alchemy, 60% of the store value), rounded up,
charged the moment it lands in the pack. Only an add counts: items moved in from the bank or handed over by the
owner are wrapped in `OwnedWages.receiving`, and a move between the pack's own slots is no new item. A task won't
start without five items' wages in hand (the dearest product), the bot warns once under ten items' worth, and a
coffer that runs dry stops the task the next tick (status Failed). Combat, following and world orders are free.

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
  style of the enemy npc's hardest attack from its combat table, the enemy being the worst of the npcs on
  the bot in multi-way, else the last to hit it, else its own target, dragonfire weighed against the bot's shield
  and antifire, decided once per enemy in `BotCombatContext.threat`; a player enemy is read live) plus a top-up (`BotOwnedHeal`:
  out of a fight, in every state, eat back to full with the food that wastes least; Saradomin brews only while a
  super restore is carried to undo the stat drain, a restore after three brews), and `BotEscape` (while
  fighting only: warn, then teleport home with an escape item, a free teleport when `bots.owned.freeEscape`
  allows one, else run). Escaping fails the frame with `Reason.Escaped`; `BotManager.failListener` reports
  every hard failure to the owner and drops the order.
- **Spells** ask only the level and the runes, never the owner's book: `::companion spell` autocasts a spell from
  whichever book has it (`BotOwnedSpell` opens that book on the bot), a mage with no pick mirrors the owner's
  autocast the same way, and a melee bot with 94 Magic and the runes keeps Vengeance up (`BotOwnedVengeance`,
  which opens the lunar book for it). Nothing of the owner's spellbook is mirrored.
- **Getting there**: the navigation graph's teleport shortcuts (`data/bot/teleport.shortcuts.toml`: Varrock and
  Camelot with the runes carried or fetched from the bank first, the Lumbridge home teleport) are virtual edges any
  bot takes when cheaper than walking, so a companion with Magic 45 and air and law runes in its owner's bank
  teleports to Camelot for Seers' Village. Falador and Ardougne wait on navigation graphs for their towns.
- **Task catalogue** (`OwnedTasks`): found, not listed. Every world-bot activity whose `produces` has an item and a
  non-combat skill, or an item alone (picking flax: "gathering"), is a task, grouped by that skill and by the folder of its area's file; `owned.tasks.toml` only
  names folders (`[locations]`), picks the verb a skill's labels start with (`[verbs]`) and tunes single
  activities (`label`, `location`, `hidden`). The skill is the last one an activity lists (chop-and-fletch is
  fletching), same-thing-same-place patches collapse to one task that remembers them all, the order sending the bot to
  the least used of them (world bots in its slots plus owned bots sent there, random among equals), and a product that isn't an item is named from
  the activity's id. `::companion tasks` prints `index|skill|level|location|members|label|wage` (wage: coins an item) and, for a task whose setup asks for
  items, `[BotTaskItems] index|id/id:amount,...` (the plugin's hover tip) (a game chat line
  wraps at the chat box's width, so no ids) and `::companion task <slot>` takes the index or the activity id.
- **Bank orders** (`BotOwnedErrand.carryOut`): a charged or dosed item the bank is out of or short of
  (`amulet_of_glory_4`, `super_restore_4`) is made up from its other charges or doses, best first, then the
  uncharged one; the equip order accepts the stand-in and the owner is told what went in place of what.
- **Recovery** after a flight or on `::companion recover` (`OwnedBots.recover`, status Recovering; the order is
  refused when the bot is full, has its prayer and wears its preset, and starts with `BotOwnedHomeTeleport`,
  the Lumbridge Home Teleport spell with its wait waived, so the trip is made in Lumbridge): eat what it carries, nearest bank
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

`data/bot/owned.varcs.toml`: `owned_bots_meta` (2000) then 50 ints a slot from 2001, then each slot's wage coffer
(2201-2204), then 42 a slot from 2205: a worn or carried item's whole amount when its packed variable (16 bits of
amount) can't hold it. Ints only (the Void
client does not extend varcstrings). Sent only after the plugin's handshake `::companion ui <\u0001><api version>`,
a control character no chat box can type, so a vanilla client never receives ids past its tables. The
plugin also reads `::companion tasks` lines (`[BotTasks] index|id|label`) and `[Bot n]` chat lines.

## Tests

`game/src/test/kotlin/content/bot/owned/OwnedBotsTest.kt`; the 634 client's `OwnedBotsTest` covers the
plugin's reading of the variables and lines.
