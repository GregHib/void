package content.quest.member.tree_gnome_village

import content.entity.npc.shop.openShop
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.inventory

class TreeGnomeVillageLocals : Script {
    init {
        npcOperate("Talk-to", "elkoy_tree_gnome_village,elkoy_tree_gnome_village_2,elkoy_tree_gnome_village_3,elkoy_tree_gnome_village_4") { (target) ->
            elkoy(target.tile.y < 3180)
        }
        npcOperate("Talk-to", "remsai_tree_gnome_village") { remsai() }
        npcOperate("Talk-to", "kalron_tree_gnome_village") {
            val id = "kalron_tree_gnome_village"
            when {
                questStage("tree_gnome_village") == 0 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>(id, "Gotta find a way out. We built this maze for protection but I can't get used to it. I'm always getting lost.")
                }
                questStage("tree_gnome_village") == 9 -> {
                    player<Neutral>("Hello there, you look lost.")
                    npc<Neutral>(id, "Are you trying to be funny?")
                    player<Neutral>("No.")
                    npc<Neutral>(id, "Hmmm.")
                }
                questStage("tree_gnome_village") >= 8 -> {
                    player<Neutral>("Hello little man.")
                    npc<Neutral>(id, "Hello. I hope they come out and find me soon. It's getting cold.")
                }
                questStage("tree_gnome_village") >= 6 -> {
                    player<Neutral>("Hello there.")
                    npc<Neutral>(id, "Oh my, oh my, the village has been pillaged and I'm still lost. Oh dear.")
                }
                else -> {
                    player<Neutral>("Hello, how are you?")
                    npc<Neutral>(id, "Oh my. I'll never find my way back before Khazard's men come and hunt me down.")
                }
            }
        }
        npcOperate("Talk-to", "local_gnome_tree_gnome_village") {
            val id = "local_gnome_tree_gnome_village"
            when (questStage("tree_gnome_village")) {
                0 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>(id, "Lardi dee, lardi da.")
                    player<Neutral>("Are you alright?")
                    npc<Neutral>(id, "Hee hee, lardi da, lardi dee. (The gnome appears to be singing.)")
                }
                5 -> {
                    player<Neutral>("Hello little man.")
                    npc<Neutral>(id, "Little man stronger than big man. Hee hee, lardi dee, lardi da.")
                }
                6, 7 -> {
                    player<Neutral>("Hi.")
                    npc<Neutral>(id, "Must save the orbs and kill the Khazard warlord. That will be fun, hee hee.")
                }
                8 -> {
                    player<Neutral>("Hello gnome.")
                    npc<Neutral>(id, "Soon we're gonna have the sacred ceremony and boy am I going to party. Lock up your daughters. Hee hee.")
                }
                else -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>(id, "You're the best!")
                    player<Neutral>("Thanks.")
                    npc<Neutral>(id, "Well I'm better. Hee hee.")
                }
            }
        }
        npcOperate("Trade", "bolkoy_tree_gnome_village") { openShop("bolkoys_village_shop") }
        npcOperate("Talk-to", "bolkoy_tree_gnome_village") {
            val id = "bolkoy_tree_gnome_village"
            when (questStage("tree_gnome_village")) {
                5 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>(id, "Amazing, you recovered the orb.")
                    npc<Neutral>(id, "Well I am impressed. Would you like to buy something?")
                }
                6, 7 -> {
                    player<Neutral>("Hi.")
                    npc<Neutral>(id, "Oh, hello there. Have you heard? They took the other orbs, it's terrible. I suppose the show must go on.")
                    npc<Neutral>(id, "Would you like to buy something?")
                }
                8 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>(id, "Hello there. You're that hero who saved the orbs. Soon we will perform the ritual and the village will be safe again.")
                    npc<Neutral>(id, "Anyway, would you like anything from my shop?")
                }
                9 -> {
                    player<Neutral>("Hello.")
                    npc<Neutral>(id, "Welcome, welcome. It's good to see you again. The village is much safer now you have returned the orbs.")
                    npc<Neutral>(id, "By the way, I'm the village shop keeper. Would you like to buy something?")
                }
                else -> {
                    player<Neutral>("Hello there.")
                    npc<Neutral>(id, "Hello stranger, new to these parts?")
                    npc<Neutral>(id, "I'm Bolkoy by the way. I'm the village shopkeeper. Would you like to buy something?")
                }
            }
            choice {
                option<Neutral>("What have you got?") {
                    npc<Neutral>(id, "Take a look.")
                    openShop("bolkoys_village_shop")
                }
                option<Neutral>("No thank you.") { npc<Neutral>(id, "Ok, maybe later.") }
            }
        }
    }

    private suspend fun Player.elkoy(inside: Boolean) {
        val stage = questStage("tree_gnome_village")
        if (stage == 0) {
            player<Neutral>("Hello there.")
            npc<Neutral>(ELKOY, if (inside) "Hello, welcome to our village. I'm Elkoy the tree gnome." else "Hello, welcome to our maze. I'm Elkoy the tree gnome.")
            player<Neutral>(if (inside) "I haven't heard of your sort before." else "I haven't heard of your sort.")
            npc<Neutral>(ELKOY, "There's not many of us left. Once you could find tree gnomes anywhere in the world, now we hide in small groups to avoid capture.")
            player<Neutral>("Capture by whom?")
            npc<Neutral>(ELKOY, "Tree gnomes have been hunted for so called 'fun' since as long as I can remember.")
            npc<Neutral>(ELKOY, "Our main threat nowadays are General Khazard's troops. They know no mercy, but are also very dense. They'll never find their way through our maze.")
            npc<Neutral>(ELKOY, "Have fun.")
            return // The first maze traversal is required before Elkoy offers escorts.
        }
        when {
            stage == 9 -> {
                player<Neutral>("Hello Elkoy.")
                npc<Neutral>(ELKOY, if (inside) "Hi there, I hope life is treating you well. Would you like me to show you the way out of the village?" else "Hi there, I hope life is treating you well. Would you like me to show you the way to the village?")
                escortChoice(inside, if (inside) "Here we are. Have a safe journey." else "Here we are. Feel free to have a look around.", no = if (inside) "Not now, thanks." else "No thanks Elkoy.")
            }
            stage >= 8 -> {
                player<Neutral>("Hello Elkoy.")
                npc<Neutral>(ELKOY, "You truly are a hero.")
                player<Neutral>("Thanks.")
                npc<Neutral>(ELKOY, "You saved us by returning the orbs of protection. I'm humbled and wish you well.")
                npc<Neutral>(ELKOY, if (inside) "Would you like me to show you through the maze?" else "Would you like me to show you the way to the village?")
                escortChoice(inside, if (inside) "Here we are. Have a safe journey." else "Here we are. Feel free to have a look around.", no = if (inside) "Not now, thanks." else "No thanks Elkoy.")
            }
            stage >= 6 -> {
                player<Neutral>("Hello Elkoy.")
                npc<Neutral>(ELKOY, "Did you hear?")
                npc<Neutral>(ELKOY, "Khazard's men have pillaged the village! They slaughtered many, and took the other orbs in an attempt to lead us out of the maze. When will the misery end?")
                if (inside) {
                    choice {
                        option<Neutral>("Can you show me out of the village?") { gnomeEscort(true, "Please help us find the orbs.") }
                        option<Neutral>("I'm very sorry.") { }
                    }
                } else {
                    npc<Neutral>(ELKOY, "Would you like me to show you the way to the village?")
                    escortChoice(false, "Here we are. Despite what has happened here, I hope you feel welcome.", no = "No thanks Elkoy.")
                }
            }
            stage == 5 && inventory.contains("orb_of_protection") -> {
                if (inside) {
                    player<Neutral>("Hello Elkoy. I have the orb.")
                    npc<Neutral>(ELKOY, "Take it to King Bolren, I'm sure he'll be pleased to see you.")
                    choice {
                        option<Neutral>("Can you show me out of the village?") { gnomeEscort(true, "Please return with our orb soon.") }
                        option<Neutral>("Okay.") { }
                    }
                } else {
                    player<Neutral>("Hello Elkoy.")
                    npc<Neutral>(ELKOY, "You're back! And the orb?")
                    player<Neutral>("I have it here.")
                    npc<Neutral>(ELKOY, "You're our saviour. Please return it to the village and we are all saved. Would you like me to show you the way to the village?")
                    choice {
                        option<Neutral>("Yes please.") { gnomeEscort(false, "Here we are. Take the orb to King Bolren, I'm sure he'll be pleased to see you.") }
                        option<Neutral>("No thanks Elkoy.") { npc<Neutral>(ELKOY, "Please, we must have the orb if we are to survive.") }
                    }
                }
            }
            stage == 5 -> {
                player<Neutral>("Hello Elkoy.")
                npc<Neutral>(ELKOY, "You're back! And the orb?")
                player<Neutral>("No, I'm afraid not.")
                npc<Neutral>(ELKOY, "Please, we must have the orb if we are to survive. Do you need me to show you through the maze?")
                escortChoice(inside, "Please help us get our orb back.")
            }
            stage >= 2 -> {
                player<Neutral>("Hello.")
                npc<Neutral>(ELKOY, "You must retrieve the orb, or the gnome village is doomed. Do you need me to show you through the maze?")
                escortChoice(inside, "Please help us get our orb back.")
            }
            else -> {
                player<Neutral>("Hello Elkoy.")
                npc<Neutral>(ELKOY, "Oh my! Oh my!")
                player<Neutral>("What's wrong?")
                npc<Neutral>(ELKOY, if (inside) "The orb, they have the orb. We're doomed. Do you need me to show you through the maze?" else "The orb, they have the orb. We're doomed. Do you need me to show you back to the village?")
                escortChoice(inside, "Please help us get our orb back.")
            }
        }
    }

    private suspend fun Player.escortChoice(inside: Boolean, text: String, no: String = "Not now, thanks.") {
        choice {
            option<Neutral>("Yes please.") { gnomeEscort(inside, text) }
            option<Neutral>(no) {
                if (no == "No thanks Elkoy.") npc<Neutral>(ELKOY, "Ok then, take care.")
            }
        }
    }

    private suspend fun Player.remsai() {
        val id = "remsai_tree_gnome_village"
        when {
            questStage("tree_gnome_village") >= 8 -> {
                player<Neutral>("I've returned.")
                npc<Neutral>(id, "You're back, well done brave adventurer. Now the orbs are safe we can perform the ritual for the spirit tree. We can live in peace once again.")
            }
            questStage("tree_gnome_village") >= 6 -> {
                player<Neutral>("Are you ok?")
                npc<Neutral>(id, "Khazard's men came. Without the orb we were defenceless. They killed many and then took our last hope, the other orbs.")
                npc<Neutral>(id, "Now surely we're all doomed. Without them the spirit tree is useless.")
            }
            questStage("tree_gnome_village") == 5 -> {
                player<Neutral>("Hello Remsai.")
                npc<Neutral>(id, "Hello, did you find the orb?")
                if (inventory.contains("orb_of_protection")) {
                    player<Neutral>("I have it here.")
                    npc<Neutral>(id, "You're our saviour.")
                } else {
                    player<Neutral>("No, I'm afraid not.")
                    npc<Neutral>(id, "Please, we must have the orb if we are to survive.")
                }
            }
            else -> {
                npc<Neutral>(id, "Oh my, oh my!")
                player<Neutral>("What's wrong?")
                npc<Neutral>(id, "The orb, they have the orb. It must be returned or we're doomed.")
            }
        }
    }
}
