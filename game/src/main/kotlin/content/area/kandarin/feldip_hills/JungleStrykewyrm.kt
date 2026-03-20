package content.area.kandarin.feldip_hills

import content.entity.combat.hit.directHit
import content.entity.combat.inCombat
import content.entity.combat.target
import content.entity.effect.clearTransform
import content.entity.effect.toxin.poison
import content.entity.effect.transform
import content.skill.slayer.slayerTask
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.random

class JungleStrykewyrm : Script {
    init {
        npcOperate("Investigate", "mound_feldip_hills") { (target) ->
            if (slayerTask != "jungle_strykewyrm") {
                message("You need to have strykewyrm assigned as a task in order to fight them.")
                return@npcOperate
            }

            anim("emote_stomp")
            target.start("movement_delay", Int.MAX_VALUE)
            target.mode = EmptyMode
            target.steps.clear()
            target.softTimers.start("strykewyrm_revert")
            target.softQueue("styrkyewyrm_transform", 3) {
                target.mode = EmptyMode
                target.transform("jungle_strykewyrm")
                target.anim("strykewyrm_surface")
                target.face(this@npcOperate)
            }
        }

        npcTimerStart("strykewyrm_revert") { 20 }

        npcTimerTick("strykewyrm_revert") {
            if (inCombat) {
                return@npcTimerTick Timer.CONTINUE
            }
            anim("strykewyrm_bury")
            softQueue("bury", 3) {
                clearTransform()
            }
            Timer.CANCEL
        }

        npcAttack("jungle_strykewyrm", "dig") { target ->
            anim("strykewyrm_bury")
            val temp = mode
            softQueue("resurface", 3) {
                clearTransform()
                mode = PauseMode
                walkToDelay(target.tile)
                mode = temp
                transform("jungle_strykewyrm")
                anim("strykewyrm_surface")
                if (tile.toCuboid(size, size).contains(target.tile)) {
                    target.directHit(random.nextInt(50, 201))
                    poison(target, 88)
                }
            }
        }
    }
}