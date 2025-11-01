package content.area.asgarnia.falador

import content.entity.combat.hit.damage
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.onBarCrawl
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

class BarmaidsRisingSunInn : Script {

    val barCrawl: suspend ItemOnNPC.() -> Unit = {
        if (!player.containsVarbit("barcrawl_signatures", "hand_of_death_cocktail")) {
            player.barCrawl(target)
        }
    }
    val tip: suspend ItemOnNPC.() -> Unit = {
        player.inventory.remove("coins", 1)
        npc<Happy>("Thanks!")
    }

    val emptyGlass: suspend ItemOnNPC.() -> Unit = {
        player.interactNpc(target, "Talk-to")
    }

    val itemDefinitions: ItemDefinitions by inject()

    init {
        npcApproach("Talk-to", "barmaid_emily") { (target) ->
            approachRange(3)
            menu(target)
        }

        npcOperate("Talk-to", "barmaid_kaylee,barmaid_tina") { (target) ->
            menu(target)
        }

        itemOnNPCApproach("barcrawl_card", "barmaid_emily", handler = barCrawl)

        itemOnNPCOperate("barcrawl_card", "barmaid_kaylee", handler = barCrawl)

        itemOnNPCOperate("barcrawl_card", "barmaid_tina", handler = barCrawl)

        itemOnNPCApproach("coins", "barmaid_emily", handler = tip)

        itemOnNPCApproach("coins", "barmaid_kaylee", handler = tip)

        itemOnNPCApproach("coins", "barmaid_tina", handler = tip)

        itemOnNPCApproach("beer_glass", "barmaid_emily", handler = emptyGlass)

        itemOnNPCApproach("beer_glass", "barmaid_kaylee", handler = emptyGlass)

        itemOnNPCApproach("beer_glass", "barmaid_tina", handler = emptyGlass)
    }

    suspend fun Player.menu(target: NPC) {
        npc<Quiz>("Heya! What can I get you?")
        choice {
            option<Quiz>("What ales are you serving?") {
                npc<Talk>("Well, we've got Asgarnian Ale, Wizard's Mind Bomb and Dwarven Stout, all for only 3 coins.")
                choice {
                    option<Talk>("One Asgarnian Ale, please.") {
                        buyBeer("asgarnian_ale")
                    }
                    option<Talk>("I'll try the Mind Bomb.") {
                        buyBeer("wizards_mind_bomb")
                    }
                    option<Talk>("Can I have a Dwarven Stout?") {
                        buyBeer("dwarven_stout")
                    }
                    option<Talk>("I don't feel like any of those.")
                }
            }
            if (onBarCrawl(target)) {
                option("I'm doing Alfred Grimhand's barcrawl.") {
                    barCrawl(target)
                }
            }
            when (inventory.count("beer_glass")) {
                0 -> {}
                1 -> option<Talk>("I've got this beer glass...") {
                    npc<Quiz>("We'll buy it for a couple of coins if you're interested.")
                    buyEmptyGlasses()
                }
                else -> option<Talk>("I've got these beer glasses...") {
                    npc<Quiz>("Ooh, we'll buy those off you if you're interested. 2 coins per glass.")
                    buyEmptyGlasses()
                }
            }
        }
    }

    suspend fun Player.buyBeer(beer: String) {
        inventory.transaction {
            remove("coin", 3)
            add(beer)
        }
        when (inventory.transaction.error) {
            is TransactionError.Deficient -> {
                npc<Angry>("I said 3 coins! You haven't got 3 coins!")
                player<Sad>("Sorry, I'll come back another day.")
            }
            is TransactionError.Full -> inventoryFull()
            TransactionError.None -> {
                message("You buy a ${itemDefinitions.get(beer).name}.")
                player<Talk>("Thanks, Emily.")
            }
            else -> {}
        }
    }

    suspend fun Player.buyEmptyGlasses() {
        choice {
            option<Talk>("Okay, sure.") {
                inventory.transaction {
                    val removed = removeToLimit("beer_glass", 28)
                    add("coins", 2 * removed)
                }
                npc<Happy>("There you go.")
                player<Talk>("Thanks!")
            }
            option<Shifty>("No thanks, I like empty beer glasses.")
        }
    }

    suspend fun Player.barCrawl(target: NPC) = barCrawlDrink(
        target,
        start = {
            npc<Laugh>("Heehee, this'll be fun!")
            npc<Angry>("You'll be after our Hand of Death cocktail, then. Lots of expensive parts to the cocktail, though, so it will cost you 70 coins.")
        },
        effects = {
            levels.drain(Skill.Ranged, 6)
            levels.drain(Skill.Defence, 6)
            levels.drain(Skill.Fishing, 6)
            levels.drain(Skill.Attack, 7)
            //        player.shakeCamera() TODO camera shake
            //        player.softQueue("clear_shake", random.nextInt(2, 5)) {
            //            player.clearCamera()
            //        }
            damage(10)
        },
    )
}
