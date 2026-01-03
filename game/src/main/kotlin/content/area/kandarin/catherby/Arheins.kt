package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script

class Arheins : Script {

    init {
        npcOperate("Talk-to", "arhein") {
            npc<Neutral>("Hello! Would you like to trade?")

            choice {
                option("Yes.") {
                    player<Neutral>("Sure.")
                    openShop("arheins_store")
                }
                option("No thank you.") {
                    player<Neutral>("No thanks.")
                }

                option("Is that your ship?") {
                    player<Neutral>("Is that your ship?")
                    npc<Neutral>("Yes, I use it to make deliveries to my customers up and down the coast. These crates here are all ready for my next trip.")

                    choice {
                        option("Where do you deliver to?") {
                            player<Neutral>("Where do you deliver to?")
                            npc<Neutral>("Oh, various places up and down the coast. Mostly Karamja and Port Sarim.")

                            choice {
                                option("I don't suppose I could get a lift anywhere?") {
                                    player<Neutral>("I don't suppose I could get a lift anywhere?")
                                    npc<Neutral>("Sorry pal, but I'm afraid I'm not quite ready to sail yet.")
                                    npc<Neutral>("I'm waiting on a big delivery of candles which I need to deliver further along the coast.")
                                    // End of dialogue
                                }

                                option("Well, good luck with your business.") {
                                    player<Neutral>("Well, good luck with your business.")
                                    npc<Neutral>("Thanks buddy!")
                                    // End of dialogue
                                }
                            }
                        }

                        option("Are you rich then?") {
                            player<Neutral>("Are you rich then?")
                            npc<Neutral>("Business is going reasonably well... I wouldn't say I was the richest of merchants ever, but I'm doing fairly well all things considered.")
                        }
                    }
                }
            }
        }

        objectOperate("Cross", "arhein_ship_gangplank") {
            npc("arhein", "Hey buddy! Get away from my ship alright?")
            player<Neutral>("Yeah... uh... sorry...")
        }
    }

    // TODO: add One Small Favour Quest & fill out the rest of dialogue out for quest
}
