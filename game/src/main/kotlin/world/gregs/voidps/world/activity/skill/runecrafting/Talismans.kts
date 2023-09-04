package world.gregs.voidps.world.activity.skill.runecrafting

import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.map.instance.Instances
import world.gregs.voidps.type.Direction
import world.gregs.voidps.world.interact.entity.obj.Teleports
import world.gregs.voidps.world.interact.entity.player.equip.InventoryOption

val teleports: Teleports by inject()

on<InventoryOption>({ item.id.endsWith("_talisman") && option == "Locate" }) { player: Player ->
    val id = item.id.replace("_talisman", "_altar_portal")
    val teleport = teleports.get(id, "Enter").first()
    if (player.tile.region == teleport.tile.region) {
        val type = item.id.removeSuffix("_talisman").toSentenceCase()
        player.message("You are standing in the $type temple.")
        return@on
    }
    val direction = teleport.to.delta(player.tile).toDirection()
    if (Instances.isInstance(player.tile.region) || direction == Direction.NONE) {
        player.message("The talisman is having trouble pin-pointing the location.")
    } else {
        player.message("The talisman is pulling towards the ${direction.name.toKebabCase()}.")
    }
}