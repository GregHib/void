package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script

@Script
class Arheins {

    init {
        npcOperate("Talk-to", "arhein") {
            npc<Talk>("Hello! Would you like to trade?")

            choice {
                option("Yes.") {
                    player<Talk>("Sure.")
                    player.openShop("arheins_store")
                }
                option("No thank you.") {
                    player<Talk>("No thanks.")
                }

                option("Is that your ship?") {
                    player<Talk>("Is that your ship?")
                    npc<Talk>("Yes, I use it to make deliveries to my customers up and down the coast. These crates here are all ready for my next trip.")

                    choice {
                        option("Where do you deliver to?") {
                            player<Talk>("Where do you deliver to?")
                            npc<Talk>("Oh, various places up and down the coast. Mostly Karamja and Port Sarim.")

                            choice {
                                option("I don't suppose I could get a lift anywhere?") {
                                    player<Talk>("I don't suppose I could get a lift anywhere?")
                                    npc<Talk>("Sorry pal, but I'm afraid I'm not quite ready to sail yet.")
                                    npc<Talk>("I'm waiting on a big delivery of candles which I need to deliver further along the coast.")
                                    // End of dialogue
                                }

                                option("Well, good luck with your business.") {
                                    player<Talk>("Well, good luck with your business.")
                                    npc<Talk>("Thanks buddy!")
                                    // End of dialogue
                                }
                            }
                        }

                        option("Are you rich then?") {
                            player<Talk>("Are you rich then?")
                            npc<Talk>("Business is going reasonably well... I wouldn't say I was the richest of merchants ever, but I'm doing fairly well all things considered.")
                        }
                    }
                }
            }
        }

        objectOperate("Cross", "arhein_ship_gangplank") {
            npc("arhein", "Hey buddy! Get away from my ship alright?")
            player<Talk>("Yeah... uh... sorry...")
        }
    }

    // TODO: add One Small Favour Quest & fill out the rest of dialogue out for quest
}
