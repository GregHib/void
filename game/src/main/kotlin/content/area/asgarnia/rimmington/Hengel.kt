package content.area.asgarnia.rimmington

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.queue.queue

class Hengel : Script {

    init {
        npcOperate("Talk-to", "hengel") { (target) ->
            player<Happy>("Hello.")
            npc<Quiz>("What are you doing here?")
            choice("What would you like to say?") {
                option<Shifty>("I'm just wandering around.") {
                    wandering()
                }
                option<Happy>("I was hoping you'd give me some free stuff.") {
                    freeStuff()
                }
                option<Happy>("I've come to kill you.") {
                    threaten(target)
                }
            }
        }
    }

    private suspend fun Player.wandering() {
        npc<Neutral>("You do realise you're wandering around in my house?")
        player<Shifty>("Yep.")
        npc<Angry>("Well please get out!")
        player<Quiz>("Sheesh, keep your wig on!")
    }

    private suspend fun Player.freeStuff() {
        npc<Angry>("No, I jolly well wouldn't!\nGet out of my house!")
        player<Sad>("Meanie!")
    }

    private suspend fun Player.threaten(hengel: NPC) {
        anja()?.let {
            it.retreatFrom(this)
            it.say("Eeeek!")
        }
        hengel.retreatFrom(this)
        hengel.say("Aaaarrgh!")
    }

    private fun Player.anja(): NPC? = NPCs.findOrNull(tile.regionLevel, "anja")

    private fun NPC.retreatFrom(player: Player) {
        mode = Retreat(this, player)
        queue.clear("hengel_retreat_reset")
        queue("hengel_retreat_reset", 8) {
            if (mode is Retreat) {
                mode = EmptyMode
            }
        }
    }
}
