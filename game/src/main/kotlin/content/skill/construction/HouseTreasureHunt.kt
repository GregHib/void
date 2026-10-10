package content.skill.construction

import content.skill.construction.House.Companion.houseBase
import content.skill.construction.House.Companion.houseOwner
import content.skill.construction.House.Companion.houseRoomPositions
import content.skill.construction.House.Companion.roomZone
import content.skill.construction.HouseGamesRoom.Companion.FAIRY
import content.skill.construction.HouseGamesRoom.Companion.offerPrize
import content.skill.construction.HouseGamesRoom.Companion.winPrize
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.carriesItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

/**
 * Games room treasure hunt: a fairy hides somewhere in the house and magic stones tell how close she is.
 * The player who finds her is awarded the prize if it was offered for the game.
 * https://oldschool.runescape.wiki/w/Games_room
 */
class HouseTreasureHunt : Script {
    init {
        objectOperate("Activate", "treasure_hunt") { (target) ->
            if (get("house_build_mode", false)) {
                message("You cannot summon the fairy while in building mode.")
                return@objectOperate
            }
            val owner = houseOwner() ?: return@objectOperate
            if (owner.contains("treasure_fairy")) {
                if (!carriesItem("treasure_stone") && inventory.add("treasure_stone")) {
                    message("You take a magic stone that can lead you to the treasure.")
                }
                return@objectOperate
            }
            val tile = hide() ?: return@objectOperate
            areaSound("poh_fairy_appear", target.tile, radius = 5)
            offerPrize(FAIRY)
            val fairy = NPCs.add(FAIRY_NPC, target.tile.add(-2, 1), Direction.SOUTH, ticks = 10)
            fairy.say("I will hide somewhere in the house.")
            owner["treasure_fairy"] = tile
            delay(3)
            fairy.say("These magic stones will guide you to me!")
            message("Where is she hiding the treasure?", ChatType.ObjectExamine)
        }

        itemOption("Feel", "treasure_stone") {
            val owner = houseOwner()
            val fairy: Tile? = owner?.get("treasure_fairy")
            if (owner == null || fairy == null) {
                message("The stone is inert. No fairy is hidden in the house.")
                return@itemOption
            }
            val distance = if (tile.level == fairy.level) tile.distanceTo(fairy) else FAR
            if (distance <= FOUND) {
                found(owner, fairy)
                return@itemOption
            }
            val band = BANDS.indexOfFirst { distance <= it.first }
            val last = get("treasure_distance", Int.MAX_VALUE)
            val lastBand = get("treasure_band", -1)
            set("treasure_distance", distance)
            set("treasure_band", band)
            message(
                when {
                    band != lastBand || distance == last -> BANDS[band].second
                    distance < last -> "Warmer..."
                    else -> "Colder..."
                },
            )
        }
    }

    private fun Player.found(owner: Player, fairy: Tile) {
        message("You've found the fairy!", ChatType.Filter)
        anim("emote_dance")
        areaSound("poh_fairy_appear", fairy, radius = 5)
        NPCs.add(FAIRY_NPC, fairy, Direction.SOUTH, ticks = 10).say("$accountName has found me!")
        owner.clear("treasure_fairy")
        clear("treasure_distance")
        clear("treasure_band")
        winPrize(FAIRY)
    }

    /**
     * A random free tile in one of the rooms of the house
     */
    private fun Player.hide(): Tile? {
        val base = houseBase() ?: return null
        val positions = houseRoomPositions
        repeat(ATTEMPTS) {
            val zone = roomZone(base, positions[random.nextInt(positions.size)])
            val tile = zone.tile.add(random.nextInt(ROOM_SIZE), random.nextInt(ROOM_SIZE))
            if (!blocked(tile, Direction.NONE) && tile != this.tile) {
                return tile
            }
        }
        return null
    }

    companion object {
        private const val FAIRY_NPC = "3954"
        private const val FOUND = 2
        private const val FAR = 100
        private const val ATTEMPTS = 50
        private const val ROOM_SIZE = 8

        // Furthest distance for each message, from hottest to coldest
        private val BANDS = listOf(
            4 to "Very hot!",
            8 to "Hot!",
            14 to "Very warm.",
            22 to "Warm.",
            Int.MAX_VALUE to "Cold.",
        )
    }
}
