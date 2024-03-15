package world.gregs.voidps.world.activity.skill.herblore

import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.data.Cleaning
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.replace
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption

inventoryOption("Clean", "inventory") {
    val herb: Cleaning = item.def.getOrNull("cleaning") ?: return@inventoryOption
    if (!player.has(Skill.Herblore, herb.level, true)) {
        return@inventoryOption
    }

    if (player.inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))) {
        player.experience.add(Skill.Herblore, herb.xp)
    }
}