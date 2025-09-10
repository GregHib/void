package content.skill.runecrafting

import content.entity.obj.ObjectTeleports
import content.entity.player.inv.inventoryItem
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.engine.event.Script
@Script
class Talismans {

    val overworld = Rectangle(2048, 2496, 3903, 4159)
    
    val teleports: ObjectTeleports by inject()
    
    init {
        inventoryItem("Locate", "*_talisman") {
            if (item.id == "elemental_talisman") {
                player.message("You cannot tell which direction the talisman is pulling...")
                return@inventoryItem
            }
            val id = item.id.replace("_talisman", "_altar_portal")
            val teleport = teleports.get(id, "Enter").first()
            if (player.tile.region == teleport.tile.region) {
                val type = item.id.removeSuffix("_talisman").toSentenceCase()
                player.message("You are standing in the $type temple.")
                return@inventoryItem
            }
            val direction = teleport.to.delta(player.tile).toDirection()
            if (player.tile in overworld || direction == Direction.NONE) {
                player.message("The talisman is pulling towards the ${direction.name.toKebabCase()}.")
            } else {
                player.message("The talisman is having trouble pin-pointing the location.")
            }
        }

    }

}
