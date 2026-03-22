package content.area.morytania.canifis

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Fidelio : Script {
    init {
        npcOperate("Talk-to", "fidelio") { (target) ->
            player<Happy>("Hello there.")
            npc<Neutral>("H-hello. You l-look like a s-stranger to these p-parts. Would you l-like to buy something? I h-have some s- special offers at the m-minute...some s-sample bottles for s-storing s-snail slime.")
            choice {
                option<Neutral>("Yes, please.") {
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks.") {
                    npc<Sad>("(sigh) Th-that's okay. Nobody ever w-wants to buy my wares. Oh, s-sure, if it was food or c-clothes they would though!")
                }
                option<Angry>("Why are your prices so high?") {
                    npc<Sad>("As you p-probably know, the h-humans hate our kind and k-keep us trapped here. To get my s-stocks I have to sneak into the human lands in s-secret!")
                }
            }
        }
    }
}
