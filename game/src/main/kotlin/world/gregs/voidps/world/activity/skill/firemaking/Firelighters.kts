import world.gregs.voidps.engine.client.ui.interact.InterfaceOnInterface
import world.gregs.voidps.engine.contain.inventory
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on

on<InterfaceOnInterface>({ fromItem.id.endsWith("firelighter") && toItem.id == "logs" }) { player: Player ->
    player.inventory.transaction {
        remove(fromItem.id)
        val colour = fromItem.id.removeSuffix("_firelighter")
        replace(toItem.id, "${colour}_logs")
    }
}