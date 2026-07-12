package content.skill.summoning

import content.entity.combat.target
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject

/**
 * Registry + dispatcher for the scroll-driven familiar special moves triggered from the cast button
 * on the `familiar_details` interface.
 *
 * The interface defines a distinct `cast_<move>` component per move, but several familiars share a
 * move name (Bull Rush x6, Petrifying Gaze x7, Titan's Constitution x3, Call to Arms x4), so moves
 * are keyed on the **follower's npc id** rather than the component - the follower unambiguously
 * identifies the familiar and therefore its special. The four maps mirror the engine's interaction
 * kinds; a familiar registers in exactly the one matching how its special is triggered:
 *  - [instant]         : self/AoE specials sent as a plain interface option
 *  - [npcTarget]       : combat specials where the player then clicks an npc
 *  - [playerTarget]    : combat specials where the player then clicks another player
 *  - [objectTarget]    : specials where the player then clicks scenery (chop a tree, fill a bin, ...)
 *  - [itemTarget]      : specials where the player uses the cast button on an inventory item
 *  - [floorItemTarget] : specials where the player then clicks a ground item (phoenix's ashes)
 *
 * Item-target specials (a fish/egg/bar) can also be triggered by using the item on the familiar,
 * wired separately with `itemOnNPCOperate` in their own scripts (operate, not approach, so their
 * exact item:npc key beats the beast-of-burden store's wildcard). Both surfaces run the same block.
 *
 * Each block returns whether the move actually happened so [castFamiliarSpecial] only spends a
 * scroll + points on a real cast.
 */
object FamiliarSpecialMoves : AutoCloseable {
    val instant = HashMap<String, Player.() -> Boolean>()
    val npcTarget = HashMap<String, Player.(NPC) -> Boolean>()
    val playerTarget = HashMap<String, Player.(Player) -> Boolean>()
    val objectTarget = HashMap<String, Player.(GameObject) -> Boolean>()
    val itemTarget = HashMap<String, Player.(Item) -> Boolean>()
    val floorItemTarget = HashMap<String, Player.(FloorItem) -> Boolean>()

    fun instant(vararg familiars: String, block: Player.() -> Boolean) {
        for (familiar in familiars) instant[familiar] = block
    }

    fun npc(vararg familiars: String, block: Player.(NPC) -> Boolean) {
        for (familiar in familiars) npcTarget[familiar] = block
    }

    fun player(vararg familiars: String, block: Player.(Player) -> Boolean) {
        for (familiar in familiars) playerTarget[familiar] = block
    }

    fun obj(vararg familiars: String, block: Player.(GameObject) -> Boolean) {
        for (familiar in familiars) objectTarget[familiar] = block
    }

    fun item(vararg familiars: String, block: Player.(Item) -> Boolean) {
        for (familiar in familiars) itemTarget[familiar] = block
    }

    fun floorItem(vararg familiars: String, block: Player.(FloorItem) -> Boolean) {
        for (familiar in familiars) floorItemTarget[familiar] = block
    }

    override fun close() {
        instant.clear()
        npcTarget.clear()
        playerTarget.clear()
        objectTarget.clear()
        itemTarget.clear()
        floorItemTarget.clear()
    }
}

/**
 * Single dispatcher routing every special-move cast to the move registered for the current follower.
 * The move can be cast from two surfaces - the follower-details tab's cast button
 * (`familiar_details:cast_*`) and the minimap summoning orb's "Cast <special>" right/left-click option
 * (`summoning_orb:*cast`) - so both are wired. Combat/scenery casts come through the approach hooks
 * (the player picks a world target after clicking the button); self/AoE casts come through the plain
 * interface option.
 */
class FamiliarSpecialMovesDispatch : Script {
    init {
        // Follower-details tab cast button.
        interfaceOption("*", "familiar_details:cast_*") {
            castButton()
        }

        onNPCApproach("familiar_details:cast_*", "*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onNPCApproach
            val block = FamiliarSpecialMoves.npcTarget[id] ?: return@onNPCApproach
            castFamiliarSpecial { block(target) }
        }

        onPlayerApproach("familiar_details:cast_*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onPlayerApproach
            val block = FamiliarSpecialMoves.playerTarget[id] ?: return@onPlayerApproach
            castFamiliarSpecial { block(target) }
        }

        onObjectApproach("familiar_details:cast_*", "*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onObjectApproach
            val block = FamiliarSpecialMoves.objectTarget[id] ?: return@onObjectApproach
            castFamiliarSpecial { block(target) }
        }

        // Cast button used on an inventory item (Winter Storage, Immense Heat, ...).
        onItem("familiar_details:cast_*") { item, _ ->
            val id = follower?.id ?: return@onItem
            val block = FamiliarSpecialMoves.itemTarget[id] ?: return@onItem
            castFamiliarSpecial { block(item) }
        }

        // Cast button used on a ground item (Rise from the Ashes' ashes).
        onFloorItemApproach("familiar_details:cast_*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onFloorItemApproach
            val block = FamiliarSpecialMoves.floorItemTarget[id] ?: return@onFloorItemApproach
            castFamiliarSpecial { block(target) }
        }

        // Minimap summoning orb "Cast <special>" option - one `cast_<special>` component per move.
        interfaceOption("*", "summoning_orb:cast_*") {
            castButton()
        }

        onNPCApproach("summoning_orb:cast_*", "*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onNPCApproach
            val block = FamiliarSpecialMoves.npcTarget[id] ?: return@onNPCApproach
            castFamiliarSpecial { block(target) }
        }

        onPlayerApproach("summoning_orb:cast_*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onPlayerApproach
            val block = FamiliarSpecialMoves.playerTarget[id] ?: return@onPlayerApproach
            castFamiliarSpecial { block(target) }
        }

        onObjectApproach("summoning_orb:cast_*", "*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onObjectApproach
            val block = FamiliarSpecialMoves.objectTarget[id] ?: return@onObjectApproach
            castFamiliarSpecial { block(target) }
        }

        onItem("summoning_orb:cast_*") { item, _ ->
            val id = follower?.id ?: return@onItem
            val block = FamiliarSpecialMoves.itemTarget[id] ?: return@onItem
            castFamiliarSpecial { block(item) }
        }

        onFloorItemApproach("summoning_orb:cast_*") { (target) ->
            approachRange(16, update = false)
            val id = follower?.id ?: return@onFloorItemApproach
            val block = FamiliarSpecialMoves.floorItemTarget[id] ?: return@onFloorItemApproach
            castFamiliarSpecial { block(target) }
        }
    }

    /**
     * A plain click on the cast button: instant specials fire directly; combat specials auto-fire
     * at the familiar's current foe (or the owner's), as in the live game - clicking a target
     * after the button still works through the approach hooks.
     */
    private fun Player.castButton() {
        val id = follower?.id ?: return
        val instant = FamiliarSpecialMoves.instant[id]
        if (instant != null) {
            castFamiliarSpecial { instant() }
            return
        }
        val combat = FamiliarSpecialMoves.npcTarget[id] ?: return
        val enemy = (follower?.target ?: target) as? NPC
        if (enemy == null) {
            message("Your familiar has no target to attack.")
            return
        }
        castFamiliarSpecial { combat(enemy) }
    }
}
