package content.area.asgarnia.falador

import content.entity.combat.hit.damage
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.miniquest.alfred_grimhands_barcrawl.barCrawlDrink
import content.quest.miniquest.alfred_grimhands_barcrawl.doingBarCrawl
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.instruction.handle.interactNpc
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.dialogue.Dialogue
import world.gregs.voidps.engine.client.ui.dialogue.talkWith
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.Approach
import world.gregs.voidps.engine.entity.Id
import world.gregs.voidps.engine.entity.ItemOn
import world.gregs.voidps.engine.entity.character.mode.interact.approachRange
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit

@Script
class BarmaidsRisingSunInn : Api {
    val itemDefinitions: ItemDefinitions by inject()

    @ItemOn("barcrawl_card,coins,beer_glass", "barmaid_emily,barmaid_kaylee,barmaid_tina")
    override suspend fun operate(player: Player, id: String, item: Item, slot: Int, target: NPC) {
        when (item.id) {
            "barcrawl_card" -> if (!player.containsVarbit("barcrawl_signatures", "hand_of_death_cocktail")) {
                player.talkWith(target) {
                    barCrawl()
                }
            }
            "coins" -> {
                player.inventory.remove("coins", 1)
                player.talkWith(target) {
                    npc<Happy>("Thanks!")
                }
            }
            "beer_glass" -> player.interactNpc(target, "Talk-to")
        }
    }

    @Id("barmaid_kaylee,barmaid_tina")
    override suspend fun Dialogue.talk(player: Player, target: NPC) {
        menu()
    }

    @Approach("Talk-to", "barmaid_emily")
    override suspend fun approach(player: Player, target: NPC, option: String) {
        player.approachRange(3)
        player.talkWith(target) {
            menu()
        }
    }

    suspend fun Dialogue.menu() {
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
            if (doingBarCrawl()) {
                option("I'm doing Alfred Grimhand's barcrawl.") {
                    barCrawl()
                }
            }
            when (player.inventory.count("beer_glass")) {
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

    suspend fun Dialogue.buyBeer(beer: String) {
        player.inventory.transaction {
            remove("coin", 3)
            add(beer)
        }
        when (player.inventory.transaction.error) {
            is TransactionError.Deficient -> {
                npc<Angry>("I said 3 coins! You haven't got 3 coins!")
                player<Sad>("Sorry, I'll come back another day.")
            }
            is TransactionError.Full -> player.inventoryFull()
            TransactionError.None -> {
                player.message("You buy a ${itemDefinitions.get(beer).name}.")
                player<Talk>("Thanks, Emily.")
            }
            else -> {}
        }
    }

    suspend fun Dialogue.buyEmptyGlasses() {
        choice {
            option<Talk>("Okay, sure.") {
                player.inventory.transaction {
                    val removed = removeToLimit("beer_glass", 28)
                    add("coins", 2 * removed)
                }
                npc<Happy>("There you go.")
                player<Talk>("Thanks!")
            }
            option<Shifty>("No thanks, I like empty beer glasses.")
        }
    }

    suspend fun Dialogue.barCrawl() = barCrawlDrink(
        start = {
            npc<Laugh>("Heehee, this'll be fun!")
            npc<Angry>("You'll be after our Hand of Death cocktail, then. Lots of expensive parts to the cocktail, though, so it will cost you 70 coins.")
        },
        effects = {
            player.levels.drain(Skill.Ranged, 6)
            player.levels.drain(Skill.Defence, 6)
            player.levels.drain(Skill.Fishing, 6)
            player.levels.drain(Skill.Attack, 7)
            //        player.shakeCamera() TODO camera shake
            //        player.softQueue("clear_shake", random.nextInt(2, 5)) {
            //            player.clearCamera()
            //        }
            player.damage(10)
        },
    )
}
