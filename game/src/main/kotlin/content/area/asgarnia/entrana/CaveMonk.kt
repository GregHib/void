package content.area.asgarnia.entrana

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player

class CaveMonk : Script {
    init {
        npcOperate("Talk-to", "cave_monk") { (target) ->
            careful()
        }

        objectOperate("Climb-down", "entrana_ladder") {
            val monk = NPCs.find(tile.regionLevel, "cave_monk")
            talkWith(monk)
            careful()
        }
    }

    private suspend fun Player.careful() {
        npc<Confused>("Be careful going in there! You are unarmed, and there is much evilness lurking down there! The evilness seems to block off our contact with our gods,")
        npc<Confused>("so our prayers seem to have less effect down there. Oh, also, you won't be able to come back this way -  This ladder only goes one way!")
        npc<Confused>("The only exit from the caves below is a portal which leads only to the deepest wilderness!")
        choice {
            option<Sad>("I don't think I'm strong enough to enter then.")
            option<Neutral>("Well that is a risk I will have to take.") {
                message("You climb down the ladder.")
                tele(2822, 9774)
            }
        }
    }
}
