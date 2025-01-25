package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import content.skill.magic.spell.removeSpellItems
import content.entity.player.inv.inventoryItem
import content.entity.sound.playSound

val areas: AreaDefinitions by inject()
val definitions: SpellDefinitions by inject()

interfaceOption("Cast", "*_teleport", "*_spellbook") {
    if (component == "lumbridge_home_teleport") {
        return@interfaceOption
    }
    if (player.queue.contains(ActionPriority.Normal)) {
        return@interfaceOption
    }
    player.closeInterfaces()
    player.queue("teleport", onCancel = null) {
        if (!player.removeSpellItems(component)) {
            cancel()
            return@queue
        }
        val definition = definitions.get(component)
        player.exp(Skill.Magic, definition.experience)
        val book = id.removeSuffix("_spellbook")
        player.playSound("teleport")
        player.gfx("teleport_$book")
        player.animDelay("teleport_$book")
        player.tele(areas[component].random(player)!!)
        delay(1)
        player.playSound("teleport_land")
        player.gfx("teleport_land_$book")
        player.animDelay("teleport_land_$book")
        if (book == "ancient") {
            delay(1)
            player.clearAnim()
        }
    }
}

inventoryItem("*", "*_teleport") {
    if (player.queue.contains(ActionPriority.Normal)) {
        return@inventoryItem
    }
    player.closeInterfaces()
    val definition = areas.getOrNull(item.id) ?: return@inventoryItem
    val scrolls = areas.getTagged("scroll")
    val type = if (scrolls.contains(definition)) "scroll" else "tablet"
    val map = definition.area
    player.queue("teleport", onCancel = null) {
        if (player.inventory.remove(item.id)) {
            player.playSound("teleport_$type")
            player.gfx("teleport_$type")
            player.anim("teleport_$type")
            delay(3)
            player.tele(map.random(player)!!)
            player.animDelay("teleport_land")
        }
    }
}