package content.entity.npc.combat.melee

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class Chicken(val floorItems: FloorItems) : Script {

    init {
        npcSpawn("chicken*") {
            areaSound("chicken_defend", tile)
            say("squawk!")
            softTimers.start("lay_eggs")
        }
        npcTimerStart("lay_eggs") {
            // Do not have authentic data. Used this so no spamming eggs all the time.
            random.nextInt(200, 400)
        }
        npcTimerTick("lay_eggs") {
            anim("chicken_defend")
            // Timed on Rs3 but can be inauthentic for 2011
            floorItems.add(tile, "egg", disappearTicks = 100)
            say("squawk!")
            areaSound("chicken_defend", tile)
            Timer.CONTINUE
        }
    }
}
