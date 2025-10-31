package content.area.troll_country.god_wars_dungeon

import content.entity.obj.door.doorTarget
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inv.inventory

class BandosDoor : Script {

    init {
        objectOperate("Bang", "godwars_bandos_big_door") { (target) ->
            if (tile.x >= target.tile.x) {
                if (!has(Skill.Strength, 70, message = true)) {
                    return@objectOperate
                }
                if (!inventory.contains("hammer")) {
                    message("You need a suitable hammer to ring the gong.")
                    return@objectOperate
                }
                anim("godwars_hammer_bang")
                delay(3)
            }
            target.remove(ticks = 2, collision = false)
            walkOverDelay(doorTarget(this, target) ?: return@objectOperate)
        }
    }
}
