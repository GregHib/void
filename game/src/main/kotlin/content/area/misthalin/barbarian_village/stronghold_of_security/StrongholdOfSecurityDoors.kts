package content.area.misthalin.barbarian_village.stronghold_of_security

import content.entity.player.dialogue.DoorHead
import content.entity.player.dialogue.Surprised
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.sound.sound
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.objectOperate

objectOperate("Open", "gate_of_war*") {
    if (target.tile.y == 5238 && player.tile.y > target.tile.y) {
        npc<DoorHead>("gate_of_war", "Greetings Adventurer. This place is kept safe by the spirits within the doors. As you pass through you will be asked questions about security. Hopefully you will learn much from us.")
        npc<DoorHead>("gate_of_war", "Please pass through and begin your adventure, beware of the various monsters that dwell within.'")
    }
    player.anim("stronghold_of_security_door")
    player.sound("stronghold_of_security_through_door")
    delay()
    enterDoor()
    player.anim("stronghold_of_security_door_appear")
    player<Surprised>("Oh my! I just got sucked through that door... what a weird feeling! Still, I guess I should expect it as these evidently aren't your average kind of doors.... they talk and look creepy!")
}

fun ObjectOption<Player>.enterDoor() {
    when (target.rotation) {
        0 -> if (player.tile.x >= target.tile.x) {
            player.tele(target.tile.addX(-1))
        } else {
            player.tele(target.tile)
        }
        1 -> if (player.tile.y > target.tile.y) {
            player.tele(target.tile)
        } else {
            player.tele(target.tile.addY(1))
        }
        2 -> if (player.tile.x < target.tile.x) {
            player.tele(target.tile)
        } else {
            player.tele(target.tile.addX(1))
        }
        3 -> if (player.tile.y >= target.tile.y) {
            player.tele(target.tile.addY(-1))
        } else {
            player.tele(target.tile)
        }
    }
}
