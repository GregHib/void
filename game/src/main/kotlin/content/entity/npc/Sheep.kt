package content.entity.npc

import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.player
import content.quest.quest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class Sheep : Script {

    val items: FloorItems by inject()

    private val retreatChance = 0.25

    init {
        npcOperate("Shear", "sheep*") { (target) ->
            arriveDelay()
            if (!holdsItem("shears")) {
                message("You need a set of shears to do this.")
                return@npcOperate
            }
            if (target.transform.endsWith("_shorn")) {
                return@npcOperate
            }
            anim("shear_sheep")
            if (target.id == "sheep_penguin") {
                target.anim("the_thing_reveal")
                areaSound("the_thing_escape", target.tile)
                set("the_thing_interacted", true)
                message("The... whatever it is... manages to get away from you!")
                target.mode = Retreat(target, this)
                return@npcOperate
            }
            if (random.nextDouble() < retreatChance) {
                message("The sheep manages to get away from you!")
                target.mode = Retreat(target, this)
                return@npcOperate
            }
            gfx("shearing_white_sheep")
            areaSound("shearing", target.tile)
            delay(2)
            message("You get some wool.")
            if (!inventory.add("wool")) {
                items.add(tile, "wool", revealTicks = 100, disappearTicks = 200, owner = this)
            }
            target.face(this)
            target.transform("${target.id}_shorn")
            delay(1)
            areaSound("sheep_baa2", target.tile)
            target.say("Baa!")
            target.softQueue("regrow_wool", Settings["world.npcs.sheep.regrowTicks", 50]) {
                target.clearTransform()
            }
        }

        npcOperate("Shear", "black_sheep*") { (target) ->
            arriveDelay()
            if (quest("sheep_shearer_miniquest") != "started") {
                (target.id == "black_sheep")
                // https://www.youtube.com/watch?v=0TT_cBE8shA
                message("The farmer wouldn't like you shearing these special sheep. Try the white ones.")
                return@npcOperate
            }
            if (!holdsItem("shears")) {
                message("You need a set of shears to do this.")
                return@npcOperate
            }
            if (target.transform.endsWith("_shorn")) {
                return@npcOperate
            }
            anim("shear_sheep")
            if (random.nextDouble() < retreatChance) {
                message("The sheep manages to get away from you!")
                target.mode = Retreat(target, this)
                return@npcOperate
            }
            gfx("shearing_black_sheep")
            areaSound("shearing", target.tile)
            delay(2)
            message("You get some black wool.")
            if (!inventory.add("black_wool")) {
                items.add(tile, "black_wool", revealTicks = 100, disappearTicks = 200, owner = this)
            }
            target.face(this)
            target.transform("${target.id}_shorn")
            delay(1)
            areaSound("sheep_baa", target.tile)
            target.say("Baa!")
            target.softQueue("regrow_wool", Settings["world.npcs.sheep.regrowTicks", 50]) {
                target.clearTransform()
            }
        }

        npcOperate("Talk-to", "sheep_penguin") {
            player<Neutral>("That's a sheep...I think. I can't talk to sheep.")
        }

        npcSpawn("*sheep*") {
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
}
