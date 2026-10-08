package content.area.asgarnia.port_sarim

import content.entity.combat.dead
import content.entity.combat.killer
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player

class RickTurpentine : Script {

    init {
        npcOperate("Talk-to", "rick_turpentine") {
            menu()
        }

        npcDeath("mugger_port_sarim") {
            if (killer !is Player) {
                return@npcDeath
            }
            val rick = NPCs.at(tile.regionLevel)
                .sortedBy { it.tile.distanceTo(tile) }
                .firstOrNull { it.id == "rick_turpentine" && !it.dead && !it.hide && it.tile.within(tile, 5) }
                ?: return@npcDeath
            rick.say("Hahaha! Thanks!")
            rick.anim("emote_cheer")
        }
    }

    private suspend fun Player.menu() {
        npc<Quiz>("Stand and deliver?")
        options()
    }

    private suspend fun Player.options() {
        choice("What would you like to say?") {
            option<Quiz>("What are you doing?") {
                npc<Happy>("I used to roam the lands robbing travellers and giving the loot to anyone I liked. It was such a happy life, and I didn't really do much harm.")
                npc<Angry>("Suddenly they've decided that wealth redistribution is a crime, so they've stuck me in here with a stupid mugger. We even have to take turns using the bed!")
                npc<Neutral>("I still shout 'Stand and deliver' at people, but it doesn't really work so well from in here.")
                player<Neutral>("That's really sad.")
                npc<Happy>("You could cheer me up by shooting the mugger. I like it when people shoot the mugger. I like it when people shoot the mugger, even though he always comes back.")
                options()
            }
            option<Laugh>("Hahaha, you're locked up.") {
                npc<Sad>("No need to rub it in!")
            }
            option<Neutral>("I'm leaving now.") {
                npc<Neutral>("I guess I'll be here next time you come by.")
            }
        }
    }
}
