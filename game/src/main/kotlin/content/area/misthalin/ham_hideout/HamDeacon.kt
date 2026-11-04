package content.area.misthalin.ham_hideout

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.Wander
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class HamDeacon : Script {

    init {
        npcSpawn("ham_deacon_ham_cave") {
            mode = Wander(this)
            schedulePreach(this)
        }
        npcDespawn("ham_deacon_ham_cave") {
            World.clearQueue(preachQueueName(index))
        }
    }

    private fun schedulePreach(npc: NPC) {
        val queue = preachQueueName(npc.index)
        World.clearQueue(queue)
        val delay = TimeUnit.SECONDS.toTicks(random.nextInt(PREACH_DELAY_MIN, PREACH_DELAY_MAX + 1)) - 1
        World.queue(queue, delay.coerceAtLeast(0)) {
            if (npc.index != -1) {
                startPreach(npc)
            }
        }
    }

    private fun startPreach(npc: NPC) {
        val line = PREACHING_LINES.random(random)
        npc.mode = PauseMode
        npc.steps.clear()
        npc.queue(PREACH_QUEUE) {
            face(Direction.NORTH)
            for ((index, part) in line.withIndex()) {
                val emoteTicks = anim(PREACH_EMOTES.random(random), override = true).coerceAtLeast(1)
                say(part)
                if (index < line.lastIndex) {
                    delay(random.nextInt(2) + 1)
                    clearAnim()
                    delay(1)
                } else {
                    delay(maxOf(POST_SERMON_DELAY, emoteTicks))
                }
            }
            respond(npc)
            clearAnim()
            delay(POST_SERMON_STAGE_DELAY)
            mode = Wander(this)
            schedulePreach(this)
        }
    }

    private fun respond(npc: NPC) {
        if (npc.tile.region.id != HAM_HIDEOUT_REGION_ID) {
            return
        }
        val member = NPCs.at(npc.tile.regionLevel)
            .filter { it.id == "ham_member_ham_cave_4" && it.tile.region.id == HAM_HIDEOUT_REGION_ID }
            .randomOrNull(random) ?: return
        member.say(CONGREGATION_RESPONSES.random(random))
    }

    private fun preachQueueName(index: Int) = "ham_deacon_preach_$index"

    companion object {
        private const val PREACH_QUEUE = "ham_deacon_preach_sequence"
        private const val PREACH_DELAY_MIN = 6
        private const val PREACH_DELAY_MAX = 10
        private const val HAM_HIDEOUT_REGION_ID = 12694
        private const val POST_SERMON_DELAY = 2
        private const val POST_SERMON_STAGE_DELAY = 2
        private val PREACH_EMOTES = listOf("emote_preach_2", "emote_cheer", "emote_preach")
        private val PREACHING_LINES = listOf(
            listOf("We are the", "righteous ones in", "his eyes alone."),
            listOf("For they are not", "the chosen ones", "in Saradomin's eyes."),
            listOf("Pray for thine own", "safety from the cunning", "monster for it will", "sneak up on thee..."),
            listOf("And let us smite", "these monsters", "...unto their deaths."),
            listOf("Let them not", "infest our", "cities and towns..."),
            listOf("And where do we go for safety", "from these monsters", "...here, with our brethren!"),
            listOf("And lo, we become the power,", "indeed, the force to", "stop these monsters", "in their tracks."),
            listOf("And let there be no cave", "or shelter for their spawn", "until the end of days."),
            listOf("Verily I urge you, my friends-", "to take up your spades", "and farm your farms", "to feed our people", "in this blessed sanctuary."),
            listOf("For Saradomin will guide", "our sword arms", "and smash the enemies of humans till their", "bones become dust."),
            listOf("If thine monsters visage", "does frighten thee, tear it off", "I say... tear it off!"),
        )
        private val CONGREGATION_RESPONSES = listOf(
            "I hear you brother...",
            "Well said brother...",
            "You speak the truth brother...",
        )
    }
}
