package content.area.asgarnia.falador

import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Lucy : Script {

    init {
        npcOperate("Talk-to", "lucy") { (target) ->
            npc<Happy>("Hi! I'm Lucy. Welcome to the Party Room!")
            player<Neutral>("Hi.")
            npc<Quiz>("Would you like to buy a beer?")
            player<Quiz>("How much do they cost?")
            npc<Happy>("Just 2 gold pieces.")
            choice {
                option<Neutral>("Yes please!") {
                    beer()
                }
                option<Neutral>("No thanks, I can't afford that.") {
                    npc<Neutral>("I see. Well, come and see me if you change your mind. You know where I am!")
                }
            }
        }
    }

    suspend fun Player.beer() {
        npc<Happy>("Coming right up ${if (male) "sir" else "ma'am"}!")
        if (inventory.remove("coins", 2)) {
            npc<Angry>("I said 2 coins! You haven't got 2 coins!")
            player<Sad>("Sorry. I'll come back another day.")
            return
        }
        addOrDrop("beer")
        player<Happy>("Thanks, Lucy.")
    }
}
