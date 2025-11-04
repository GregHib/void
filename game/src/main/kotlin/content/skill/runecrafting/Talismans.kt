package content.skill.runecrafting

import content.entity.obj.ObjectTeleports
import net.pearx.kasechange.toKebabCase
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.area.Rectangle

class Talismans : Script {

    val overworld = Rectangle(2048, 2496, 3903, 4159)

    val teleports: ObjectTeleports by inject()

    init {
        itemOption("Locate", "*_talisman") { (item) ->
            if (item.id == "elemental_talisman") {
                message("You cannot tell which direction the talisman is pulling...")
                return@itemOption
            }
            val id = item.id.replace("_talisman", "_altar_portal")
            val teleport = teleports.get(id, "Enter").first()
            if (tile.region == teleport.tile.region) {
                val type = item.id.removeSuffix("_talisman").toSentenceCase()
                message("You are standing in the $type temple.")
                return@itemOption
            }
            val direction = teleport.to.delta(tile).toDirection()
            if (tile in overworld || direction == Direction.NONE) {
                message("The talisman is pulling towards the ${direction.name.toKebabCase()}.")
            } else {
                message("The talisman is having trouble pin-pointing the location.")
            }
        }
    }
}
