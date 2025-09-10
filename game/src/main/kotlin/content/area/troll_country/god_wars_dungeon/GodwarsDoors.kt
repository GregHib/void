package content.area.troll_country.god_wars_dungeon

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject

@Script
class GodwarsDoors {

    val areas: AreaDefinitions by inject()

    init {
        objectOperate("Open", "big_door_saradomin_closed", "big_door_bandos_closed", "big_door_armadyl_closed", "big_door_zamorak_closed") {
            val god = target.id.removePrefix("big_door_").removeSuffix("_closed")
            if (player.tile in areas["${god}_chamber"]) {
                enterDoor(target)
                return@objectOperate
            }

            if (player["${god}_killcount", 0] < 40) {
                player.message("You don't have enough kills to enter the lair of the gods.")
                return@objectOperate
            }
            player.dec("${god}_killcount", 40)
            enterDoor(target)
        }
    }
}
