package content.area.kandarin.feldip_hills

import content.entity.combat.hit.directHit
import content.entity.combat.inCombat
import content.entity.effect.clearTransform
import content.entity.effect.toxin.poison
import content.entity.effect.transform
import content.skill.slayer.slayerTask
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.random.nextInt

class JungleStrykewyrm : Script {
    init {
        npcOperate("Investigate", "mound_feldip_hills") { (target) ->
            investigate(this, target, "jungle_strykewyrm")
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
            burrow(this, target) {
                poison(target, 88)
            }
        }
    }
    companion object {

        fun burrow(source: NPC, target: Character, block: () -> Unit = {}) {
            source.anim("strykewyrm_bury")
            val temp = source.mode
            val type = source.transform
            source.start("action_delay", 8)
            source.softQueue("burrow", 3) {
                source.clearTransform()
                source.mode = PauseMode
                target.mode = EmptyMode
                source.start("action_delay", Int.MAX_VALUE)
                source.walkToDelay(target.tile)
                source.start("action_delay", 4)
                source.mode = temp
                source.transform(type)
                source.anim("strykewyrm_surface")
                if (source.tile.toCuboid(source.size, source.size).contains(target.tile)) {
                    target.directHit(random.nextInt(50..300))
                    block.invoke()
                }
            }
        }

        fun investigate(source: Player, target: NPC, to: String) {
            if (Settings["slayer.strykewyrmReqTask", false] && source.slayerTask != to) {
                source.anim("emote_stomp")
                source.softQueue("stomp_mound", 3) {
                    source.anim("emote_think")
                }
                source.message("You need to have strykewyrm assigned as a task in order to fight them.")
                return
            }
            source.anim("emote_stomp")
            target.start("movement_delay", Int.MAX_VALUE)
            target.mode = EmptyMode
            target.steps.clear()
            target.softTimers.start("strykewyrm_revert")
            target.softQueue("strykewyrm_transform", 3) {
                target.mode = EmptyMode
                target.transform(to)
                target.anim("strykewyrm_surface")
                target.face(source)
                target.start("action_delay", TimeUnit.SECONDS.toTicks(3))
                target.interactPlayer(source, "Attack")
            }
        }
    }
}
