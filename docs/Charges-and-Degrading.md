# Item Charges and Degrading

Charges are a number stored with an item to store to track depletion and degradation, they are configured in item definitions.

## Overview

Items can have a finite number of charges that are consumed over time or through use. When charges run out, the item might degrade replacing itself with a different item (a damaged or broken variant), or being destroyed outright. 

There are two types of charges:

- **Inline charges** - Stored directly using the item's stack value (`item.value`). Each item slot tracks its own charges independently.
- **Variable charges** - Stored in a [player variable](character-variables) (`player[variable]`). Meaning all identical items in different inventories (e.g. equipped vs. banked) share the same charge value.

## Item Definition Fields

Charge-related behaviour is declared in `*.items.toml` item config files under `data/`.

| Field | Type | Description |
|---|---|---|
| `charges` | `Int` | The item's default/starting charge count. For inline items this is also the max. For variable items, this is the reset value when the item degrades. |
| `charges_max` | `Int` | (Variable items only) The upper limit for charges. If omitted, `charges` is used as the cap. |
| `charge` | `String` | Name of the player variable that stores charges. |
| `degrade` | `String` | The item ID to replace this item with when it runs out of charges. Use `"destroy"` to delete the item instead. |
| `degrade_message` | `String` | Chat message sent to the player when the item degrades. Only fires when the item reaches 0 charges and transitions to the degrade target. |
| `deplete` | `String` | How the item is drained automatically by `Degradation.kt`. See depletion modes below. |

### Depletion Modes (`deplete`)

The `Degradation` script monitors equipped items in Hat, Weapon, Chest, Shield, and Legs slots. The `deplete` field controls when `discharge` is called automatically:

| Value | When charges are removed |
|---|---|
| `combat` | Once per game tick while the player is in combat |
| `equip` | Once per game tick while the item is worn (regardless of combat) |
| `per_hit` | Each time the player receives damage |
| `per_attack` | Each time the player lands a hit on an enemy |
| `teleport` | Not handled by `Degradation`; must be managed manually by the script |

Items with `deplete = "combat"` or `deplete = "equip"` start a `"degrading"` [soft timer](timers) when equipped. The timer fires every tick and calls `discharge` on the appropriate slots.

## Charge Storage: Inline vs. Variable

### Inline Charges

