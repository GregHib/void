package content.skill.summoning.familiar

import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.fletching.fletchLogDialog
import content.skill.summoning.FAMILIAR_BUSY_MESSAGE
import content.skill.summoning.FAMILIAR_CHOPPING
import content.skill.summoning.FamiliarSpecialMoves
import content.skill.summoning.beastOfBurdenCapacity
import content.skill.summoning.ensureBeastOfBurdenInventory
import content.skill.summoning.follower
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.move.Movement
import world.gregs.voidps.engine.entity.character.mode.move.target.TargetStrategy
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.random

/**
 * Logs the beaver's Multichop can hand over, lowest tier first. On a cast it may give any of these at
 * or below the targeted tree's tier - so the player can end up with a lower-level log than intended.
 */
private val MULTICHOP_LOGS = listOf("logs", "oak_logs", "willow_logs", "maple_logs", "yew_logs", "magic_logs")

/** How long the beaver chops the tree (ticks), and how often its chop animation is replayed. */
private const val CHOP_TICKS = 10
private const val CHOP_ANIM_INTERVAL = 4

private fun Player.beastOfBurdenFull(): Boolean {
    ensureBeastOfBurdenInventory()
    return beastOfBurden.items.count { it.isNotEmpty() } >= beastOfBurdenCapacity
}

class Beaver : Script {
    init {
        // Multichop - the beaver chops the targeted tree and hands over a log, possibly from a lower-
        // level tree than the one aimed at. Object-target special, so it runs through the scroll +
        // points gate; returns false (charging nothing) when it can't produce a log.
        FamiliarSpecialMoves.obj("beaver_familiar") { tree ->
            val treeLog = Tables.itemOrNull("trees.${tree.def(this).stringId}.logs")
            if (treeLog == null) {
                message("Your beaver can only chop naturally growing trees.")
                return@obj false
            }
            val beaver = follower ?: return@obj false
            if (beastOfBurdenFull()) {
                message("Your beaver's pack is too full to store any more logs.")
                return@obj false
            }
            val targetLevel = Rows.getOrNull("logs.$treeLog")?.int("level") ?: 0
            val candidates = MULTICHOP_LOGS.filter { (Rows.getOrNull("logs.$it")?.int("level") ?: 0) <= targetLevel }
            val log = if (candidates.isEmpty()) treeLog else candidates[random.nextInt(candidates.size)]
            val owner = this
            // Send the beaver over to the tree first, then have it chop and stash the log in its pack.
            // Stop tracking the owner up front so the beaver doesn't keep facing the player.
            beaver[FAMILIAR_CHOPPING] = true
            beaver.clearWatch()
            beaver.mode = Movement(beaver, TargetStrategy(beaver, tree))
            beaver.queue("beaver_multichop") {
                // Wait until it's actually next to the tree (the strict reach check can stay false when
                // adjacent to a blocked object, which would leave it idling and facing the owner).
                var ticks = 0
                while (tile.distanceTo(tree.nearestTo(tile)) > 1 && ticks++ < 15) {
                    delay()
                }
                // Chop for a spell, replaying the animation and holding its facing on the tree - this is
                // the window in which the beaver doubles as a fletching knife (see the fletch handler).
                // PauseMode (not EmptyMode) keeps the idle-familiar watchdog from re-following the owner.
                repeat(CHOP_TICKS) { tick ->
                    mode = PauseMode
                    clearWatch()
                    if (tick % CHOP_ANIM_INTERVAL == 0) {
                        anim("beaver_multichop")
                        gfx("beaver_multichop")
                    }
                    face(tree.tile)
                    delay()
                }
                if (!owner.beastOfBurdenFull()) {
                    owner.beastOfBurden.add(log, 1)
                    owner.message("Your beaver chops the tree and stashes some ${ItemDefinitions.get(log).name.lowercase()} in its pack.")
                }
                clear(FAMILIAR_CHOPPING)
                mode = Follow(this, owner)
            }
            true
        }

        // Use a log on the beaver to fletch it into a bow, no knife needed - the beaver does the cutting.
        // Registered as an operate on the specific "<log>:beaver_familiar" key so it takes precedence
        // over the beast-of-burden store handler's "*:beaver_familiar" (which would otherwise refuse
        // the log with "won't carry yours").
        itemOnNPCOperate("*logs*", "beaver_familiar") { (npc, item) ->
            if (npc != follower) {
                return@itemOnNPCOperate
            }
            // The beaver only doubles as a fletching knife while it's cutting with the Multichop scroll
            // (or the player still holds a real knife). If it stops cutting mid-batch, fletching halts
            // with the default "You need a knife to do that." The beaver plays its own cutting animation.
            fletchLogDialog(
                item.id,
                animate = { follower?.anim("beaver_multichop") },
                hasTool = { inventory.contains("knife") || follower?.get(FAMILIAR_CHOPPING, false) == true },
            )
        }

        npcOperate("Interact", "beaver_familiar") {
            if (follower?.get(FAMILIAR_CHOPPING, false) == true) {
                message(FAMILIAR_BUSY_MESSAGE)
                return@npcOperate
            }
            if (inventory.items.any { it.id.endsWith("logs") }) {
                npc<Neutral>("'Ere, you 'ave ze logs, now form zem into a mighty dam!")
                player<Happy>("Well, I was thinking of burning, selling, or fletching them.")
                npc<Neutral>("Sacre bleu! Such a waste.")
                return@npcOperate
            }
            when (random.nextInt(4)) {
                0 -> {
                    npc<Neutral>("Vot are you doing 'ere when we could be logging and building mighty dams, alors?")
                    player<Happy>("Why would I want to build a dam again?")
                    npc<Neutral>("Why vouldn't you want to build a dam again?")
                    player<Happy>("I can't argue with that logic.")
                }
                1 -> {
                    npc<Neutral>("Pardonnez-moi - you call yourself a lumberjack?")
                    player<Happy>("No")
                    npc<Neutral>("Carry on zen.")
                }
                2 -> {
                    npc<Neutral>("Paul Bunyan 'as nothing on moi!")
                    player<Happy>("Except several feet in height, a better beard, and opposable thumbs.")
                    npc<Neutral>("What was zat?")
                    player<Happy>("Nothing.")
                }
                3 -> {
                    npc<Neutral>("Zis is a fine day make some lumber.")
                    player<Happy>("That it is!")
                    npc<Neutral>("So why are you talking to moi? Get chopping!")
                }
            }
        }
    }
}
