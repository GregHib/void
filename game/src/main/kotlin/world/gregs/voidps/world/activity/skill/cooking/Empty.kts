import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.replace
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.world.interact.entity.player.equip.ContainerOption

on<ContainerOption>({ option == "Empty" }) { player: Player ->
    val replacement = when {
        item.id.startsWith("bucket_of") || item.id.endsWith("compost") -> "bucket"
        item.id.startsWith("jug_of") -> "jug"
        item.id.startsWith("pot_of") -> "empty_pot"
        item.id.startsWith("bowl_of") -> "bowl"
        else -> "vial"
    }
    player.inventory.replace(slot, item.id, replacement)
    player.message("You empty the ${item.def.name.substringBefore(" (").lowercase()}.", ChatType.Filter)
}

on<ContainerOption>({ option == "Empty Dish" }) { player: Player ->
    player.inventory.replace(slot, item.id, "pie_dish")
    player.message("You remove the burnt pie from the pie dish.", ChatType.Filter)
}
