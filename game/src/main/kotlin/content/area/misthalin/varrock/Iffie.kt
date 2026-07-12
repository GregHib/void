package content.area.misthalin.varrock

import content.entity.player.bank.ownsItem
import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.inv.sendInventory

/**
 * Iffie's costume store: costume points saved up from random event gifts are spent here on the
 * random event costumes, one piece at a time. The "Cash in your points" interface (201) draws
 * the five costume rows itself and shows the player's balance from the costume_points varbits;
 * clicking a piece claims it for a point - the outfit's own event points spend before the
 * any-outfit gift points.
 */
class Iffie : Script {

    init {
        npcOperate("Talk-to", "iffie") {
            npc<Happy>("Hello, dearie! Were you wanting to collect a random event costume, or is there something else I can do for you today?")
            choice {
                option("I've come for a random event costume.") {
                    val generic = get("costume_points", 0)
                    val total = generic + COSTUMES.sumOf { get("${it.name}_costume_points", 0) }
                    if (total == 0) {
                        npc<Neutral>("Then you'll be needing a costume point, dearie. Complete a costume's random event, or pick 'Save up for a costume!' from a random event gift, and come back to see me.")
                        return@option
                    }
                    npc<Happy>("Some of these costumes even come with a free emote!")
                    npc<Happy>("You have $generic point${if (generic == 1) "" else "s"} to spend on any outfit; points earned from an outfit's own event show on its row. Each piece costs one point.")
                    openCostumeStore()
                }
                option<Quiz>("Aren't you selling anything?") {
                    npc<Laugh>("Oh, yes, but only costumes. Thessalia sells some other clothes and runs the makeover service.")
                }
                option<Neutral>("I just came for a chat.") {
                    npc<Disheartened>("Oh, I'm sorry, but I'll never get my knitting done if I stop for a chit-chat with every young ${if (male) "lad" else "lass"} who wanders through the shop!")
                }
            }
        }

        npcOperate("Claim-costume", "iffie") {
            openCostumeStore()
        }

        interfaceOpened("costume_reward_select") { id ->
            interfaceOptions.unlockAll(id, "rewards", 0 until ROWS * ROW_STRIDE)
        }

        // Each piece child carries its item (from enum 2240), so the click names the exact piece
        interfaceOption("Claim", "costume_reward_select:rewards") {
            claim(it.item.id)
        }
    }

    private fun Player.openCostumeStore() {
        syncPoints()
        // The interface dims owned pieces by checking the bank container client-side, which the
        // client only knows about once it's been sent
        sendInventory("bank")
        open("costume_reward_select")
    }

    /**
     * The interface reads each row's "Points:" from its own varbit - mirror the balance to all -
     * and greys out a costume once every piece is owned. The frog mask greys separately from the
     * rest of the royal outfit.
     */
    private fun Player.syncPoints() {
        val generic = get("costume_points", 0)
        for (costume in COSTUMES) {
            set("costume_points_${costume.name}", generic + get("${costume.name}_costume_points", 0))
            val pieces = if (costume.name == "frog") costume.pieces - "frog_mask" else costume.pieces
            set("costume_claimed_${costume.name}", pieces.all { ownsItem(it) })
        }
        set("costume_claimed_frog_mask", ownsItem("frog_mask"))
    }

    private fun Player.claim(piece: String) {
        val costume = COSTUMES.firstOrNull { piece in it.pieces } ?: return
        if (ownsItem(piece)) {
            message("You already have that piece, dearie.")
            return
        }
        // Points from the outfit's own event spend first, saving the any-outfit gift points
        val locked = get("${costume.name}_costume_points", 0)
        val generic = get("costume_points", 0)
        when {
            locked > 0 -> set("${costume.name}_costume_points", locked - 1)
            generic > 0 -> set("costume_points", generic - 1)
            else -> {
                message("You don't have any points for that outfit; complete its random event or pick 'Save up for a costume!' from a random event gift.")
                return
            }
        }
        addOrDrop(piece)
        syncPoints()
        message("You spend a costume point on the ${piece.replace('_', ' ')}.")
    }

    private data class Costume(val name: String, val pieces: List<String>)

    companion object {
        private const val ROWS = 5
        private const val ROW_STRIDE = 10

        // In the interface's row order.
        private val COSTUMES = listOf(
            Costume("mime", listOf("mime_mask", "mime_top", "mime_legs", "mime_gloves", "mime_boots")),
            Costume("frog", listOf("frog_mask", "princess_blouse", "princess_skirt", "prince_tunic", "prince_leggings")),
            Costume("zombie", listOf("zombie_shirt", "zombie_trousers", "zombie_gloves", "zombie_boots", "zombie_mask")),
            Costume("camo", listOf("camo_top", "camo_bottoms", "camo_helmet")),
            Costume("lederhosen", listOf("lederhosen_top", "lederhosen_hat", "lederhosen_shorts")),
        )
    }
}
