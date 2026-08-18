package content.area.asgarnia.falador

import content.achievement.Tasks
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.intEntry
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.*
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

/**
 * Wyson's conversation tree. Source: https://runescape.wiki/w/Transcript:Wyson_the_gardener
 */
class WysonTheGardener : Script {

    init {
        npcOperate("Talk-to", "wyson_the_gardener") {
            npc<Idle>("I'm the head gardener around here. If you're looking for woad leaves, or if you need help with owt, I'm yer man.")
            menu()
        }
    }

    suspend fun Player.menu() {
        choice("What would you like to talk about?") {
            option("Ask about the unusual flower bed.") {
                flowerBed()
            }
            option("Ask about trading for bird nests.") {
                exchangeMoleBits()
            }
            option("Ask about woad leaves.") {
                woadLeaves()
            }
            option("Ask for advice for fighting the Giant Mole.") {
                player<Quiz>("Could you give me some advice for fighting the Giant Mole?")
                npc<Idle>("Alright. I'll give you some tips while you're down there.")
            }
            option("Leave.") {
                leave()
            }
        }
    }

    suspend fun Player.leave() {
        player<Idle>("Sorry, but I'm not interested.")
        npc<Disheartened>("Fair enough.")
    }

    suspend fun Player.flowerBed() {
        player<Quiz>("What's up with the strange flower bed?")
        npc<Idle>("Oh, the new lilies? Yes, we've just started...")
        player<Idle>("No, I meant the distorted plants whose exposed roots reach down into darkness unknowable.")
        npc<Shock>("Ah.")
        npc<Disheartened>("I was hoping it wasn't that noticeable.")
        npc<Idle>("I just wanted to make sure Falador had the best park in Gielinor, but I got carried away with some of 'Malignius Mortifer's Super-Ultra-Flora-Growth potion'.")
        npc<Disheartened>("To make matters worse, it's affected one of the local moles and she keeps ruining my flowerbeds.")
        player<Quiz>("Is there anything I can do to help?")
        npc<Idle>("Maybe someone could squeeze down there and deal with her.")
        npc<Idle>("Could you do it? I can offer you some bird nests in exchange for any claws or hides you find.")
        choice("Select an Option") {
            option("Okay.") {
                agreeToHelp()
            }
            option<Quiz>("Why are you even collecting mole claws?") {
                npc<Idle>("The wife loves them.")
                choice("Select an Option") {
                    option("... Okay.") {
                        agreeToHelp()
                    }
                    option("Leave") {
                        leave()
                    }
                }
            }
            option("Leave.") {
                leave()
            }
        }
    }

    suspend fun Player.agreeToHelp() {
        player<Idle>("Okay, I'll put a stop to it.")
        npc<Happy>("Thank you, adventurer.")
    }

    /*
        Woad leaves
     */

    suspend fun Player.woadLeaves() {
        player<Idle>("Yes please, I need woad leaves.")
        npc<Shifty>("How much are you willing to pay?")
        choice("What would you like to say?") {
            option<Idle>("How about 5 coins?") {
                tooLittle()
            }
            option<Idle>("How about 10 coins?") {
                tooLittle()
            }
            option<Idle>("How about 15 coins?") {
                buyWoadLeaf()
            }
            option<Idle>("How about 20 coins?") {
                buyWoadLeaves()
            }
            option<Idle>("Tell me your price.") {
                haggle()
            }
        }
    }

    suspend fun Player.tooLittle() {
        npc<Angry>("No no, that's far too little. Woad leaves are hard to get. I used to have plenty but someone kept stealing them off me.")
    }

    suspend fun Player.buyWoadLeaf() {
        npc<Idle>("Mmmm... okay, that sounds fair.")
        if (inventory.remove("coins", 15)) {
            addOrDrop("woad_leaf")
            player<Happy>("Thanks.")
            item("woad_leaf", "You buy a woad leaf from Wyson.")
            npc<Idle>("I'll be around if you have any more gardening needs.")
        } else {
            player<Disheartened>("I don't have enough coins to buy the leaves. I'll come back later.")
        }
    }

    suspend fun Player.buyWoadLeaves() {
        npc<Happy>("Ok that's more than fair.")
        npc<Happy>("Here, have two. You're a generous person.")
        if (inventory.remove("coins", 20)) {
            addOrDrop("woad_leaf", 2)
            player<Happy>("Thanks.")
            item("woad_leaf", "Wyson gives you a pair of woad leaves.")
        } else {
            player<Disheartened>("I don't have enough coins to buy the leaves. I'll come back later.")
        }
    }

