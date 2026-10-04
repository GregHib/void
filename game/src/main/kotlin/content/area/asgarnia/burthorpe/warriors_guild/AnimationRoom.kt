package content.area.asgarnia.burthorpe.warriors_guild

import content.entity.combat.killer
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.clearHint
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.markHint
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.closeDialogue
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.network.login.protocol.encode.HintType

/**
 * Place a full helm, platebody and platelegs on the magical animator to fight them for tokens.
 * https://runescape.wiki/w/Warriors%27_Guild#Animation_Room
 */
class AnimationRoom : Script {

    init {
        itemOnObjectOperate(obj = "magical_animator") { (target, item) ->
            val metal = item.id.substringBefore("_")
            val npc = Tables.npcOrNull("warriors_guild_animated_armour.$metal.npc")
            if (npc == null || item.id !in pieces(metal)) {
                message("Nothing interesting happens.")
                return@itemOnObjectOperate
            }
            if (contains("warriors_guild_animated_armour")) {
                message("You already have a set animated.") // TODO proper message
                return@itemOnObjectOperate
            }
            val armour = pieces(metal)
            if (armour.any { !inventory.contains(it) }) {
                statement("You need a plate body, plate legs and full helm of the same type to activate the armour animator.")
                return@itemOnObjectOperate
            }
            anim("human_pickupfloor")
            statement("You place your armour on the platform where it disappears...", clickToContinue = false)
            delay(4)
            if (!inventory.remove(*armour.toTypedArray())) {
                closeDialogue()
                return@itemOnObjectOperate
            }
            set("warriors_guild_animated_armour", metal)
            statement("The animator hums, something appears to be working. You stand back...", clickToContinue = false)
            delay(4)
            closeDialogue()
            val animated = NPCs.add(npc, target.tile, ticks = -1, owner = this)
            animated.anim("animated_armour_rise")
            animated.say("I'm ALIVE!")
            animated.interactPlayer(this, "Attack")
            this["warriors_guild_animated_armour_index"] = animated.index
            markHint(animated)
        }

        npcDeath("animated_*_armour") {
            val player = killer as? Player ?: return@npcDeath
            player.clear("warriors_guild_animated_armour")
            player.clear("warriors_guild_animated_armour_index")
            player.clearHint(HintType.NPC)
        }

        exited("warriors_guild_animation_room") {
            reclaimArmour()
        }

        playerDespawn {
            reclaimArmour()
        }

        playerSpawn {
            reclaimArmour()
        }
    }

    /**
     * Removes the players animated armour (if any) and returns the pieces to them
     */
    private fun Player.reclaimArmour() {
        val metal: String = get("warriors_guild_animated_armour") ?: return
        val index: Int? = remove("warriors_guild_animated_armour_index")
        if (index != null) {
            val npc = NPCs.indexed(index)
            if (npc != null && npc.id == Tables.npc("warriors_guild_animated_armour.$metal.npc")) {
                NPCs.remove(npc)
            }
        }
        clear("warriors_guild_animated_armour")
        clearHint(HintType.NPC)
        for (piece in pieces(metal)) {
            if (!inventory.add(piece)) {
                FloorItems.add(tile, piece, owner = this)
            }
        }
    }

    companion object {
        fun pieces(metal: String) = listOf("${metal}_full_helm", "${metal}_platebody", "${metal}_platelegs")
    }
}
