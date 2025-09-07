package content.area.fremennik_province.waterbirth_island_dungeon

import content.entity.combat.attackers
import content.entity.death.npcDeath
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

val objects: GameObjects by inject()

npcOperate("Destroy", "door_support*") {
    player.message("This door does not seem to be openable from this side...")
}

npcDeath("door_support*") { npc ->
    for (attack in npc.attackers) {
        attack.mode = EmptyMode
    }
    val base = objects[npc.tile, "door_support_base"]!!
    base.remove(ticks = TimeUnit.SECONDS.toTicks(10))
}