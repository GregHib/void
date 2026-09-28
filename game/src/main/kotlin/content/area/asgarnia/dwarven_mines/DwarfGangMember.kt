package content.area.asgarnia.dwarven_mines

import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player

class DwarfGangMember : Script {

    init {
        npcOperate("Talk-to", "dwarf_gang_member,dwarf_gang_member_2,dwarf_gang_member_3") {
            npc<Neutral>("Yeah...whada you want?")
            menu()
        }
    }

    private suspend fun Player.menu() {
        choice {
            option<Quiz>("Who are you?") {
                npc<Neutral>("I'm nobody you need to worry about...who are you? Why are you asking so many questions? Are you with the Varrock guards?")
                menu()
            }
            option<Quiz>("What're you doing here?") {
                npc<Neutral>("Well, as if it's any of your business, I'm an associate of Hammerspike. He's a great dwarf you know. You could learn a lot from a dwarf like him.")
                menu()
            }
            option<Quiz>("Who do you work for?") {
                npc<Neutral>("I have an ongoing contract with Hammerspike, when he gives the word, the hammer starts flying.")
                menu()
            }
            option<Neutral>("Okay, thanks.")
        }
    }
}
