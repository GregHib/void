package content.area.wilderness

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Tile

class MagicAxeHut : Script {

    init {
        objectOperate("Open", "door_68_closed") { (target) ->
            if (insideHut(target)) {
                enterDoor(target)
            } else {
                message("The door is locked.")
                sound("locked")
            }
        }

        objectOperate("Pick-lock", "door_68_closed") { (target) ->
            if (!inventory.contains("lockpick")) {
                message("You need a lockpick for this lock.")
                sound("locked")
                return@objectOperate
            }
            if (levels.get(Skill.Thieving) < 32) {
                message("You need a Thieving level of 32 to pick this lock.")
                sound("locked")
                return@objectOperate
            }
            message("You attempt to pick the lock.")
            if (!Level.success(levels.get(Skill.Thieving), 24..175)) {
                message("You fail to pick the lock.")
                sound("locked")
                return@objectOperate
            }
            message("You manage to pick the lock.")
            exp(Skill.Thieving, 22.5)
            enterDoor(target)
        }
    }

    private fun Player.insideHut(door: GameObject): Boolean {
        if (tile.level != door.tile.level) {
            return false
        }
        val hut = Tile(3190, 3959, door.tile.level)
        return tile.distanceTo(hut) < door.tile.distanceTo(hut)
    }
}
