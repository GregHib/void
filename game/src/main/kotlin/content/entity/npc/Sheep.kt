package content.entity.npc

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.player
import content.quest.quest
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class Sheep : Script {

    private val retreatChance = 0.25

    init {
        npcOperate("Shear", "sheep*") { (target) ->
            arriveDelay()
            shear(target, "white")
        }
        npcOperate("Shear", "black_sheep") { (target) ->
            arriveDelay()
            if (quest("sheep_shearer_miniquest") != "started") {
                (target.id == "black_sheep")
                // https://www.youtube.com/watch?v=0TT_cBE8shA
                message("The farmer wouldn't like you shearing these special sheep. Try the white ones.")
                return@npcOperate
            }
            shear(target, "black")
        }
        npcOperate("Talk-to", "sheep_penguin") {
            player<Idle>("That's a sheep...I think. I can't talk to sheep.")
        }
        npcSpawn("sheep*,black_sheep") {
            if (id != "sheep_penguin") {
                softTimers.start("baa_sound")
            }
        }
        npcTimerStart("baa_sound") {
            // Don't have authentic data.
            random.nextInt(50, 120)
        }
        npcTimerTick("baa_sound") {
            say("Baa!")
            // In Rs3 it has two different baa sounds maybe authentic as well in 2011
            when (random.nextInt(0, 2)) {
                0 -> areaSound("sheep_baa", tile)
                1 -> areaSound("sheep_baa2", tile)
            }
            Timer.CONTINUE
        }
    }

    private suspend fun Player.shear(target: NPC, colour: String) {
        if (!carriesItem("shears")) {
            message("You need a set of shears to do this.")
            return
        }
        if (target.transform.endsWith("_shorn")) {
            return
        }
        anim("shear_sheep")
        if (target.id == "sheep_penguin") {
            target.anim("the_thing_reveal")
            areaSound("the_thing_escape", target.tile)
            set("the_thing_interacted", true)
            message("The... whatever it is... manages to get away from you!")
            target.mode = Retreat(target, this)
            return
        } else if (random.nextDouble() < retreatChance) {
            message("The sheep manages to get away from you!")
            target.mode = Retreat(target, this)
            return
        }
        gfx("shearing_${colour}_sheep")
        areaSound("shearing", target.tile)
        delay(2)
        val item = if (colour == "black") "black_wool" else "wool"
        message("You get some ${item.toLowerSpaceCase()}.")
        if (!inventory.add(item)) {
            FloorItems.add(tile, item, revealTicks = 100, disappearTicks = 200, owner = this)
        }
        target.face(this)
        target.transform("${target.id}_shorn")
        delay(1)
        areaSound("sheep_baa${if (colour == "black") "" else "2"}", target.tile)
        target.say("Baa!")
        target.softQueue("regrow_wool", Settings["world.npcs.sheep.regrowTicks", 50]) {
            target.clearTransform()
        }
    }
}
