package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.ObjectOption
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.hasItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.sound.playSound
import java.util.concurrent.TimeUnit

val floorItems: FloorItems by inject()

on<ObjectOption>({ target.id == "cupboard_the_knights_sword_closed" && option == "Open" }) { player: Player ->
    player.playSound("cupboard_open")
    target.replace("cupboard_the_knights_sword_opened", ticks = TimeUnit.MINUTES.toTicks(3))
}

on<ObjectOption>({ target.id == "cupboard_the_knights_sword_opened" && option == "Shut" }) { player: Player ->
    player.playSound("cupboard_close")
    target.replace("cupboard_the_knights_sword_closed")
}


//npc<Angry>("""
//    HEY! Just WHAT do you THINK you are
//     DOING??? STAY OUT of MY cupboard!
//""")
on<ObjectOption>({ target.id == "cupboard_the_knights_sword_opened" && option == "Search" }) { player: Player ->
    when (player["the_knights_sword", "unstarted"]) {
        "cupboard", "stage6" -> {
            if (player.hasItem("portrait")) {
                statement("There is just a load of junk in here.")
            } else {
                statement("You find a small portrait in here which you take.")
                if (player.inventory.isFull()) {
                    floorItems.add(player.tile, "portrait", disappearTicks = 300, owner = player)
                    return@on
                }
                player.inventory.add("portrait")
            }
        }
        else -> statement("There is just a load of junk in here.")
    }
}