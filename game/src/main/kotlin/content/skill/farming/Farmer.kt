package content.skill.farming

import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.random

class Farmer(
    val enumDefinitions: EnumDefinitions,
    val itemDefinitions: ItemDefinitions,
) : Script {

    init {
        npcOperate("Talk-to", "alain,amaethwr,dreven,dantaera,ellena,elstan,fayeth,francis,garth_brimhaven,gileth_observatory,heskel,imiago_tai_bwo_wannai_normal,kragen_ardougne,lyra,rhazien,rhonen,selena,taria,torrell,treznor,vasquen,bolongo,frizzy_skernip,praistan_ebola,prissy_scilla,yulf_squecks") { (target) ->
            choice {
                if (target.contains("patch")) {
                    val variable: String = target["patch"] ?: return@choice
                    if (variable.contains("tree") && variable.substringBeforeLast("_").endsWith("_dead")) {
                        option<Quiz>("Would you chop my ${if (variable.contains("fruit_tree")) "fruit " else ""}tree down for me?") {
                            npc<Confused>("Why? You look like you could chop it down yourself!")
                            choice {
                                option<Neutral>("Yes, you're right - I'll do it myself.")
                                option<Neutral>("I can't be bothered - I'd rather pay you to do it.") {
                                    npc<Neutral>("Well, it's a lot of hard work - if you pay me 200 Coins I'll chop it down for you.")
                                    if (inventory.contains("coins", 200)) {
                                        option<Sad>("I don't have 200 Coins I'm afraid.")
                                        return@option
                                    }
                                    choice {
                                        option<Neutral>("Here's 200 Coins - chop my tree down please.") {
                                            if (inventory.remove("coins", 200)) {
                                                chopDownTree(variable)
                                            }
                                        }
                                        option<Sad>("I don't want to pay that much, sorry.")
                                    }
                                }
                            }
                        }
                    } else {
                        option<Quiz>("Would you look after my crops for me?") {
                            protectPatch(variable)
                        }
                    }
                } else if (target.contains("north_patch")) {
                    option<Quiz>("Would you look after my crops for me?") {
                        npc<Neutral>("I might - which patch were you thinking of?")
                        choice {
                            option("The northwestern allotment") {
                                val variable: String = target["north_patch"] ?: return@option
                                protectPatch(variable)
                            }
                            option("The southeastern allotment") {
                                val variable: String = target["south_patch"] ?: return@option
                                protectPatch(variable)
                            }
                        }
                    }
                }
                option<Quiz>("Can you give me any farming advice?") {
                    npc<Neutral>(
                        when (random.nextInt(11)) {
                            0 -> "There is a special patch for growing Belladonna - I believe that it is somewhere near Draynor Manor, where the ground is a tad 'unblessed'"
                            1 -> "Hops are good for brewing ales. I believe there's a brewery up in Keldagrim somewhere, and I've heard rumours that a place called Phasmatys used to be good for that type of thing. 'Fore they all died, of course."
                            2 -> "There are many Farming areas out there. I know of Elstan's area near Falador, Dantaera's area near Catherby, Kragen's area near Ardougne and Lyra's area in Morytania. That said, I'm sure there's plenty more that I don't know about."
                            3 -> "Don't just throw away your weeds after you've raked a patch - put them in a compost bin and make some compost."
                            5 -> "You don't have to buy all your plantpots you know, you can make them yourself on a pottery wheel. If you're good enough at crafting, that is."
                            6 -> "You can put up to ten potatoes, cabbages or onions in vegetable sacks, although you can't have a mix in the same sack."
                            7 -> "If you want to grow fruit trees you could try a few places: Catherby, Brimhaven and The Gnome Stronghold all have fruit tree patches."
                            8 -> "You can fill plantpots with soil from any empty patch, if you have a gardening trowel."
                            9 -> "The only way to cure a bush or tree of disease is to prune away the diseased leaves with a pair of secateurs. For all other crops I would just apply some plant-cure."
                            10 -> "You can buy all the farming tools from farming shops, which can be found close to the allotments in catherby."
                            else -> "Vegetables, hops and flowers need constant watering - if you ignore my advice, you will sooner or later find yourself in possession of a dead farming patch."
                        },
                    )
                }
                option<Neutral>("I'll come back another time.")
            }
        }

        npcOperate("Pay", "alain,amaethwr,dreven,ellena,fayeth,francis,garth_brimhaven,gileth_observatory,heskel,imiago_tai_bwo_wannai_normal,rhazien,rhonen,selena,taria,torrell,treznor,vasquen,bolongo,frizzy_skernip,praistan_ebola,prissy_scilla,yulf_squecks") {
            val variable: String = it.target["patch"] ?: return@npcOperate
            if (!variable.contains("_tree") || !variable.substringBeforeLast("_").endsWith("_dead")) {
                protectPatch(variable)
                return@npcOperate
            }
            if (!inventory.contains("coins", 200)) {
                npc<Sad>("I'll want 200 Coins to chop down your tree.")
                return@npcOperate
            }
            choice("Pay 200 Coins to have your tree chopped down?") {
                option("Yes.") {
                    if (inventory.remove("coins", 200)) {
                        chopDownTree(variable)
                    }
                }
                option("No.")
            }
        }

        npcOperate("Pay (North)", "dantaera,kragen_ardougne") {
            protectPatch("north")
        }

        npcOperate("Pay (South)", "dantaera,kragen_ardougne") {
            protectPatch("south")
        }

        npcOperate("Pay (North-west)", "elstan,lyra") {
            protectPatch("north")
        }

        npcOperate("Pay (South-east)", "elstan,lyra") {
            protectPatch("south")
        }
    }

    private fun Player.chopDownTree(variable: String) {
        val value = get(variable, "weeds_3")
        val type = value.substringBeforeLast("_")
        set(variable, "weeds_0")
        sound("woodchop")
        ScrollOfLife.checkLife(this, type, chop = true)
    }

    fun requiredItems(value: String): Pair<List<Item>, List<Item>> {
        val def = itemDefinitions.get(value)
        if (value == "spirit_tree") {
            return listOf(Item("monkey_nuts", 5), Item("monkey_bar"), Item("ground_tooth")) to emptyList()
        }
        val id = def.getOrNull<String>("farming_protect_id") ?: return Pair(emptyList<Item>(), emptyList<Item>())
        val amount = def["farming_protect_amount", 1]
        val noted = if (itemDefinitions.contains("${id}_noted")) listOf(Item("${id}_noted", amount)) else emptyList()
        return Pair(listOf(Item(id, amount)), noted)
    }

    suspend fun Player.protectPatch(variable: String) {
        val value = get(variable, "weeds_3")
        if (value.endsWith("dead")) {
            return
        }
        if (value.endsWith("diseased")) {
            npc<Confused>("That patch is diseased - you should cure it before asking me to be responsible for it!")
            return
        }
        if (value.startsWith("stump")) {
            npc<Confused>("That's a stump. It'll grow back if you just leave it a alone for a while.")
            return
        }
        val stage = value.substringAfterLast("_")
        if (stage.startsWith("life") || stage.startsWith("claim")) {
            npc<Confused>("That patch is already fully grown! I don't know what you want me to do with it!")
            return
        }
        if (get("${variable}_protect", false)) {
            npc<Confused>("I'm already looking after that patch for you.")
            return
        }
        val def = ObjectDefinitions.get("${value.substringBeforeLast("_")}_fullygrown")
        val item: String = def.getOrNull("harvest") ?: return
        val harvest = enumDefinitions.get("farming_protection").getString(itemDefinitions.get(item).id).substringAfter(":")
        npc<Neutral>("If you like, but I want $harvest for that.")
        val (required, noted) = requiredItems(item)
        if (!inventory.remove(required) && (noted.isEmpty() || !inventory.remove(noted))) {
            player<Sad>("I'm afraid I don't have any of those at the moment.")
            npc<Neutral>("Well, I'm not wasting my time for free.")
            return
        }
        npc<Neutral>("That'll do nicely, ${if (male) "sir" else "madam"}. Leave it with me - I'll make sure that patch grows for you.")
        set("${variable}_protect", true)
    }
}
