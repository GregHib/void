# Code Style Guide for LLMs

This guide describes how Void's Kotlin code is *structured and shaped*, beyond what
the linter checks. The goal is that generated code reads like the rest of the
codebase. Every rule below is drawn from existing content scripts — when in doubt,
open a neighbouring file in the same `content/` subdirectory and match it.

For build, module, and linter rules see `CLAUDE.md`. This document is only about how
code should *look* and *flow*.

---

## 1. Flatten with early exits, never nest deeply

This is the single most important structural rule. Guard against every disqualifying
condition first and `return` immediately. By the time you reach the "real" work, all
preconditions are known to hold and the body sits at the base indentation level.

Do **not** write:

```kotlin
if (a) {
    if (b) {
        // the main code
    }
}
```

Write:

```kotlin
if (!a) {
    return
}
if (!b) {
    return
}
// the main code
```

In event handlers the return is labelled, which is the dominant form across content:

```kotlin
onItem("modern_spellbook:*_level_alchemy") { item, id ->
    if (item.def.contains("destroy")) {
        message("This spell can not be cast on this item.")
        return@onItem
    }
    val spell = id.substringAfter(":")
    if (item.def.cost >= warningLimit) {
        // ...show warning...
        return@onItem
    }
    alch(this, spell, item)
}
```

Stack guards in sequence rather than combining them — each guard answers one question
and, where useful, emits its own user-facing message before returning:

```kotlin
if (hasClock("action_delay")) {
    return@interfaceOption
}
if (!inventory.contains("bones") && !inventory.contains("big_bones")) {
    message("You don't have any bones to cast this spell on.")
    return@interfaceOption
}
```

A guard may combine several *cheap, related* conditions on one line when they share a
single reason to bail (note: still one `return`, no nesting):

```kotlin
if (source == this || type == "deflect" || type == "poison" || damage < 1) {
    return@combatDamage
}
```

`return@label` can also carry the bail-out action inline, which keeps the guard to one
line:

```kotlin
is TransactionError.Full -> return@onFloorItemApproach inventoryFull("to hold that item")
```

---

## 2. Braces on every block, even one-liners

Single-statement `if` guards still use braces and a newline. The codebase does **not**
use brace-less `if (x) return`.

```kotlin
if (player.hasClock("action_delay")) {
    return
}
```

The exception is `when` arms, which are expression-style and may put the action on the
same line after `->` (see §4).

---

## 3. Scripts are thin registration shells; logic lives in helpers

A `Script` class is a registry. Its `init {}` block wires up event handlers and nothing
more. When a handler's body grows past a few statements, extract a private function (or
a `suspend` extension on `Player`) and call it from the handler. This keeps each `init`
readable as a table of contents.

```kotlin
class Alchemy : Script {
    init {
        onItem("modern_spellbook:*_level_alchemy") { item, id ->
            // ...guards + decide whether to warn...
            alch(this, spell, item)
        }
    }

    private fun alch(player: Player, spell: String, item: Item) {
        // the actual work
    }
}
```

Dialogue trees follow the same idea: shared branches become `suspend fun Player.x()`
extensions so options can reuse them instead of duplicating lines.

```kotlin
suspend fun Player.ghost() {
    npc<Angry>("Oh, the silly fool.")
    // ...
}
```

Keep helpers `private` unless another script in the package needs them. Define them as
extensions on the relevant entity (`Player`, `NPC`) so the call site reads naturally and
the receiver is implicit.

---

## 4. Prefer exhaustive `when` over if/else chains

When branching on a value — quest stage, transaction error, spell id — use `when`.
Handle the error/`else` cases explicitly so nothing is silently skipped. The transaction
pattern below is the canonical shape: every `TransactionError` variant is named, and the
success case is the only one that does work.

```kotlin
when (player.inventory.transaction.error) {
    is TransactionError.Deficient -> return
    is TransactionError.Full -> player.inventoryFull("room in your inventory")
    TransactionError.Invalid -> return
    TransactionError.None -> {
        // commit succeeded — do the rewards
    }
}
```

`when` as an expression is the idiomatic way to build a value from a state. Assign the
result rather than mutating a variable across branches:

```kotlin
val lines = when (quest("dorics_quest")) {
    "completed" -> listOf(/* ... */)
    "started" -> listOf(/* ... */)
    else -> listOf(/* ... */)
}
questJournal("Doric's Quest", lines)
```

