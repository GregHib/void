package content.area.misthalin.draynor_village

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.*
import content.entity.sound.sound
import content.quest.quest
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.remove
import world.gregs.voidps.type.Tile


val floorItems: FloorItems by inject()

npcOperate("Talk-to", "aggie") {
    npc<Happy>("What can I help you with?")
    choice {
        option<Quiz>("What could you make for me?") {
            npc<Neutral>("I mostly just make what I find pretty. I sometimes make dye for the women's clothes to brighten the place up. I can make red, yellow and blue dyes. If you'd like some, just bring me the appropriate ingredients.")
            menu()
        }
        when (player.quest("prince_ali_resuce")) {
            "osman", "leela", "joe_one_beer", "joe_two_beers", "joe_three_beers", "tie_up_lady_keli" -> {
                option<Quiz>("Can you make skin paste?") {
                    if (player.holdsItem("ashes") && player.holdsItem("pot_of_flour") && player.holdsItem("bucket_of_water") && player.holdsItem("redberries")) {
                        npc<Happy>(" Yes I can. I see you already have the ingredients. Would you like me to mix some for you now?")
                        choice {
                            option<Happy>("Yes please. Mix me some skin paste.") {
                                npc<Happy>("That should be simple. Hand the things to Aggie then.")
                                items("redberries", "pot_of_flour", "You hand the ash, flour, water and redberries to Aggie. She tips the ingredients into a cauldron and mutters some words.")
                                npc<Uncertain>(" Tourniquet, Fenderbaum, Tottenham, Marshmallow, Marblearch.")
                                player.inventory.transaction {
                                    remove("ashes")
                                    remove("pot_of_flour")
                                    remove("bucket_of_water")
                                    remove("redberries")
                                    add("paste")
                                }
                                item("paste", 400, "Aggie hands you the skin paste.")
                                npc<Happy>("There you go dearie. That will make you look good at the Varrock dances.")
                            }
                            option<Neutral>("No thank you. I don't need any skin paste right now.") {
                                npc<Neutral>("Okay dearie, that's always your choice.")
                            }
                        }
                    } else {
                        npc<Happy>("Why, it's one of my most popular potions! Lots of people around here like to pretty their faces up a bit. I can make it for you if you get me what's needed.")
                        player<Quiz>("What do you need?")
                        npc<Neutral>("Well dearie, you need a base for the paste. That's a mix of ash, flour and water. Then you need redberries to colour it as you want. Bring me those four items and I will make you some.")
                    }
                }
            }
        }
        option<Happy>("Cool, do you turn people into frogs?") {
            npc<Neutral>("Oh, not for years, but if you meet a talking chicken, you have probably met the professor in the manor north of here. A few years ago it was flying fish. That machine is a menace.")
        }
        option<Angry>("You mad old witch, you can't help me.") {
            if (player.inventory.contains("pot_of_flour")) {
                player.inventory.remove("pot_of_flour")
                npc<Angry>("Oh, you like to call a witch names do you?")
                target.anim("pick_pocket")
                player.sound("pick")
                item("pot_of_flour", 600, "Aggie waves her hands about, and you seem to have a pot of flour less.")
                npc<Neutral>("Thank you for your kind present of some flour. I am sure you never meant to insult me.")
            } else if (player.inventory.contains("coins", 101)) {
                player.inventory.remove("coins", 20)
                npc<Angry>("Oh, you like to call a witch names do you?")
                target.anim("pick_pocket")
                player.sound("pick")
                item("coins", 600, "Aggie waves her hands about, and you seem to be 20 coins poorer.")
                npc<Neutral>("That's a fine for insulting a witch. You should learn some respect.")
            } else {
                npc<Angry>("You should be careful about insulting a witch. You never know what shape you could wake up in.")
            }
        }
        option<Quiz>("Can you make dyes for me please?") {
            npc<Quiz>("What sort of dye would you like? Red, yellow or blue?")
            menu()
        }
    }
}

suspend fun NPCOption<Player>.menu() {
    choice {
        option<Quiz>("What do you need to make red dye?") {
            npc<Neutral>("Three lots of redberries and five coins to you.")
            redDye()
        }
        option<Quiz>("What do you need to make yellow dye?") {
            npc<Neutral>("Two onions and five coins to you.")
            yellowDye()
        }
        option<Quiz>("What do you need to make blue dye?") {
            npc<Neutral>("Two woad leaves and five coins to you.")
            blueDye()
        }
        option<Neutral>("No thanks, I am happy the colour I am.") {
            npc<Neutral>("You are easily pleased with yourself then. When you need dyes, come to me.")
        }
    }
}