    suspend fun Player.haggle() {
        npc<Frustrated>("Hmph. The art of haggling really is lost these days. Fine. $HAGGLED_PRICE coins a piece.")
        val requested = intEntry("How many would you like? ($HAGGLED_PRICE coins each)")
        if (requested <= 0) {
            return
        }
        val affordable = minOf(requested, inventory.count("coins") / HAGGLED_PRICE)
        if (affordable <= 0) {
            player<Disheartened>("I don't have enough coins to buy the leaves. I'll come back later.")
            return
        }
        var bought = 0
        while (bought < affordable &&
            inventory.transaction {
                remove("coins", HAGGLED_PRICE)
                add("woad_leaf")
            }
        ) {
            bought++
        }
        if (bought == 0) {
            message("You don't have enough inventory space.")
            return
        }
        player<Happy>("Thanks.")
        message("Wyson sells you: $bought x Woad leaf")
    }

    /*
        Mole part exchange
     */

    suspend fun Player.exchangeMoleBits() {
        val held = MOLE_PARTS.filter { inventory.contains(it) }
        if (held.isEmpty()) {
            npc<Idle>("If you get any mole claws, skins or noses from that giant beastie down there, I'll happily exchange them for some bird nests.")
            player<Idle>("Okay, I'll come back if I find any.")
            return
        }
        if (!World.members) {
            message("You need to be on a members' world to use this feature.")
            return
        }
        val seeds = faladorHardTasksComplete(this)
        if (seeds) {
            npc<Happy>("If I'm not mistaken, you've got some mole bits there! I'll trade them for bird nests, if you like. I also see that you're a shield holder; a champion of Falador, eh? I'll throw some special seeds into the bargain.")
        } else {
            npc<Happy>("If I'm not mistaken, you've got some bits from a big mole there! I'll trade them for bird nests, if you like.")
        }
        choice("Choose an option:") {
            if (held.contains("mole_claw")) {
                option<Idle>("Yes, I will trade the mole claws.") {
                    trade(listOf("mole_claw"), seeds)
                }
            }
            if (held.contains("mole_skin")) {
                option<Idle>("Okay, I will trade the mole skin.") {
                    trade(listOf("mole_skin"), seeds)
                }
            }
            if (held.contains("mole_nose")) {
                option<Idle>("Can I trade the mole nose?") {
                    trade(listOf("mole_nose"), seeds)
                }
            }
            if (held.size == 2) {
                option<Idle>("I'd like to trade both.") {
                    trade(held, seeds)
                }
            }
            if (held.size == 3) {
                option<Idle>("I'd like to trade all three.") {
                    trade(held, seeds)
                }
            }
            option("Actually, I've changed my mind.") {
                menu()
            }
        }
    }

    /**
     * Swaps each of the [parts] for a bird nest, one at a time so a full inventory only stops the
     * trade rather than cancelling it, plus a white lily seed per mole skin when [seeds] is true.
     */
    suspend fun Player.trade(parts: List<String>, seeds: Boolean) {
        var traded = 0
        var lilies = 0
        for (part in parts) {
            val nest = if (part == "mole_nose") "birds_nest_seeds_1" else "birds_nest_seeds_2"
            val lily = seeds && part == "mole_skin"
            var remaining = inventory.count(part)
            while (remaining > 0 &&
                inventory.transaction {
                    remove(part)
                    add(nest)
                    if (lily) {
                        add("white_lily_seed")
                    }
                }
            ) {
                remaining--
                traded++
                if (lily) {
                    lilies++
                }
            }
            if (remaining > 0) {
                break
            }
        }
        if (traded == 0) {
            message("You don't have enough inventory space.")
            return
        }
        if (lilies > 0) {
            npc<Idle>("If you don't plan on using those seeds, some of the other gardeners may be interested in them.")
        }
    }

    companion object {
        private const val HAGGLED_PRICE = 25
        private const val FALADOR_TASK_AREA = 3
        private const val HARD_TASK_DIFFICULTY = 4
        private val MOLE_PARTS = listOf("mole_claw", "mole_skin", "mole_nose")

        fun faladorHardTasksComplete(player: Player): Boolean = Tasks.forEach(FALADOR_TASK_AREA) {
            if (definition["task_difficulty", 0] == HARD_TASK_DIFFICULTY && !Tasks.isCompleted(player, definition.stringId)) {
                return@forEach false
            }
            null
        } ?: true
    }
}
