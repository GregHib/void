package world.gregs.voidps.world.activity.skill.runecrafting

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.cache.definition.data.ItemDefinition
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.interact.ItemOnObject
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Rune
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.suspend.arriveDelay
import world.gregs.voidps.world.interact.entity.sound.playSound

val itemDefinitions: ItemDefinitions by inject()
val logger = InlineLogger()

on<ItemOnObject>({ operate && target.id.endsWith("_altar") && item.id.endsWith("_essence") }) { player: Player ->
    arriveDelay()
    bindRunes(player, item.id, item.def)
}

on<ObjectOption>({ operate && target.id.endsWith("_altar") && option == "Craft-rune" }) { player: Player ->
    arriveDelay()
    val id = target.id.replace("_altar", "_rune")
    bindRunes(player, id, itemDefinitions.get(id))
}

fun Runecrafting.bindRunes(player: Player, id: String, itemDefinition: ItemDefinition) {
    val rune: Rune = itemDefinition.getOrNull("runecrafting") ?: return
    if (!player.has(Skill.Runecrafting, rune.levels.first(), message = true)) {
        return
    }
    val pure = rune.pure || !player.inventory.contains("rune_essence")
    val essenceId = if (pure) "pure_essence" else "rune_essence"
    val essence = player.inventory.count(essenceId)
    player.inventory.transaction {
        remove(essenceId, essence)
        val count = rune.multiplier(player)
        add(id, essence * count)
    }
    when (player.inventory.transaction.error) {
        is TransactionError.Deficient, is TransactionError.Invalid -> {
            player.message("You don't have any rune essences to bind.")
        }
        TransactionError.None -> {
            player.exp(Skill.Runecrafting, rune.xp * essence)
            player.setAnimation("bind_runes")
            player.setGraphic("bind_runes")
            player.playSound("bind_runes")
            player.message("You bind the temple's power into ${id.toSentenceCase().plural()}.", ChatType.Filter)
        }
        else -> logger.warn { "Error binding runes $player $rune ${player.levels.get(Skill.Runecrafting)} $essence" }
    }
}