suspend fun NPCOption<Player>.yellowDye() {
    choice {
        option<Quiz>("Okay, make me some yellow dye please.") {
            if (!player.inventory.contains("coins", 5)) {
                statement("You don't have enough coins to pay for the dye.")
            } else if (!player.inventory.contains("onion", 2)) {
                statement("You don't have enough onions to make the yellow dye.")
            } else {
                target.walkTo(Tile(3085, 3258))
                delay(3)
                target.face(Tile(3085, 3258))
                target.anim("mixing_dye")
                areaGfx("mixing_yellow_dye", Tile(3085, 3258))
                player.inventory.transaction {
                    remove("coins", 5)
                    remove("onion", 2)
                    add("yellow_dye")
                }
                item("yellow_dye", 380, "You hand the onions and payment to Aggie. Aggie produces a yellow bottle and hands it to you.")
            }
        }
        option<Uncertain>("I don't think I have all the ingredients yet.") {
            npc<Neutral>("You know what you need to get, so come back when you have them. Goodbye for now.")
        }
        option<Angry>("I can do without dye at that price.") {
            npc<Neutral>("That's your choice, but I would think you have killed for less. I can see it in your eyes.")
        }
        option<Quiz>("Where do I get onions?") {
            npc<Neutral>("There are some onions growing on a farm to the east of here, next to the sheep field.")
            otherColours()
        }
        option<Quiz>("What other colours can you make?") {
            npc<Neutral>("Red, yellow and blue. Which one would you like?")
            menu()
        }
    }
}

suspend fun NPCOption<Player>.redDye() {
    choice {
        option<Quiz>("Okay, make me some red dye please.") {
            if (!player.inventory.contains("coins", 5)) {
                statement("You don't have enough coins to pay for the dye.")
            } else if (!player.inventory.contains("redberries", 3)) {
                statement("You don't have enough berries to make the red dye.")
            } else {
                target.walkTo(Tile(3085, 3258))
                delay(3)
                target.face(Tile(3085, 3258))
                target.anim("mixing_dye")
                areaGfx("mixing_red_dye", Tile(3085, 3258))
                player.inventory.transaction {
                    remove("coins", 5)
                    remove("redberries", 3)
                    add("red_dye")
                }
                item("red_dye", 380, "You hand the berries and payment to Aggie. Aggie produces a red bottle and hands it to you.")
            }
        }
        option<Uncertain>("I don't think I have all the ingredients yet.") {
            npc<Neutral>("You know what you need to get, so come back when you have them. Goodbye for now.")
        }
        option<Angry>("I can do without dye at that price.") {
            npc<Neutral>("That's your choice, but I would think you have killed for less. I can see it in your eyes.")
        }
        option<Quiz>("Where do I get redberries?") {
            npc<Neutral>("I pick mine from the woods south of Varrock. The food shop in Port Sarim sometimes has some as well.")
            otherColours()
        }
        option<Quiz>("What other colours can you make?") {
            npc<Neutral>("Red, yellow and blue. Which one would you like?")
            menu()
        }
    }
}

suspend fun NPCOption<Player>.blueDye() {
    choice {
        option<Quiz>("Okay, make me some blue dye please.") {
            if (!player.inventory.contains("coins", 5)) {
                statement("You don't have enough coins to pay for the dye.")
            } else if (!player.inventory.contains("woad_leaf", 2)) {
                statement("You don't have enough woad leaves to make the blue dye.")
            } else {
                target.walkTo(Tile(3085, 3258))
                delay(3)
                target.face(Tile(3085, 3258))
                target.anim("mixing_dye")
                areaGfx("mixing_blue_dye", Tile(3085, 3258))
                player.inventory.remove("coins", 5)
                player.inventory.remove("woad_leaf", 2)
                if (!player.inventory.add("blue_dye")) {
                    floorItems.add(player.tile, "blue_dye", disappearTicks = 300, owner = player)
                }
                item("blue_dye", 380, "You hand the woad leaves and payment to Aggie. Aggie produces a blue bottle and hands it to you.")
            }
        }
        option<Uncertain>("I don't think I have all the ingredients yet.") {
            npc<Neutral>("You know what you need to get, so come back when you have them. Goodbye for now.")
        }
        option<Angry>("I can do without dye at that price.") {
            npc<Neutral>("That's your choice, but I would think you have killed for less. I can see it in your eyes.")
        }
        option<Quiz>("Where do I get woad leaves?") {
            npc<Neutral>("Woad leaves are fairly hard to find. My other customers tell me that Wyson, the head gardener in Falador Park, grows them.")
            otherColours()
        }
        option<Quiz>("What other colours can you make?") {
            npc<Neutral>("Red, yellow and blue. Which one would you like?")
            menu()
        }
    }
}

suspend fun NPCOption<Player>.otherColours() {
    choice {
        option<Quiz>("What other colours can you make?") {
            npc<Neutral>("Red, yellow and blue. Which one would you like?")
            menu()
        }
        option<Happy>("Thanks.") {
            npc<Happy>("You're welcome!")
        }
    }
}