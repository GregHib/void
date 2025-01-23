package world.gregs.voidps.world.map.ourania

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.data.Rune
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.item.drop.ItemDrop
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.type.random
import world.gregs.voidps.world.interact.entity.sound.playSound

val drops: DropTables by inject()
val logger = InlineLogger()

objectOperate("Craft-rune", "ourania_altar") {
    val level = player.levels.get(Skill.Runecrafting)
    val table = drops.get("ourania_rune_table_level_${if (level >= 99) 10 else level / 10}") ?: return@objectOperate
    var experience = 0.0
    var usedArdougneCloak = false
    player.inventory.transaction {
        val essence = removeToLimit("pure_essence", 28)
        if (essence == 0) {
            error = TransactionError.Deficient()
            return@transaction
        }
        val runes = mutableListOf<ItemDrop>()
        for (i in 0 until essence) {
            table.role(list = runes)
        }
        for (drop in runes) {
            val item = drop.toItem()
            val rune: Rune = item.def["runecrafting"]
            val amount = if (player["ardougne_medium_diary_complete", false] && random.nextDouble(100.0) <= rune.doubleChance) 2 else 1
            usedArdougneCloak = usedArdougneCloak || amount == 2
            add(item.id, amount)
            experience += rune.xp * 2.0
        }
    }
    player.start("movement_delay", 3)
    when (player.inventory.transaction.error) {
        is TransactionError.Deficient, is TransactionError.Invalid -> {
            player.message("You don't have any pure essences to bind.")
        }
        TransactionError.None -> {
            player.exp(Skill.Runecrafting, experience)
            player.anim("bind_runes")
            player.gfx("bind_runes")
            player.playSound("bind_runes")
            player.message("You bind the temple's power into runes.", ChatType.Filter)
            if (usedArdougneCloak) {
                player.message("Your Ardougne cloak seems to shimmer with power.", ChatType.Filter)
            }
        }
        else -> logger.warn { "Error binding runes $player ${player.levels.get(Skill.Runecrafting)} $experience" }
    }
}

itemOnObjectOperate("*_talisman", "ourania_altar") {
    player.message("Your talisman has no effect on the altar.")
}