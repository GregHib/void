package content.area.morytania.braindeath_island

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Bored
import content.entity.player.dialogue.Drunk
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.Leash
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class ZombieProtester : Script {
    init {

        huntPlayer("zombie_protester*", "spotted") {
            val post: Tile = get("spawn_tile") ?: return@huntPlayer
            if (!tile.within(it.tile, 4) || !it.tile.within(post, LEASH_RANGE)) {
                return@huntPlayer
            }
            softTimers.startIfAbsent("protesting")
            // Leash rather than Follow; a protester that chased with Follow would teleport after
            // the player the moment they outran it or changed level, and Hunting only re-runs for
            // an idle, wandering or patrolling npc so nothing would ever call it back.
            mode = Leash(this, it, LEASH_RANGE, post)
        }

        npcOperate("Talk-to", "zombie_protester*") {
            if (questStage("rum_deal") >= 18) {
                drunkProtester()
            } else {
                soberProtester()
            }
        }

        npcTimerStart("protesting") { TimeUnit.SECONDS.toTicks(30) }

        npcTimerTick("protesting") {
            say(
                when (random.nextInt(8)) {
                    0 -> "Whadda we want? Rum!"
                    1 -> "Give us rum or give us death!"
                    2 -> "Give us yer rum, ye scurvy dog!"
                    3 -> "Yer rum or yer brains!"
                    4 -> "When do we want it? Now!"
                    5 -> "Where d'ye think yer goin?"
                    6 -> "Ye'll never beat us all!"
                    7 -> "United we stagger!"
                    else -> "Rum, rum, we want rum!"
                },
            )
            Timer.CONTINUE
        }
    }

    private suspend fun Player.soberProtester() {
        player<Quiz>("Excuse me, but...")
        npc<Angry>("Arr!")
        player<Quiz>("Is there any way you could...")
        npc<Angry>("ARRR!")
        player<Quiz>("Is there anyone else I could...")
        npc<Angry>("Arrrrrrrrrr!!!")
        player<Bored>("Fiiine...")
    }

    private suspend fun Player.drunkProtester() {
        npc<Drunk>("Arrrr! Tis yerself! Have a drink!")
        player<Shifty>("Errr...Arrr! I will in a sec, I've just go to, err, plunder some landlubbers...")
        npc<Drunk>("Good huntin'!")
    }

    companion object {
        /** How far a protester will chase from its post before losing interest. */
        private const val LEASH_RANGE = 10
    }
}
