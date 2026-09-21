# Inventories

Inventories are a fixed size grid of slots capable of holding an amount of items e.g. `Item("bucket_noted", 15)`. The player backpack (aka *the* inventory), worn equipment, and beast of burden are examples of inventories used by players. [Shops](shops) are an example of inventories used by npcs. Inventory definitions are found in [`*.invs.toml`](https://github.com/GregHib/void/blob/main/data/entity/player/bank/bank.invs.toml) files.

## Item stack behaviour

Items come in two categories: stackable and non-stackable. Stackable items can have up to [2,147 million](https://en.wikipedia.org/wiki/2,147,483,647) items within one slot. Non-stackable items can only have 1 item per slot. 

This means having two non-stackable items like a `bronze_dagger` will take up two slots, where two stackable `bronze_dagger_noted` will take up only one slot.

![Screenshot 2024-02-19 174140](https://github.com/GregHib/void/assets/5911414/c844fd42-e06a-4f16-a486-476605eda491)

## Stack modes

Each inventory has a stack mode which can change the stacking behaviour, banks for example will always stack items even if they are non-stackable.

```toml
[bank]
id = 95
stack = "Always"
```

![Screenshot 2024-02-19 175815](https://github.com/GregHib/void/assets/5911414/c93b557f-5b6b-4e47-938f-210ac869bf16)

| Stack mode | Behaviour |
|---|---|
| DependentOnItem | The default; stacks stackable items, doesn't stack non-stackable items |
| Always | Always stacks items regardless if they are stackable or non-stackable |
| Never | Never stacks items even if they are stackable |

> [!NOTE]
> Custom stacking rules can also be created, see [ItemStackingRule.kt](https://github.com/GregHib/void/blob/c4d6e458984c742d2d68a6cd685727ee9bbeda0d/engine/src/main/kotlin/world/gregs/voidps/engine/inv/stack/ItemStackingRule.kt#L8).

An item can be checked if stackable against a certain inventory using the `stackable()` function:
 
```kotlin
val stackable = player.bank.stackable("bronze_dagger") // true
```

## Getting an inventory

Player's have a map of inventories connected to them, you can access one by calling:

```kotlin
val equipment = player.inventories.inventory("worn_equipment")
```

> [!TIP]
> A lot of the commonly used player inventories have helper functions for easy access e.g. `player.equipment`
> ```kotlin
> val Player.equipment: Inventory
>     get() = inventories.inventory("worn_equipment")
> ```

## Finding an item

You can check if an inventory has an item or a number of items using `contains`

```kotlin
if (!inventory.contains("tinderbox")) {
    message("You don't have the required items to light this.")
    return
}
```

And count exactly how many of an item is in an inventory, regardless of stack behaviour.

```kotlin
val goldBars = player.inventory.count("gold_bar")
```

## Free space

There are a number of functions to help manage the free slots in an inventory.

| Function | Description |
|---|---|
| `inventory.isEmpty()` | Check if an inventory has no items inside. |
| `inventory.spaces` | The number of empty slots. |
| `inventory.isFull()` | Check if an inventory has no empty slots. |
| `inventory.count` | The number of item slots in use. |
| `inventory.freeIndex()` | Get the index of an empty slot. |
| `item.isEmpty()` | Check if an item slot is empty. |
| `item.isNotEmpty()` | Check if an item slot is not empty. |

# Inventory Modifications

## Adding items

Adding a single item or a stack of items is simple with the `add` helper function

```kotlin
player.inventory.add("coins", 100)
```

If an inventory is full the item won't be added and `add` will return false allowing you to run different code depending on the outcome.

```kotlin
if (player.inventory.add("silverlight")) {
    npc<Furious>("Yes, I know, someone returned it to me. Take better care of it this time.")
} else {
    npc<Furious>("Yes, I know, someone returned it to me. I'll keep it until you have free inventory space.")
}
```

> [!NOTE]
> Adding two non-stackable items to a normal inventory will add the items to two empty slots

## Removing items

Removing items is much the same as adding, the `remove` item will return true when the required number of items was removed, and false when the inventory doesn't have the required amount.

```kotlin
player.inventory.remove("coins", 123)
```

## Clearing items

Clearing will remove an item from a given slot, regardless of amount

```kotlin
player.inventory.clear(4)
```

Clear without a slot will remove all items from an inventory

```kotlin
player.loan.clear()
```

## Replacing items

Replace is commonly used to switch one non-stackable item for another of a different type

```kotlin
inventoryItem("Empty", "pot_of_flour", "inventory") {
    player.inventory.replace("pot_of_flour", "empty_pot")
}
```

## Moving items

Move will take all of the items on one slot add it to another slot in the same or a different inventory only if the target slot is empty.

```kotlin
inventoryOption("Remove", "worn_equipment") {
    val success = player.equipment.move(slot, player.inventory) // true
}
```

## Other modifications

There are plenty of other helper functions to explore that should cover the majority of use-cases, for a full list see [InventoryOperations.kt](https://github.com/GregHib/void/blob/main/engine/src/main/kotlin/world/gregs/voidps/engine/inv/InventoryOperations.kt).

| Function name | Description |
|---|---|
| `swap` | Swap the items in two slots, in the same or different inventories. |
| `moveAll` | Move all items to another inventory |
| `moveToLimit` | Move as many items as possible until an amount given. |
| `shift` | Move an item along a row; used in banking |
| `removeToLimit` | Remove as many items as possible until an amount given. |

> [!IMPORTANT]
> For advanced modifications including combining multiple and cross-inventory operations see [Transactions](transactions).