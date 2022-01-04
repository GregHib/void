import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.definition.SpellDefinitions
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.hasOrStart
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.engine.map.collision.Collisions
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.world.interact.entity.player.combat.magic.Runes.hasSpellRequirements
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound

val areas: Areas by inject()
val definitions: SpellDefinitions by inject()
val collisions: Collisions by inject()

on<InterfaceOption>({ id.endsWith("_spellbook") && component.endsWith("_teleport") && component != "lumbridge_home_teleport" && option == "Cast" }) { player: Player ->
    if (player.hasEffect("teleport_delay")) {
        return@on
    }
    player.teleport {
        if (!hasSpellRequirements(player, component)) {
            cancel(ActionType.Teleport)
            return@teleport
        }
        player.start("teleport_delay", 2)
        val definition = definitions.get(component)
        val area = areas.getValue(component).area
        player.exp(Skill.Magic, definition.experience)
        val book = id.removeSuffix("_spellbook")
        player.playSound("teleport")
        player.setGraphic("teleport_$book")
        player.playAnimation("teleport_$book")
        player.move(area.random(collisions, player)!!)
        player.playSound("teleport_land")
        player.setGraphic("teleport_land_$book")
        player.playAnimation("teleport_land_$book")
        if (book == "ancient") {
            delay(1)
            player.clearAnimation()
        }
    }
}

on<ContainerOption>({ item.id.endsWith("_teleport") }) { player: Player ->
    if (player.hasOrStart("teleport_delay", 2)) {
        return@on
    }
    player.teleport {
        if (player.inventory.remove(item.id)) {
            player.playSound("teleport_tablet")
            player.setGraphic("teleport_tablet")
            player.setAnimation("teleport_tablet")
            delay(2)
            val map = areas.getValue(item.id)
            player.move(map.area.random(collisions, player)!!)
            player.playAnimation("teleport_land_tablet")
        }
    }
}

fun Player.teleport(block: suspend Action.() -> Unit) = action(ActionType.Teleport) {
    withContext(NonCancellable) {
        block.invoke(this@action)
    }
}