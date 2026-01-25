package content.area.asgarnia.falador

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Herquin : Script {

    init {
        npcOperate("Talk-to", "herquin") {
                choice {
                    option("Do you wish to trade?") {
                        player<Neutral>("Do you wish to trade?")
                        npc<Neutral>("Why, yes, this is a jewel shop after all.")
                        openShop("herquins_gems")
                    }
                    option("Sorry, I don't want to talk to you, actually.") {
                        player<Neutral>("Sorry, I don't want to talk to you, actually.")
                        npc<Neutral>("Huh, charming.")
                    }
                }

            }
        }
    }