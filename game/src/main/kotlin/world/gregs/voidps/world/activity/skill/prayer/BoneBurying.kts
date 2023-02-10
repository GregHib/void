package world.gregs.voidps.world.activity.skill.prayer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.clear
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && item.def.has("prayer_xp") && option == "Bury" }) { player: Player ->
    if (player.queue.contains(ActionPriority.Weak)) {
        return@on
    }
    val xp = item.def["prayer_xp", 0.0]
    if (xp <= 0.0) {
        logger.warn { "Missing bone xp: ${item.id}" }
        return@on
    }
    player.message("You dig a hole in the ground.", ChatType.Filter)
    if (!player.inventory.clear(slot)) {
        return@on
    }
    player.setAnimation("bury_bones")
    player.experience.add(Skill.Prayer, xp)
    player.weakQueue(1) {
        player.message("You bury the ${item.def.name.lowercase()}.", ChatType.Filter)
    }
}