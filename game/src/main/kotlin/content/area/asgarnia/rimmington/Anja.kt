package content.area.asgarnia.rimmington

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.queue.queue
import kotlin.random.Random

class Anja : Script {

    init {
        npcOperate("Talk-to", "anja") { (target) ->
            player<Happy>("Hello.")
            npc<Quiz>("Hello ${if (male) "sir" else "madam"}. What are you doing in my house?")
            choice("What would you like to say?") {
                option<Shifty>("I'm just wandering around.") {
                    wandering()
                }
                option<Shifty>("I was hoping you'd give me some free stuff.") {
                    freeStuff()
                }
                option<Happy>("I've come to kill you.") {
                    threaten(target)
                }
            }
        }
    }

    private suspend fun Player.wandering() {
        npc<Confused>("Oh dear, are you lost?")
        choice("What would you like to say?") {
            option<Happy>("Yes, I'm lost.") {
                npc<Happy>("Ok, just walk north-east when you leave this house, and soon you'll reach the big city of Falador.")
                player<Happy>("Thanks a lot.")
            }
            option<Shifty>("No, I know where I am.") {
                npc<Confused>("Oh? Well, would you mind wandering somewhere else? This is my house.")
                player<Sad>("Meh!")
            }
        }
    }

    private suspend fun Player.freeStuff() {
        while (true) {
            when (Random.nextInt(5)) {
                0 -> npc<Confused>("I don't know...")
                1 -> npc<Quiz>("Do you REALLY need it?")
                2 -> npc<Sad>("I don't have much on me...")
                3 -> npc<Confused>("Er...")
                else -> {
                    npc<Happy>("Oh, alright. Here you go.")
                    val coins = Random.nextInt(2) + 1
                    addOrDrop("coins", coins)
                    message("Anja gives you some money.")
                    return
                }
            }
            when (Random.nextInt(4)) {
                0 -> player<Happy>("I promise I'll stop bothering you!")
                1 -> player<Happy>("Pwetty pleathe wiv thugar on top!")
                2 -> player<Neutral>("(beg beg beg beg beg)")
                else -> player<Shifty>("Pleeease!")
            }
        }
    }

    private suspend fun Player.threaten(anja: NPC) {
        anja.retreatFrom(this)
        anja.say("Eeeek!")
        hengel()?.let {
            it.retreatFrom(this)
            it.say("Aaaarrgh!")
        }
    }

    private fun Player.hengel(): NPC? = NPCs.findOrNull(tile.regionLevel, "hengel")

    private fun NPC.retreatFrom(player: Player) {
        mode = Retreat(this, player)
        queue.clear("anja_retreat_reset")
        queue("anja_retreat_reset", 8) {
            if (mode is Retreat) {
                mode = EmptyMode
            }
        }
    }
}

// for future reference, when pickpocketed, the player gets an endless amount of 3 coins (20 for those who have completed Lost Her Marbles). https://runescape.wiki/w/Anja?oldid=4161144