Short same-line arms are fine and preferred for terminal/guard cases; reach for a
`{ ... }` block only when an arm needs multiple statements.

---

## 5. Order inside a handler: guards → setup → effects

Within a handler body the flow is consistent and worth matching:

1. **Guards** — every early `return` (§1).
2. **Local setup** — derive ids and values with `val` (`val spell = id.substringAfter(":")`).
3. **State mutation** — run the inventory transaction / set variables.
4. **Check the result** — `when` on the transaction error, or `if (!success) return`.
5. **Feedback effects** — animation, graphics, sound, xp, audit log, last.

```kotlin
start("action_delay", 1)
anim("bones_to_spell")
gfx("bones_to_spell")
sound("bones_to_spell")
exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
```

Effects come *after* success is confirmed, never before. Don't play an animation and
then discover the item couldn't be removed.

---

## 6. Use the receiver; don't re-qualify what `this` already gives you

Handler lambdas run with a `Player` (or other entity) receiver. Call its members
directly — `message(...)`, `inventory`, `anim(...)`, `face(...)` — instead of repeating a
`player.` prefix. Only name the receiver explicitly when you need it as an argument or
inside a nested lambda where `this` has changed:

```kotlin
AuditLog.event(this@onFloorItemApproach, "telegrab", floorItem, floorItem.tile)
```

Helper functions that *receive* a player as a parameter (rather than running on a
receiver) do qualify with the parameter name (`player.tab(...)`), as in `alch` above.
Match whichever the surrounding code uses.

---

## 7. Immutability and local derivation

Prefer `val`. Reach for `var` only for genuine loop/accumulator state, as in the
`while` loop that walks inventory indices:

```kotlin
var next = inventory.indexOf("bones")
while (next != -1 && !failed) {
    replace(next, "bones", produce)
    next = inventory.indexOf("bones")
}
```

Derive small values right where they're used with concise expressions and the standard
library (`substringAfter`, `coerceAtMost`, `indexOf`) rather than hand-rolled logic:

```kotlin
val coins = (item.def.cost * if (spell == "high_level_alchemy") 0.6 else 0.4).toInt()
val deflect = (10 + (damage / 10)).coerceAtMost(charges)
```

---

## 8. Naming and identifiers

Class names are `PascalCase` and describe the content unit (`RingOfRecoil`,
`TelekineticGrab`, `DoricsQuest`) — usually matching the file name one-to-one.

Game content is referenced by lowercase, snake_case **string ids**, not enums or
constants: `"ring_of_recoil"`, `"modern_spellbook:bones_to_*"`, `"action_delay"`. The
`book:component` form (colon-separated) and `*` wildcards in registration patterns are
the standard way to match interfaces and items. Keep these consistent with the YAML
config ids — don't invent new spellings.

Functions and locals are `camelCase` and read as verbs/nouns at the call site
(`removeSpellItems`, `requiredItem`, `directHit`).

---

## 9. Imports

Wildcard imports are allowed and used for tightly-related symbol groups, especially
dialogue:

```kotlin
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
```

Otherwise import specific members, including extension functions and `object` members
brought in by name (`import ...AddItem.add`, `import ...RemoveItem.remove`). Don't
fully-qualify a symbol inline when an import reads cleaner — the codebase imports the
function and calls it bare.

---

## 10. Comments are rare and purposeful

Content code is largely uncommented; the structure carries the meaning. When a comment
does appear it earns its place: a `// TODO` marking known-incomplete behaviour, or a
reference link justifying a piece of data (e.g. a video timestamp sourcing quest
dialogue). Don't narrate what the code already says.

```kotlin
"given_beads" -> emptyList<String>() // TODO proper message
// https://youtu.be/HM3xeOjl5Ww?t=9
```

---

## Checklist before finishing a content script

- [ ] Every disqualifying condition is an early `return@label` at the top — no `if`
      pyramids.
- [ ] `init {}` only registers handlers; multi-step logic is in `private` helpers /
      `suspend` extensions.
- [ ] Branching on a state uses an exhaustive `when` with explicit error/`else` arms.
- [ ] Transaction result is checked before any animation/sound/xp runs.
- [ ] Receiver members are called bare; no redundant `player.` qualifiers.
- [ ] `val` everywhere except real mutable loop state.
- [ ] Ids are lowercase snake_case strings matching config; class name matches file.
- [ ] No explanatory comments restating the code.
