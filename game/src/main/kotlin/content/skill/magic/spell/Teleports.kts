package content.skill.magic.spell

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
import world.gregs.voidps.engine.queue.queue
import content.entity.player.inv.inventoryItem
import content.entity.sound.sound
import world.gregs.voidps.engine.client.ui.InterfaceOption

val areas: AreaDefinitions by inject()
val definitions: SpellDefinitions by inject()

interfaceOption("Cast", "*_teleport", "modern_spellbook") {
    cast()
}

interfaceOption("Cast", "*_teleport", "ancient_spellbook") {
    cast()
}

interfaceOption("Cast", "*_teleport", "lunar_spellbook") {
    cast()
}

interfaceOption("Cast", "*_teleport", "dungeoneering_spellbook") {
    cast()
}

fun InterfaceOption.cast() {
    if (component == "lumbridge_home_teleport") {
        return
    }
    if (player.contains("delay") || player.queue.contains("teleport")) {
        return
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
        player.sound("teleport")
        player.gfx("teleport_$book")
        player.animDelay("teleport_$book")
        player.tele(areas[component].random(player)!!)
        delay(1)
        player.sound("teleport_land")
        player.gfx("teleport_land_$book")
        player.animDelay("teleport_land_$book")
        if (book == "ancient") {
            delay(1)
            player.clearAnim()
        }
    }
}

inventoryItem("*", "*_teleport") {
    if (player.contains("delay") || player.queue.contains("teleport")) {
        return@inventoryItem
    }
    player.closeInterfaces()
    val definition = areas.getOrNull(item.id) ?: return@inventoryItem
    val scrolls = areas.getTagged("scroll")
    val type = if (scrolls.contains(definition)) "scroll" else "tablet"
    val map = definition.area
    player.queue("teleport", onCancel = null) {
        if (player.inventory.remove(item.id)) {
            player.sound("teleport_$type")
            player.gfx("teleport_$type")
            player.anim("teleport_$type")
            delay(3)
            player.tele(map.random(player)!!)
            player.animDelay("teleport_land")
        }
    }
}