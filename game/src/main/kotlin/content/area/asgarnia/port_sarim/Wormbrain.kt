package content.area.asgarnia.port_sarim

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Wormbrain : Script {

    init {
        npcOperate("Talk-to", "wormbrain_port_sarim") {
            npc<Confused>("Whut you want?")
            menu()
        }
    }

    private suspend fun Player.menu() = choice("Select an Option") {
        option<Quiz>("What are you in for?") {
            npc<Confused>("Me not sure. Me pick some stuff up and take it away.")
            player<Quiz>("Well, did the stuff belong to you?")
            npc<Confused>("Umm...no.")
            player<Neutral>("Well, that would be why then.")
            npc<Happy>("Oh, right.")
        }
        option<Neutral>("Sorry, thought this was a zoo.") {
        }
    }
}