Charges are stored in `item.value` (the item's stack amount field). The item itself holds its state, so two copies of the same item can have different charge levels.

```toml
[medium_pouch]
id = 5510
charges = 45
degrade = "medium_pouch_damaged"
degrade_message = "Your pouch has decayed through use."
```

Here `charges = 45` is both the starting count and the maximum. When the item reaches 0 charges it is replaced by `medium_pouch_damaged`.

### Variable Charges

Charges are stored in a named player variable rather than on the item. This is used when the item has a large charge range, needs to persist across sessions, or where an `item.value` of 0 would conflict with stack quantities.

```toml
[camulet]
id = 6707
charges = 4          # reset value after degrade (and starting value)
charge = "camulet_charges"  # player variable name
```

The corresponding variable must be declared in `data/entity/player/charges.vars.toml`:

```toml
[camulet_charges]
format = "int"
persist = true
```

For items that can be charged up from 0, use `charges = 0` (starting value) and `charges_max` for the cap:

```toml
# dungeoneering nature staff
charges = 0
charges_max = 1000
charge = "nature_staff_charges"
```

## Multi-Stage Degradation (Barrows-style)

Some items degrade through several intermediate states before becoming broken. Each stage is a separate item definition that points to the next:

```toml
[ahrims_hood]          # clean, unworn version (charges = 1, just a flag)
charges = 1
degrade = "ahrims_hood_100"
deplete = "combat"

[ahrims_hood_100]      # worn, full charges
charges = 22500
degrade = "ahrims_hood_75"
deplete = "combat"

[ahrims_hood_75]
clone = "ahrims_hood_100"
degrade = "ahrims_hood_50"

[ahrims_hood_50]
clone = "ahrims_hood_100"
degrade = "ahrims_hood_25"

[ahrims_hood_25]
clone = "ahrims_hood_100"
degrade = "ahrims_hood_broken"

[ahrims_hood_broken]   # no charges field - no further degradation
```

The `clone` field copies all fields from the named item, so each 25% stage only needs to override `id` and `degrade`. When `ahrims_hood` (with `charges = 1`) is first equipped and its single charge is consumed, it is replaced by `ahrims_hood_100` (22,500 charges). Each subsequent depletion cycle counts down from 22,500.

## Charge API

The charge system is accessed through extension functions on `Inventory` and `Item`. All mutations go through the transaction system.

### Reading Charges

```kotlin
// From an inventory slot
val current = equipment.charges(player, EquipSlot.Ring.index)

// From the item directly (inline-only, no player variable lookup)
val count = item.charges()

// From the item with player variable support
val count = item.charges(player)
```

### Adding Charges

```kotlin
// Add exactly `amount` charges; fails (TransactionError.Full) if it would overflow
inventory.charge(player, slot, amount)

// Add up to `amount` charges, stopping at the maximum; returns how many were added
transaction { chargeToLimit(slot, amount) }
```

### Removing Charges

```kotlin
// Remove exactly `amount` charges; triggers degrade if charges hit 0
inventory.discharge(player, slot, amount)

// Remove up to `amount` charges, stopping at 0; returns how many were removed
transaction { dischargeToLimit(slot, amount) }
```

`discharge` returns `true` on success. When charges reach 0, the engine automatically replaces the item with the `degrade` target (or destroys it if `degrade = "destroy"`), then sends the `degrade_message` if configured.

### Clearing All Charges

```kotlin
// Set charges to 0 immediately (no degrade triggered by this call)
inventory.clearCharges(player, slot)
```

### Setting Charges Directly

```kotlin
// Set to an exact value (0 to max); fails if amount > maximum
transaction { setCharge(slot, amount) }
```

## Examples

### Ring of Recoil - inline charges, discharged per hit received

```kotlin
combatDamage { (source, type, damage) ->
    val charges = equipment.charges(this, EquipSlot.Ring.index)
    val deflect = (10 + (damage / 10)).coerceAtMost(charges)
    if (equipment.discharge(this, EquipSlot.Ring.index, deflect)) {
        source.directHit(deflect, "deflect", source = this)
    }
}
```

The ring definition sets `charges = 40` and `degrade = "destroy"` (it shatters). `discharge` removes the deflect amount and destroys the ring when charges hit 0.

### Ring of Life - single-use item (inline, `charges = 1`)

```kotlin
private fun activateRingOfLife(player: Player) {
    if (!player.equipment.discharge(player, EquipSlot.Ring.index)) return
    Teleport.teleport(player, destination, "jewellery")
}
```

No `degrade` target is set in the item definition, so the item is simply removed on depletion.

### Camulet - variable charges, recharged by script

```kotlin
// Check charges
val charges = inventory.charges(this, slot)

// Recharge by setting the player variable directly
set("camulet_charges", 4)

// Teleport (discharges one charge)
jewelleryTeleport(this, inventory, slot, Areas["camulet_teleport"])
```

The `camulet_charges` variable persists across logins. The script sets it directly when recharging with camel dung, bypassing the transaction layer since the recharge logic is custom.

### Essence Pouch - inline charges used as a durability counter

```kotlin
// On empty, discharge once to track degradation uses
if (Settings["runecrafting.pouch.degrade", true]) {
    inventory.discharge(this, slot)
}
```

The pouch's `charges` field counts how many more times it can be emptied before degrading to a damaged variant. The damaged variant has its own `charges` value and degrades to `"destroy"`.

## Adding a New Chargeable Item

1. **Define the item** in the appropriate `.toml` file with `charges`, and optionally `degrade`, `degrade_message`, and `deplete`.
2. **If using variable charges**, add the variable to `data/entity/player/charges.vars.toml` with `persist = true`.
3. **If the item degrades automatically** (worn equipment), set `deplete` to the appropriate mode. No script changes are needed - `Degradation.kt` handles it.
4. **If the item depletes through script actions** (teleports, special attacks, etc.), call `inventory.discharge(player, slot)` at the appropriate point in your script.
5. **Define degrade targets** as separate item entries. Use `clone = "base_item_id"` to inherit fields and only override what changes between stages.
