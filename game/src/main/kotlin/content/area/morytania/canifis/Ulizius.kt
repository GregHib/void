package content.area.morytania.canifis

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Scared
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.quest
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
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
            val ulizius = NPCs.findOrNull(Region(13622).toLevel(0), "ulizius") ?: return@objectOperate
            val enter = tile.y >= target.tile.y
            if (enter && !questCompleted("nature_spirit")) {
                talkWith(ulizius)
                canIGoThrough()
                return@objectOperate
            }
            enterDoor(target)
            if (!enter) {
                ulizius.say("Oh my! You're still alive!")
            }
        }
    }

    private suspend fun Player.canIGoThrough() {
        player<Neutral>("Can I go through the gate please?")
        if (questCompleted("nature_spirit")) {
            npc<Happy>("Yes of course my friend, you seem to be able to handle yourself with those ghasts really well.")
        } else {
            npc<Scared>("Absolutely not! I've been given strict instructions not to let anyone through. It's just too dangerous. No one gets in without Drezels say so!")
            if (quest("nature_spirit") == "unstarted") {
                player<Quiz>("Where is Drezel?")
                npc<Neutral>("Oh, he's in the temple, just go back over the bridge, down the ladder and along the hallway, you can't miss him.")
            } else {
                player<Neutral>("But I'm doing a quest for Drezel!")
                npc<Neutral>("Ok, on your way then!")
            }
        }
    }
}
