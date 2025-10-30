package content.area.kandarin.catherby

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Talk
import content.entity.player.dialogue.type.*
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.npc.npcOperate

class Ellena : Script {

    init {
        npcOperate("Talk-to", "ellena") {
            suspend fun showMainOptions() {
                choice {
                    option("Would you look after my crops for me?") {
                        player<Talk>("Would you look after my crops for me?")
                        npc<Talk>("I'm afraid I can't help with that right now. Maybe once the Farming system is ready.")
                    }

                    option("Can you give me any farming advice?") {
                        player<Talk>("Can you give me any Farming advice?")
                        val tips = listOf(
                            "You can put up to five tomatoes, strawberries, apples, bananas or oranges into a fruit basket, although you can't have a mix in the same basket.",
                            "There are seven main Farming areas - Elstan's area near Falador, Dantaera's area near Catherby, Kragen's area near Ardougne, Lyra's area in Morytania, Marisi's area in Hosidius, Alan's area in the Farming Guild and Oswallt's area in Prifddinas.",
                            "Supercompost is far better than normal compost, but more expensive to make. You need to rot the right type of item; show me an item, and I'll tell you if it's super-compostable or not.",
                            "Vegetables, hops and flowers need constant watering - if you ignore my advice, you will sooner or later find yourself in possession of a dead farming patch.",
                            "There is a special patch for growing Belladonna - I believe that it is somewhere near Draynor Manor, where the ground is a tad 'unblessed'.",
                            "Hops are good for brewing ales. I believe there's a brewery up in Keldagrim somewhere, and I've heard rumours that a place called Phasmatys used to be good for that type of thing. 'Fore they all died, of course.",
                            "Bittercap mushrooms can only be grown in a special patch in Morytania, near the Mort Myre swamp. There the ground is especially dank and suited to growing poisonous fungii.",
                            "You can buy all the farming tools from farming shops, which can be found close to the allotments.",
                            "Tree seeds must be grown in a plantpot of soil into a sapling, and then transferred to a tree patch to continue growing to adulthood.",
                            "The only way to cure a bush or tree of disease is to prune away the diseased leaves with a pair of secateurs. For all other crops I would just apply some plant-cure.",
                            "You can fill plantpots with soil from any empty patch, if you have a gardening trowel.",
                            "If you need to be rid of your fruit trees for any reason, all you have to do is chop them down and then dig up the stump.",
                            "Don't just throw away your weeds after you've raked a patch - put them in a compost bin and make some compost.",
                            "Applying compost to a patch will not only reduce the chance that your crops will get diseased, but you will also grow more crops to harvest.",
                            "If you want to make your own sacks and baskets you'll need to use the loom that's near the Farming shop in Falador. If you're a good enough craftsman, that is.",
                            "You don't have to buy all your plantpots you know, you can make them yourself on a pottery wheel. If you're a good enough craftsman, that is.",
                            "You can put up to ten potatoes, cabbages or onions in vegetable sacks, although you can't have a mix in the same sack.",
                        )
                        npc<Talk>(tips.random())
                        showMainOptions()
                    }

                    option("Can you sell me something?") {
                        player<Talk>("Can you sell me something?")
                        npc<Talk>("That depends on whether I have it to sell. What is it that you're looking for?")
                        player.openShop("ellena_farming_supplies")
                    }

                    option("I'll come back another time.") {
                        player<Talk>("I'll come back another time.")
                        // Ends dialogue
                    }
                }
            }

            showMainOptions()
        }
    }
}
