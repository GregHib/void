import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.client.ui.closeInterfaces
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.remove
import world.gregs.voidps.engine.data.definition.extra.SpellDefinitions
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.character.setGraphic
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.queue.ActionPriority
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.suspend.playAnimation
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes.hasSpellRequirements
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val areas: Areas by inject()
val definitions: SpellDefinitions by inject()
val collisions: Collisions by inject()

on<InterfaceOption>({ id.endsWith("_spellbook") && component.endsWith("_teleport") && component != "lumbridge_home_teleport" && option == "Cast" }) { player: Player ->
    if (player.queue.contains(ActionPriority.Normal)) {
        return@on
    }
    player.closeInterfaces()
    player.queue("teleport") {
        if (!hasSpellRequirements(player, component)) {
            cancel()
            return@queue
        }
        val definition = definitions.get(component)
        val area = areas.getValue(component).area
        player.exp(Skill.Magic, definition.experience)
        val book = id.removeSuffix("_spellbook")
        player.playSound("teleport")
        player.setGraphic("teleport_$book")
        player.start("movement_delay", 2)
        player.playAnimation("teleport_$book")
        player.tele(area.random(collisions, player)!!)
        pause(1)
        player.playSound("teleport_land")
        player.setGraphic("teleport_land_$book")
        player.playAnimation("teleport_land_$book")
        if (book == "ancient") {
            pause(1)
            player.clearAnimation()
        }
    }
}

on<ContainerOption>({ item.id.endsWith("_teleport") }) { player: Player ->
    if (player.queue.contains(ActionPriority.Normal)) {
        return@on
    }
    player.closeInterfaces()
    player.queue("teleport") {
        if (player.inventory.remove(item.id)) {
            player.playSound("teleport_tablet")
            player.setGraphic("teleport_tablet")
            player.start("movement_delay", 2)
            player.setAnimation("teleport_tablet")
            pause(3)
            val map = areas.getValue(item.id)
            player.tele(map.area.random(collisions, player)!!)
            player.playAnimation("teleport_land_tablet")
        }
    }
}