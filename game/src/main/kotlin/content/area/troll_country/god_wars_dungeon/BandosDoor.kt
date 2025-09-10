package content.area.troll_country.god_wars_dungeon

import content.entity.obj.door.doorTarget
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.event.Script
@Script
class BandosDoor {

    init {
        objectOperate("Bang", "godwars_bandos_big_door") {
            if (player.tile.x >= target.tile.x) {
                if (!player.has(Skill.Strength, 70, message = true)) {
                    return@objectOperate
                }
                if (!player.inventory.contains("hammer")) {
                    player.message("You need a suitable hammer to ring the gong.")
                    return@objectOperate
                }
                player.anim("godwars_hammer_bang")
                delay(3)
            }
            target.remove(ticks = 2, collision = false)
            player.walkOverDelay(doorTarget(player, target) ?: return@objectOperate)
        }

    }

}
