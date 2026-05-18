package content.area.morytania.canifis

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.dialogue.type.statement
import content.entity.player.dialogue.type.warning
import content.quest.member.myreque.nature_spirit
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.type.Region

class Ulizius : Script {
    init {
        npcOperate("Talk-to", "ulizius") {
            player<Neutral>("Hello there.")
            npc<Scared>("What... Oh, don't creep up on me like that... I thought you were a Ghast!")
            canIGoThrough()
        }

        objectOperate("Open", "gate_mort_myre*_closed") { (target) ->
            val ulizius = NPCs.findOrNull(Region(13622).toLevel(0), "ulizius")
            val entering = tile.y >= target.tile.y

            if (!entering) {
                enterDoor(target)
                ulizius?.say("Oh my! You're still alive!")
                return@objectOperate
            }

            if (nature_spirit == 0) {
                statement(
                    "There's a message attached to this gate, it reads:<br>" +
                            "<navy> ~ Mort Myre is a dangerous Ghast infested swamp. ~<br>" +
                            "<navy> ~ Do not enter if you value your life. ~<br>" +
                            "<navy> ~ All persons wishing to enter must see Drezel. ~"
                )
                return@objectOperate
            }

            if (!warning("mort_myre")) {
                return@objectOperate
            }

            enterDoor(target)
        }
    }

    private suspend fun Player.canIGoThrough() {
        player<Quiz>("Can I go through the gate please?")
        if (questCompleted("nature_spirit")) {
            npc<Happy>("Yes of course my friend, you seem to be able to handle yourself with those ghasts really well.")
            return
        }
        npc<Scared>(
            "Absolutely not. I've been given strict instructions not to let anyone through. It's " +
                    "just too dangerous. No one gets in without Drezel's say so!"
        )
        if (nature_spirit == 0) {
            player<Quiz>("Where is Drezel?")
            npc<Scared>(
                "Oh, he's in the temple, just go back over the bridge, down the ladder and along " +
                        "the hallway, you can't miss him."
            )
        } else {
            player<Neutral>("But I'm doing a quest for Drezel!")
            npc<Scared>("Ok, on your way then!")
        }
    }
}
