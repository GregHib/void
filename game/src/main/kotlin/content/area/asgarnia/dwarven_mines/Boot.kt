package content.area.asgarnia.dwarven_mines

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class Boot : Script {

    init {
        npcOperate("Talk-to", "boot_dwarven_mine") { (target) ->
            npc<Neutral>("Hello tall person.")
            menu()
        }
    }

    private suspend fun Player.menu() = choice("Select an Option") {
        option<Neutral>("Hello short person.") {
            npc<Neutral>("Hello tall person.")
        }
        option("Why are you called boot?") {
            player<Quiz>("Why are you called Boot?")
            npc<Neutral>("I'm called Boot, because when I was very young, I used to sleep, in a large boot.")
            player<Neutral>("Yeah, great, I didn't want your life story.")
        }
    }
}
