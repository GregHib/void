package content.area.asgarnia.burthorpe.warriors_guild

import content.entity.combat.killer
import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.item
import content.entity.player.dialogue.type.npc
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

/**
 * Pay tokens to fight the cyclopes on the top floor for a chance at the next tier of defender.
 * https://runescape.wiki/w/Warriors%27_Guild#Cyclopes
 */
class CyclopesRoom : Script {

    init {
        objectOperate("Open", "cyclopes_room_door_closed,cyclopes_room_door_2_closed") { (target) ->
            if (tile in Areas["warriors_guild_cyclopes_room"]) {
                enterDoor(target)
                return@objectOperate
            }
            if (!inventory.contains(TOKEN, ENTRY_TOKENS)) {
                item(TOKEN, "You don't have enough Warrior Guild Tokens to enter the cyclopes enclosure yet, collect at least $ENTRY_TOKENS then come back.")
                return@objectOperate
            }
            explainDefenders()
            if (!inventory.remove(TOKEN, COST)) {
                return@objectOperate
            }
            message("$COST tokens are taken as you enter the room.")
            enterDoor(target)
        }

        npcOperate("Talk-to", "kamfreena") {
            explainDefenders()
        }

        entered("warriors_guild_cyclopes_room") {
            softTimers.start("warriors_guild_cyclopes")
        }

        exited("warriors_guild_cyclopes_room") {
            softTimers.stop("warriors_guild_cyclopes")
        }

        timerStart("warriors_guild_cyclopes") { TimeUnit.MINUTES.toTicks(1) }

        timerTick("warriors_guild_cyclopes") {
            if (!inventory.remove(TOKEN, COST)) {
                message("You have run out of tokens.") // TODO proper message
                tele(EXIT)
                return@timerTick Timer.CANCEL
            }
            message("$COST of your tokens crumble away.")
            Timer.CONTINUE
        }

        playerSpawn {
            if (tile in Areas["warriors_guild_cyclopes_room"]) {
                tele(EXIT)
            }
        }

        npcDeath("cyclopse_*") {
            val player = killer as? Player ?: return@npcDeath
            if (tile !in Areas["warriors_guild_cyclopes_room"]) {
                return@npcDeath
            }
            if (random.nextInt(DEFENDER_CHANCE) != 0) {
                return@npcDeath
            }
            // Dragon defenders only come from the basement so the top floor tops out at rune
            val defender = nextDefender(player.highestDefender()).takeUnless { it == "dragon_defender" } ?: "rune_defender"
            FloorItems.add(tile, defender, disappearTicks = 200, owner = player)
        }
    }

    private suspend fun Player.explainDefenders() {
        val defender = highestDefender()
        if (defender == null) {
            npc<Neutral>("kamfreena", "Well, since you haven't shown me a defender to prove your prowess as a warrior,")
            npc<Happy>("kamfreena", "I'll release some cyclopes which might drop bronze defenders for you to start off with, unless you show me another. Have fun in there.")
            return
        }
        npc<Happy>("kamfreena", "Ahh I see that you have one of the defenders already! Well done.")
        if (defender == nextDefender(defender)) {
            npc<Happy>("kamfreena", "I'll release some cyclopes which might drop the same rune defender for you as there isn't any higher! Have fun in there.")
        } else {
            npc<Happy>("kamfreena", "I'll release some cyclopes which might drop the next defender for you. Have fun in there.")
        }
    }

    /**
     * The best defender in the players inventory or equipment
     */
    private fun Player.highestDefender(): String? {
        var best: String? = null
        for (row in Tables.get("warriors_guild_defenders").rows()) {
            val defender = row.rowId
            if (inventory.contains(defender) || equipment.contains(defender)) {
                best = defender
            }
        }
        return best
    }

    private fun nextDefender(defender: String?): String = Tables.item("warriors_guild_defenders.${defender ?: "none"}.next")

    companion object {
        private const val TOKEN = "warrior_guild_token"
        private const val ENTRY_TOKENS = 100
        private const val COST = 10
        private const val DEFENDER_CHANCE = 50
        private val EXIT = Tile(2846, 3540, 2)
    }
}
