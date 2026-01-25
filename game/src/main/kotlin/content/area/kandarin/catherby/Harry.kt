package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

class Harry : Script {

    init {
        npcOperate("Talk-to", "harry") {
            npc<Neutral>("Welcome! You can buy Fishing equipment at my store. We'll also give you a good price for any fish that you catch.")

            choice {
                option("Let's see what you've got, then.") {
                    openShop("harrys_fishing_shop")
                }

                if (carriesItem("fishbowl_water") || carriesItem("fishbowl_seaweed")) {
                    option("Can I get a fish for this bowl?") {
                        player<Neutral>("Can I get a fish for this bowl?")

                        when {
                            carriesItem("fishbowl_water") -> {
                                npc<Neutral>("Sorry, you need to put some seaweed into the bowl first")
                                player<Idle>("Seaweed?")
                                npc<Neutral>("Yes, the fish seem to like it. Come and see me when you have put some in the bowl.")
                            }

                            carriesItem("fishbowl_seaweed") -> {
                                npc<Happy>("Yes, you can. I can see that you have a nicely filled fishbowl there to use, and you can catch a fish from my aquarium if you want. You will need a special net to do this though - I sell them for 10 gold.")
                                choice {
                                    option("I'll take it.") {
                                        if (inventory.contains("coins", 10)) {
                                            player<Neutral>("I'll take it.")
                                            inventory.remove("coins", 10)
                                            inventory.add("tiny_net")
                                            npc<Happy>("Here you go.")
                                            // Optionally add a game message: "Harry sells you a tiny net."
                                        } else {
                                            npc<Idle>("It looks like you don't have enough coins.")
                                        }
                                    }
                                    option("No, thanks; later, maybe.") {
                                        player<Idle>("No, thanks; later, maybe.")
                                    }
                                }
                            }
                        }
                    }
                }

                // TODO: add fishing spot for tiny_net & get pet_fish to work like it is in real runescape

                if (carriesItem("fishbowl_water") || carriesItem("fishbowl_seaweed") || carriesItem("fishbowl")) {
                    option("Do you have any fish food?") {
                        player<Neutral>("Do you have any fish food?")
                        npc<Neutral>("Sorry, I'm all out. I used up the last of it feeding the fish in the aquarium. I have some empty boxes, though - they have the ingredients written on the back.")
                        npc<Idle>("I'm sure if you pick up a pestle and mortar you will be able to make your own.")
                        inventory.add("empty_box_fish")
                        npc<Happy>("Here. I can hardly charge you for an empty box.")
                    }
                }

                option("Sorry, I'm not interested.") {
                    // Ends dialogue naturally
                }
            }
        }
    }
}
