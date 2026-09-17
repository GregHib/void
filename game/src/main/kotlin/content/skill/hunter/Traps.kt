package content.skill.hunter

import content.entity.player.inv.item.drop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.stepAway
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Tile

object Traps {
    fun max(level: Int, max: Int) = (1 + level / 20).coerceAtMost(max)

    suspend fun lay(player: Player, trapId: String, sound: String, floorItem: FloorItem?) {
        val trap = Rows.getOrNull("traps.$trapId") ?: return
        val level = player.levels.get(Skill.Hunter)
        if (!player.has(Skill.Hunter, trap.int("level"), message = true)) {
            return
        }
        if (Areas.get(player.tile.zone).any { it.tags.contains("bank") } || GameObjects.getLayer(player.tile, ObjectLayer.GROUND) != null) {
            player.message("You can't lay a trap here.", ChatType.Filter)
            return
        }
        val max = max(level, trap.int("max"))
        if (player.get("trap_count", 0) >= max) {
            player.message("You may setup only $max ${"trap".plural(max)} at a time at your Hunter level.")
            return
        }
        player.arriveDelay()
        player.message("You begin setting up ${if (max == 1) "the" else "a"} trap.", ChatType.Filter)
        player.anim("lay_trap")
        player.sound(sound)
        player.delay(3)
        if (GameObjects.getLayer(player.tile, ObjectLayer.GROUND) != null) {
            player.message("You can't lay a trap here.", ChatType.Filter)
            return
        }
        if (floorItem != null) {
            if (!FloorItems.remove(floorItem)) {
                return
            }
        } else if (!player.inventory.remove(trapId)) {
            return
        }
        player.inc("trap_count")
        NPCs.add(Tables.npc("traps.$trapId.npc"), player.tile, ticks = 100, owner = player)
        val obj = GameObjects.add(trapId, player.tile, collision = false)
        player.stepAway(obj)
    }

    fun despawn(npc: NPC, trapItem: String, collapseMessage: String) {
        val trap = GameObjects.getLayer(npc.tile, ObjectLayer.GROUND) ?: return
        GameObjects.remove(trap)
        val bait: String? = npc["bait"]
        val player = npc.owner
        if (player == null) {
            FloorItems.add(trap.tile, trapItem, disappearTicks = 200)
            if (bait != null) {
                FloorItems.add(trap.tile, bait, disappearTicks = 200)
            }
            return
        }
        player.dec("trap_count")
        val drop = if (npc.lifecycle == 0) {
            player.message(collapseMessage)
            true
        } else {
            player["logged_out", false]
        }
        if (drop) {
            player.drop(trap.tile, trapItem)
            if (bait != null) {
                player.drop(trap.tile, bait)
            }
        }
    }

    fun chance(npc: NPC, creature: RowDefinition): IntRange {
        val chance = creature.intRange("chance")
        var add = 0
        if (npc.contains("bait")) {
            add += 7 // 3%
        }
        if (npc["smoked", false]) {
            add += 5 // 2%
        }
        if (add == 0) {
            return chance
        }
        return (chance.first + add)..(chance.last + add)
    }

    fun investigate(player: Player, npc: NPC) {
        val bait: String? = npc["bait"]
        if (bait != null) {
            player.message("This trap has been baited with ${bait.toLowerSpaceCase()}.")
        } else {
            player.message("This trap has been set without any bait.")
        }
        if (npc["smoked", false]) {
            player.message("The scent on this trap has been masked.")
        } else {
            player.message("Your scent lingers around this trap.")
        }
    }

    fun smoke(player: Player, trap: String, tile: Tile) {
        val id = Tables.npc("traps.$trap.npc")
        val npc = NPCs.find(tile, id)
        if (npc["owner", ""] != player.accountName) {
            player.message("This isn't your trap.")
            return
        }
        if (npc["smoked", false]) {
            player.message("You've already smoked this trap.")
            return
        }
        player.anim("lay_trap_small")
        areaSound("hunting_smoke2", tile = tile, radius = 5)
        npc["smoked"] = true
        player.message("You use the smoke from the torch to remove your scent from the trap.", type = ChatType.Filter)
    }
}
