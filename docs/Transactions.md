Transactions are the underlying mechanism behind [modifying inventories](inventories#inventory-modifications); they allow multiple modifications to be made on multiple inventories without making any changes until everything has been verified to have no errors. In short either all the modifications are applied at once, or none of them are.

## Example

Let's see how this works in a hypothetical example:

```kotlin
val backpack = Inventory(arrayOf(Item("coins", 50))) // Start with 50 coins in our backpack
val bank = Inventory(arrayOf(Item.EMPTY))  // Start with nothing in the bank

// Begin a transaction
backpack.transaction {
    add("coins", 50) // Add 50 coins
    move("coins", 75, bank) // Move 75 coins to another container, in this case a bank for safe keeping
    remove("coins", 50) // Remove 50 coins for a fish
    add("raw_herring") // Add the fish
}

// As the transaction failed neither of the inventories have been changed:
println(backpack[0]) // Item(id = 'coins', amount = 50)
println(bank[0]) // Item(id = '', amount = 0)
```

As 75 of the coins were moved to the bank, there are only 25 coins left in the inventory which is not enough for the 50 coin fish. As one step of the transaction had an error the transaction was not successful, we can check the transactions error with `transaction.error`.

> [!TIP]
> Checkout [TransactionErrors.kt](https://github.com/GregHib/void/blob/3231e13f54c7fb69832bfb7f0a84edbe7b0dc891/engine/src/main/kotlin/world/gregs/voidps/engine/inv/transact/TransactionError.kt#L6) for more info about the different types of errors and what they mean.

```kotlin
when (backpack.transaction.error) {
    TransactionError.None -> {
        npc<Happy>("Enjoy the fish!")
    }
    TransactionError.Deficient -> {
        player<Upset>("Oh dear I don't actually seem to have enough money.")
    }
}
```

As the transaction wasn't successful nothing will have changed, `backpack` will have 50 coins, and `bank` will be empty.

## Operations

Much like [inventory helper functions](inventories#inventory-modifications) there are a lot of different ways of modifying an inventory. Every operation used during a transaction is only applied if all operations are successful, and each individual change made to an inventory is tracked.

> [!NOTE]
> See the full list of operations with descriptions in [`/inv/transact/operation`](https://github.com/GregHib/void/tree/main/engine/src/main/kotlin/world/gregs/voidps/engine/inv/transact/operation).

### Item changes

Item changes are used to track if an item was added to an inventory, optionally at a specific slot.

```kotlin
itemAdded("*_tiara", EquipSlot.Hat, "worn_equipment") { player ->
    player["${item.id.removeSuffix("_tiara")}_altar_ruins"] = true
}

itemRemoved("*_tiara", EquipSlot.Hat, "worn_equipment") { player ->
    player["${item.id.removeSuffix("_tiara")}_altar_ruins"] = false
}
```

All changes, additional and removals from an inventory can also be tracked

```kotlin
itemChange("bank") { player: Player ->
    player["bank_spaces_used_free"] = player.bank.countFreeToPlayItems()
    player["bank_spaces_used_member"] = player.bank.count
}
```

## Linking inventories

While a transaction are started from one inventory it is possible to link other inventories and apply operations to them as part of the same transaction.

```kotlin
player.inventory.transaction {
    val added = removeToLimit(id, amount)
    val transaction = link(player.offer)
    transaction.add(id, added)
}
```

`link()` returns the transaction for the linked inventory on which you can apply operations on like normal. If an operation fails in a linked transaction the parent transaction will also fail.




