package content.minigame.vinesweeper

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Flags are sold by both Mrs Winkin and Farmer Blinkin: the first ten are free, replacements cost coins.
 */
suspend fun Player.buyFlags() {
    val carried = inventory.count("flag")
    if (carried >= Vinesweeper.MAX_FLAGS) {
        npc<Neutral>("You've already got as many flags as you can carry. Plant some of them before you come asking for more!")
        return
    }
    val missing = Vinesweeper.MAX_FLAGS - carried
    if (!get("vinesweeper_free_flags", false)) {
        if (!inventory.add("flag", missing)) {
            npc<Sad>("You'll need some free space in your pack before I can give you any flags.")
            return
        }
        set("vinesweeper_free_flags", true)
        item("flag", "Mrs. Winkin gives you $missing flags to get you started.")
        return
    }
    npc<Neutral>("Lost some flags, have you? I can sell you replacements for ${Vinesweeper.FLAG_PRICE} coins each. You can carry ${Vinesweeper.MAX_FLAGS} at a time.")
    val amount = intEntry("How many flags would you like to buy? (${Vinesweeper.FLAG_PRICE} coins each)").coerceIn(0, missing)
    if (amount == 0) {
        return
    }
    inventory.transaction {
        remove("coins", amount * Vinesweeper.FLAG_PRICE)
        add("flag", amount)
    }
    when (inventory.transaction.error) {
        TransactionError.None -> item("flag", "You buy $amount flags.")
        is TransactionError.Deficient -> npc<Sad>("You don't have enough coins for that.")
        else -> npc<Sad>("You don't have enough space in your pack for that.")
    }
}

suspend fun Player.buyOgleroots() {
    npc<Happy>("Ogleroots are ${Vinesweeper.OGLEROOT_PRICE} coins each. How many would you like?")
    val amount = intEntry("How many ogleroots would you like to buy? (${Vinesweeper.OGLEROOT_PRICE} coins each)")
    if (amount <= 0) {
        return
    }
    inventory.transaction {
        remove("coins", amount * Vinesweeper.OGLEROOT_PRICE)
        add("ogleroot", amount)
    }
    when (inventory.transaction.error) {
        TransactionError.None -> item("ogleroot", "You buy $amount ogleroots. Don't worry, I'll buy any spares back when you leave.")
        is TransactionError.Deficient -> npc<Sad>("You don't have enough coins for that.")
        else -> npc<Sad>("You don't have enough space in your pack for that.")
    }
}

suspend fun Player.buySpade() {
    if (inventory.contains("spade")) {
        npc<Neutral>("You've already got a spade! Get out there and start digging.")
        return
    }
    inventory.transaction {
        remove("coins", Vinesweeper.SPADE_PRICE)
        add("spade")
    }
    when (inventory.transaction.error) {
        TransactionError.None -> item("spade", "You buy a spade for ${Vinesweeper.SPADE_PRICE} coins.")
        is TransactionError.Deficient -> npc<Sad>("It's only ${Vinesweeper.SPADE_PRICE} coins and you can't even afford that?")
        else -> npc<Sad>("You don't have enough space in your pack for a spade.")
    }
}
