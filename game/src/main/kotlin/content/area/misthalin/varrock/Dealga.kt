package content.area.misthalin.varrock

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Shifty
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

class Dealga : Script {

    init {

        npcOperate("Talk-to", "dealga") {

            val amulet = equipped(EquipSlot.Amulet)
            val shopName = "dealgas_scimitar_emporium"

            if (amulet.id == "monkeyspeak_amulet") {

                npc<Shifty>("Oooh, still not used to dealing with you humans...")

                choice {
                    option<Quiz>("Do you have any Dragon Scimitars in stock?") {
                        npc<Happy>("It just so happens I recently got a fresh delivery.<br>Do you want to buy one?")
                        choice {
                            option<Neutral>("Yes, please.") {
                                openShop(shopName)
                            }
                            option<Neutral>("No, thanks.") {
                            }
                        }
                    }
                    option<Neutral>("Who are you?") {
                        npc<Shifty>(
                            "The name's Dealga, I shipped over from Ape Atoll a while back when I heard that these 'humans' pay good money for certain wares! Thought I'd come over here, and much like the dragon scimitar... make a killing!",
                        )
                        npc<Shifty>("Now, what can I do for you? A nice, keen edged dragon scimitar?")
                    }
                    option<Neutral>("What are you doing here?") {
                        npc<Shifty>(
                            "Like the keen edged dragon scimitar I'm slashing away the competition! If you hairless apes won't come to Ape Atoll, then I'll come to you! I'll soon be overtaking Daga in profitability!",
                        )
                        npc<Shifty>("Now, what can I do for you? A nice, keen edged dragon scimitar?")
                    }
                }
            } else {
                npc<Shifty>("Ook. Ook! Ook! Ah Ah! Ook Ah Uh!")
            }
        }
    }
}
