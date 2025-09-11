package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove

@Script
class Harry {

    init {
        npcOperate("Talk-to", "harry") {
            npc<Talk>("Welcome! You can buy Fishing equipment at my store. We'll also give you a good price for any fish that you catch.")

            choice {
                option("Let's see what you've got, then.") {
                    player.openShop("harrys_fishing_shop")
                }

                if (player.holdsItem("fishbowl_water") || player.holdsItem("fishbowl_seaweed")) {
                    option("Can I get a fish for this bowl?") {
                        player<Talk>("Can I get a fish for this bowl?")

                        when {
                            player.holdsItem("fishbowl_water") -> {
                                npc<Talk>("Sorry, you need to put some seaweed into the bowl first")
                                player<Neutral>("Seaweed?")
                                npc<Talk>("Yes, the fish seem to like it. Come and see me when you have put some in the bowl.")
                            }

                            player.holdsItem("fishbowl_seaweed") -> {
                                npc<Happy>("Yes, you can. I can see that you have a nicely filled fishbowl there to use, and you can catch a fish from my aquarium if you want. You will need a special net to do this though - I sell them for 10 gold.")
                                choice {
                                    option("I'll take it.") {
                                        if (player.inventory.contains("coins", 10)) {
                                            player<Talk>("I'll take it.")
                                            player.inventory.remove("coins", 10)
                                            player.inventory.add("tiny_net")
                                            npc<Happy>("Here you go.")
                                            // Optionally add a game message: "Harry sells you a tiny net."
                                        } else {
                                            npc<Neutral>("It looks like you don't have enough coins.")
                                        }
                                    }
                                    option("No, thanks; later, maybe.") {
                                        player<Neutral>("No, thanks; later, maybe.")
                                    }
                                }
                            }
                        }
                    }
                }

                // TODO: add fishing spot for tiny_net & get pet_fish to work like it is in real runescape

                if (player.holdsItem("fishbowl_water") || player.holdsItem("fishbowl_seaweed") || player.holdsItem("fishbowl")) {
                    option("Do you have any fish food?") {
                        player<Talk>("Do you have any fish food?")
                        npc<Talk>("Sorry, I'm all out. I used up the last of it feeding the fish in the aquarium. I have some empty boxes, though - they have the ingredients written on the back.")
                        npc<Neutral>("I'm sure if you pick up a pestle and mortar you will be able to make your own.")
                        player.inventory.add("empty_box_fish")
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
