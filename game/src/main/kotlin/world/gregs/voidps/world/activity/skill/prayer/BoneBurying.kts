package world.gregs.voidps.world.activity.skill.prayer

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

val logger = InlineLogger()

on<ContainerOption>({ container == "inventory" && item.def.has("prayer_xp") && option == "Bury" }) { player: Player ->
    if (player.hasOrStart("skilling_delay", 1)) {
        return@on
    }
    val xp = item.def["prayer_xp", 0.0]
    if (xp <= 0.0) {
        logger.warn { "Missing bone xp: ${item.id}" }
        return@on
    }
    player.action(ActionType.Burying) {
        player.message("You dig a hole in the ground.", ChatType.Filter)
        if (player.inventory.remove(slot, item.id, 1)) {
            player.setAnimation("bury_bones")
            player.experience.add(Skill.Prayer, xp)
            delay(1)
            player.message("You bury the ${item.def.name.lowercase()}.", ChatType.Filter)
        }
    }
}