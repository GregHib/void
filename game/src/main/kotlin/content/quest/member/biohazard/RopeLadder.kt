package content.quest.member.biohazard

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.type.Tile

class RopeLadder : Script {

    init {
        npcOperate("Talk-to", "omart_west_ardougne") {
            when (questStage("biohazard")) {
                0, 1 -> {
                    player<Neutral>("Hello there.")
                    npc<Neutral>("Hello.")
                    player<Quiz>("How are you?")
                    npc<Neutral>("Fine thanks.")
                }
                2, 3 -> {
                    player<Neutral>("Omart, Jerico said you might be able to help me.")
                    npc<Neutral>("He informed me of your problem traveller. I would be glad to help, I have a rope ladder and my associate, Kilron, is waiting on the other side.")
                    player<Happy>("Good stuff.")
                    npc<Neutral>("Unfortunately we can't risk it with the watch tower so close. So first we need to distract the guards in the tower.")
                    player<Quiz>("How?")
                    npc<Neutral>("Try asking Jerico, if he's not too busy with his pigeons. I'll be waiting here for you.")
                }
                4 -> throwLadder()
                in 5..15 -> {
                    player<Happy>("Hello Omart.")
                    npc<Neutral>("Hello traveller. The guards are still distracted if you wish to cross the wall.")
                    crossChoice(west = true)
                }
                else -> {
                    player<Happy>("Hello Omart.")
                    npc<Neutral>("Hello adventurer. I'm afraid it's too risky to use the ladder again, but I believe Edmond's working on another tunnel.")
                }
            }
        }

        npcOperate("Talk-to", "kilron_west_ardougne") {
            if (questStage("biohazard") < 5) {
                player<Neutral>("Hello there.")
                npc<Neutral>("Hello.")
                player<Quiz>("How are you?")
                npc<Neutral>("Busy.")
                return@npcOperate
            }
            player<Happy>("Hello Kilron.")
            npc<Quiz>("Hello traveller. Do you need to go back over?")
            choice {
                option<Neutral>("Not yet Kilron.") {
                    npc<Neutral>("Okay, just give me the word.")
                }
                option<Neutral>("Yes I do.") {
                    npc<Shifty>("Quickly now!")
                    climbLadder(west = false)
                }
            }
        }
    }

    private suspend fun Player.throwLadder() {
        npc<Happy>("Well done, the guards are having real trouble with those birds. You must go now traveller, it's your only chance.")
        message("Omart calls to his associate.")
        delay(2)
        npc<Neutral>("Kilron!")
        message("He throws one end of the rope ladder over the wall.")
        delay(2)
        npc<Neutral>("You must go now traveller.")
        crossChoice(west = true)
    }

    private suspend fun Player.crossChoice(west: Boolean) {
        choice {
            option<Neutral>("Ok, let's do it.") {
                climbLadder(west)
            }
            option<Neutral>("I'll be back soon.") {
                npc<Neutral>("Don't take too long, those mourners will soon be rid of those birds.")
            }
        }
    }

    private suspend fun Player.climbLadder(west: Boolean) {
        for (tile in WALL_TILES) {
            GameObjects.findOrNull(tile, "bio_ladder_wall")?.replace("bio_ladder", ticks = LADDER_TICKS)
        }
        if (questStage("biohazard") == 4) {
            set("biohazard", "crossed_wall")
        }
        delay(1)
        message("You climb up the rope ladder...")
        delay(2)
        tele(if (west) WEST_LANDING else EAST_LANDING)
        message("and drop down on the other side.")
    }

    private companion object {
        val WALL_TILES = listOf(Tile(2557, 3267), Tile(2558, 3267))
        val WEST_LANDING = Tile(2556, 3267)
        val EAST_LANDING = Tile(2559, 3267)
        const val LADDER_TICKS = 5
    }
}
