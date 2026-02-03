package content.area.misthalin.varrock.champions_guild

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script

class Scavvo : Script {
    init {
        npcOperate("Talk-to", "scavvo") { (target) ->
            npc<Happy>("'Ello matey! D'ya wanna buy some exciting new toys?")
            choice {
                option<Confused>("No, toys are for kids.") {
                }
                option("Let's have a look then.") {
                    openShop(target.def["shop", ""])
                }
                option<Happy>("Ooh, goody goody, toys!") {
                    openShop(target.def["shop", ""])
                }
                option<Quiz>("Why do you sell most rune armour but not platebodies?") {
                    npc<Neutral>("Oh, you have to complete a special quest in order to wear rune platemail. You should talk to the Guildmaster downstairs about that.")
                }
            }
        }
    }
}
