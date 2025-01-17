package world.gregs.voidps.world.map.falador

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.ItemOnNPC
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCApproach
import world.gregs.voidps.engine.client.ui.interact.itemOnNPCOperate
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.TargetInteraction
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.npc.npcApproach
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.engine.suspend.approachRange
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.world.activity.quest.mini.barCrawlDrink
import world.gregs.voidps.world.activity.quest.mini.barCrawlFilter
import world.gregs.voidps.world.interact.dialogue.*
import world.gregs.voidps.world.interact.dialogue.type.choice
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.dialogue.type.player
import world.gregs.voidps.world.interact.entity.combat.hit.damage

// Dialogue

npcApproach("Talk-to", "barmaid_emily") {
    player.approachRange(3)
    pause()
    menu()
}

npcOperate("Talk-to", "barmaid_kaylee", "barmaid_tina") {
    menu()
}

// Bar crawl

val barCrawl: suspend ItemOnNPC.() -> Unit = {
    if (player.containsVarbit("barcrawl_signatures", "hand_of_death_cocktail")) {
        player.noInterest() // TODO proper message
    } else {
        barCrawl()
    }
}
itemOnNPCApproach("barcrawl_card", "barmaid_emily", handler = barCrawl)
itemOnNPCOperate("barcrawl_card", "barmaid_kaylee", handler = barCrawl)
itemOnNPCOperate("barcrawl_card", "barmaid_tina", handler = barCrawl)

val tip: suspend ItemOnNPC.() -> Unit = {
    player.inventory.remove("coins", 1)
    npc<Happy>("Thanks!")
}

// Misc
itemOnNPCApproach("coins", "barmaid_emily", handler = tip)
itemOnNPCApproach("coins", "barmaid_kaylee", handler = tip)
itemOnNPCApproach("coins", "barmaid_tina", handler = tip)

val emptyGlass: suspend ItemOnNPC.() -> Unit = {
    player.mode = Interact(player, target, NPCOption(player, target, target.def, "Talk-to"))
}

itemOnNPCApproach("beer_glass", "barmaid_emily", handler = emptyGlass)
itemOnNPCApproach("beer_glass", "barmaid_kaylee", handler = emptyGlass)
itemOnNPCApproach("beer_glass", "barmaid_tina", handler = emptyGlass)

suspend fun NPCOption<Player>.menu() {
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
        option("I'm doing Alfred Grimhand's barcrawl.", filter = barCrawlFilter) {
            barCrawl()
        }
        option<Talk>("I've got this beer glass...", filter = { player.inventory.contains("beer_glass", 1) }) {
            npc<Quiz>("We'll buy it for a couple of coins if you're interested.")
            buyEmptyGlasses()
        }
        option<Talk>("I've got these beer glasses...", filter = { player.inventory.count("beer_glass") > 1 }) {
            npc<Quiz>("Ooh, we'll buy those off you if you're interested. 2 coins per glass.")
            buyEmptyGlasses()
        }
    }
}

val itemDefinitions: ItemDefinitions by inject()

suspend fun NPCOption<Player>.buyBeer(beer: String) {
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

suspend fun NPCOption<Player>.buyEmptyGlasses() {
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

suspend fun TargetInteraction<Player, NPC>.barCrawl() = barCrawlDrink(
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
    }
)