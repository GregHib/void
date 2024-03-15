package world.gregs.voidps.world.activity.skill.herblore

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
import world.gregs.voidps.world.interact.entity.player.equip.inventoryOption

inventoryOption("Clean", "inventory") {
    val herb: Cleaning = item.def.getOrNull("cleaning") ?: return@inventoryOption
    if (!player.has(Skill.Herblore, herb.level, true)) {
        return@inventoryOption
    }
    if(player.queue.contains("cleaning")) {
        player.queue.clearWeak()
    }
    cleanHerb(slot, player, item, herb)
    player.clean(item, player, herb)
}

fun Player.clean(item: Item, player: Player, herb: Cleaning) {
    if(!player.inventory.contains(item.id)) {
        return
    }
    weakQueue("cleaning", 2) {
        cleanHerb(player.inventory.indexOf(item.id), player, item, herb)
        clean(item, player, herb)
    }
}

fun cleanHerb(slot: Int, player: Player, item: Item, herb: Cleaning) {
    player.experience.add(Skill.Herblore, herb.xp)
    player.inventory.replace(slot, item.id, item.id.replace("grimy", "clean"))
    val herbName = item.id.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString()}
    player.message("You clean the $herbName leaf.", ChatType.Game)
}