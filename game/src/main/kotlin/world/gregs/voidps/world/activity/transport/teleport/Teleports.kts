package world.gregs.voidps.world.activity.transport.teleport

import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.ui.interfaceOption
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.data.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.world.interact.entity.player.combat.magic.spell.removeSpellItems
import world.gregs.voidps.world.interact.entity.player.equip.inventoryItem
import world.gregs.voidps.world.interact.entity.sound.playSound

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
        player.setGraphic("teleport_$book")
        player.start("movement_delay", 2)
        player.playAnimation("teleport_$book", canInterrupt = false)
        player.tele(areas[component].random(player)!!)
        delay(1)
        player.playSound("teleport_land")
        player.setGraphic("teleport_land_$book")
        player.playAnimation("teleport_land_$book", canInterrupt = false)
        if (book == "ancient") {
            delay(1)
            player.clearAnimation()
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
            player.setGraphic("teleport_$type")
            player.start("movement_delay", 2)
            player.setAnimation("teleport_$type")
            delay(3)
            player.tele(map.random(player)!!)
            player.playAnimation("teleport_land", canInterrupt = false)
        }
    }
}