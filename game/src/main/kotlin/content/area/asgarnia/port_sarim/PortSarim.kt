package content.area.asgarnia.port_sarim

import content.entity.obj.door.enterDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.queue.weakQueue

class PortSarim : Script {

    init {
        takeable("white_apron_port_sarim") {
            if (carriesItem("white_apron")) {
                message("You already have one of those.")
                null
            } else {
                "white_apron"
            }
        }

        taken("white_apron_port_sarim") {
            anim("take")
            message("You take an apron. It feels freshly starched and smells of laundry.")
        }

        objectOperate("Open", "port_sarim_jail_door_closed") {
            message("The door seems to be fairly well locked.")
        }

        objectOperate("Pick-lock", "port_sarim_jail_door_closed") { (target) ->
            pickLock(target)
        }
    }

    fun Player.pickLock(door: GameObject) {
        message("You attempt to pick the lock on the door.", type = ChatType.Filter)
        anim("open_chest_mid")
        sound("locked")
        weakQueue("lock_picking", 4) {
            if (!Level.success(levels.get(Skill.Thieving), 35)) { // TODO unknown rates
                pickLock(door)
                return@weakQueue
            }
            message("You pick the lock on the prison door.", type = ChatType.Filter)
            sound("chest_open")
            sound("iron_door_open")
            exp(Skill.Thieving, 4.0)
            enterDoor(door)
            clearAnim()
        }
    }
}
