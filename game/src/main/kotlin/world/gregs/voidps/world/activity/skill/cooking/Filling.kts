import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on

val GameObject.waterSource: Boolean
    get() = def.name == "Sink" || def.name == "Fountain" || def.name == "Well" || def.name == "Water trough" || def.name == "Pump and drain"

on<InterfaceOnObject>({ obj.waterSource && item.def.has("full") }) { player: Player ->
    player.action(ActionType.Filling) {
        player.start("skilling_delay", 1)
        while (isActive && player.inventory.contains(item.id)) {
            player.setAnimation("take")
            player.inventory.replace(item.id, item.def["full"])
            delay(if (item.id == "vase") 3 else 1)
            player.message("You fill the ${item.def.name.substringBefore(" (").lowercase()} from the ${obj.def.name.lowercase()}", ChatType.Filter)
        }
    }
}