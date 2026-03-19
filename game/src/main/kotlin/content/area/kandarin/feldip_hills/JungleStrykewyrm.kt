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
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.map.Overlap
import world.gregs.voidps.engine.queue.queue
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
            target.transform("jungle_strykewyrm")
            target.anim("strykewyrm_surface")
            softTimers.start("strykewyrm_revert")
        }

        npcTimerStart("strykewyrm_revert") {
            20
        }

        npcTimerTick("strykewyrm_revert") {
            if (inCombat) {
                return@npcTimerTick Timer.CONTINUE
            }
            anim("strykewyrm_bury")
            // TODO what if in combat?
            clearTransform()
            Timer.CANCEL
        }

        npcAttack("jungle_strykewyrm", "dig") { target ->
            anim("jungle_strykewyrm_bury")
            clearTransform()
            target.target?.mode = EmptyMode
            val distance = tile.distanceTo(target.tile)
            walkTo(target.tile)
            queue("resurface", distance) {
                anim("jungle_strykewyrm_surface")
                if (Overlap.isUnder(target.tile, 1, tile, size)) {
                    directHit(random.nextInt(50, 201))
                    poison(target, 88)
                }
            }
        }
    }
}