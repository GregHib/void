package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Laugh
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Megan : Script {

    init {
        npcOperate("Talk-to", "megan_falador") { (target) ->
            npc<Happy>("Hi! I'm Megan. Welcome to the Party Room!")
            choice {
                option<Neutral>("One beer please Megan!") {
                    beer()
                }
                option<Confused>("Can you dance, Megan?") {
                    npc<Happy>("Can I dance?!")
                    npc<Laugh>("CAN I dance?!!")
                    player<Happy>("Dance with me Megan!")
                    dance(target)
                }
                option<Neutral>("Do you have any news?") {
                    npc<Neutral>("Not at the moment. I've heard that the known world is expanding as new places are discovered.")
                    npc<Happy>("These are exciting times indeed!")
                }
                option<Neutral>("Never mind.")
            }
        }
    }

    suspend fun Player.beer() {
        npc<Happy>("Certainly ${if (male) "sir" else "ma'am"}! Coming right up! That's two coins, please.")
        if (!inventory.remove("coins", 2)) {
            npc<Angry>("I said 2 coins! You haven't got 2 coins!")
            player<Sad>("Sorry. I'll come back another day.")
            return
        }
        addOrDrop("beer")
        player<Happy>("Thanks, Megan.")
    }

    suspend fun Player.dance(megan: NPC) {
        anim("emote_yes")
        megan.anim("emote_yes")
        delay(1)
        anim("emote_twirl")
        megan.anim("emote_twirl")
        delay(2)
        anim("emote_yes")
        megan.anim("emote_yes")
        delay(1)
        anim("emote_dance")
        megan.anim("emote_dance")
        delay(7)
        anim("emote_twirl")
        megan.anim("emote_twirl")
        delay(2)
        anim("emote_jig")
        megan.anim("emote_jig")
        delay(5)
        anim("emote_jump_for_joy")
        megan.anim("emote_jump_for_joy")
    }
}
