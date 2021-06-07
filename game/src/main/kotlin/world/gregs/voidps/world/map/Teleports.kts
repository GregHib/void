import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.gregs.voidps.engine.action.Action
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.ui.InterfaceOption
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.update.visual.player.move
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.character.update.visual.setGraphic
import world.gregs.voidps.engine.entity.has
import world.gregs.voidps.engine.entity.remaining
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.map.area.Areas
import world.gregs.voidps.network.encode.message
import world.gregs.voidps.utility.TICKS
import world.gregs.voidps.utility.func.plural
import world.gregs.voidps.utility.inject
import world.gregs.voidps.utility.toTicks
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val areas: Areas by inject()

on<InterfaceOption>({ name == "modern_spellbook" && component == "lumbridge_home_teleport" && option == "Cast" }) { player: Player ->
    if (player.has("home_teleport_timeout")) {
        val remaining = TICKS.toMinutes(player.remaining("home_teleport_timeout"))
        player.message("You have to wait $remaining ${"minute".plural(remaining)} before trying this again.")
        return@on
    }
    player.action(ActionType.Teleport) {
        repeat(17) {
            player.setGraphic("home_tele_${it + 1}")
            player.playAnimation("home_tele_${it + 1}")
        }
        withContext(NonCancellable) {
            val lumbridge = areas.getValue("lumbridge")
            player.move(lumbridge.area.random())
            player.start("home_teleport_timeout", TimeUnit.MINUTES.toTicks(30), persist = true)
        }
    }
}


on<InterfaceOption>({ name.endsWith("_spellbook") && component.endsWith("_teleport") && option == "Cast" }) { player: Player ->
    player.teleport {
        val book = name.removeSuffix("_spellbook")
        player.playSound("teleport")
        player.setGraphic("teleport_$book")
        player.setAnimation("teleport_$book", walk = false, run = false)
        delay(2)
        val map = areas.getValue(component)
        player.move(map.area.random(player.movement.traversal)!!)
        player.playSound("teleport_land")
        player.setGraphic("teleport_land_$book")
        player.playAnimation("teleport_land_$book", walk = false, run = false)
    }
}

on<ContainerOption>({ item.name.endsWith("_teleport") }) { player: Player ->
    player.teleport {
        if (player.inventory.remove(item.name)) {
            player.playSound("teleport_tablet")
            player.setGraphic("teleport_tablet")
            player.setAnimation("teleport_tablet", walk = false, run = false)
            delay(2)
            val map = areas.getValue(item.name)
            player.move(map.area.random(player.movement.traversal)!!)
            player.playAnimation("teleport_land_tablet", walk = false, run = false)
        }
    }
}

fun Player.teleport(block: suspend Action.() -> Unit) = action(ActionType.Teleport) {
    withContext(NonCancellable) {
        block.invoke(this@action)
    }
}