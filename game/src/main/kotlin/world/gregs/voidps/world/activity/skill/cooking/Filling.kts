import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.InterfaceOnObject
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.contain.replace
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.pause

val GameObject.waterSource: Boolean
    get() = def.name == "Sink" || def.name == "Fountain" || def.name == "Well" || def.name == "Water trough" || def.name == "Pump and drain"

on<InterfaceOnObject>({ obj.waterSource && item.def.has("full") }) { player: Player ->
    while (player.inventory.contains(item.id)) {
        player.setAnimation("take")
        player.inventory.replace(item.id, item.def["full"])
        pause(if (item.id == "vase") 3 else 1)
        player.message("You fill the ${item.def.name.substringBefore(" (").lowercase()} from the ${obj.def.name.lowercase()}", ChatType.Filter)
    }
}