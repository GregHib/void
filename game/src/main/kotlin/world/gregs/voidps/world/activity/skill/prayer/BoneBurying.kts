package world.gregs.voidps.world.activity.skill.prayer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val logger = InlineLogger()

on<InventoryOption>({ inventory == "inventory" && item.def.contains("prayer_xp") && option == "Bury" }) { player: Player ->
    if (player.hasClock("bone_delay")) {
        return@on
    }
    val xp = item.def["prayer_xp", 0.0]
    if (xp <= 0.0) {
        logger.warn { "Missing bone xp: ${item.id}" }
        return@on
    }
    player.message("You dig a hole in the ground.", ChatType.Filter)
    if (!player.inventory.remove(slot, item.id)) {
        return@on
    }
    player.start("bone_delay", 1)
    player.setAnimation("bend_down")
    player.experience.add(Skill.Prayer, xp)
    player.weakQueue("bury", 1, onCancel = null) {
        player.message("You bury the ${item.def.name.lowercase()}.", ChatType.Filter)
    }
}