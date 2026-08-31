package content.skill.hunter

import content.entity.effect.transform
import content.entity.player.inv.item.drop
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.config.RowDefinition
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.type.Direction

class BirdSnare : Script {
    init {
        itemOption("Lay", "bird_snare") {
            layTrap(null)
        }

        floorItemOperate("Lay") { (item) ->
            if (item.id == "bird_snare") {
                layTrap(item)
            }
        }

        objectOperate("Dismantle", "bird_snare,bird_snare_fail") { (target) ->
            dismantleTrap(target, null)
        }

        objectOperate("Check", "snare_*") { (target) ->
            dismantleTrap(target, creature = Rows.get("creatures.${target.id.removePrefix("snare_")}"))
        }

        objectOperate("Investigate", "bird_snare") { (target) ->
            val id = Tables.npc("traps.bird_snare.npc")
            val npc = NPCs.find(target.tile, id)
            if (npc["smoked", false]) {
                message("The scent on this trap has been masked.")
            } else {
                message("Your scent lingers around this trap.") // TODO outfits?
            }
        }

        itemOnObjectOperate("*", "bird_snare") {
            when {
                it.item.id == "unlit_torch" -> message("I should light the torch before using it to smoke the trap.")
                it.item.id == "torch_lit" -> Traps.smoke(this, "bird_snare", it.target.tile)
                it.item.def.contains(Params.HEALS) -> message("There isn't really anywhere to put any bait on this trap.")
                else -> noInterest()
            }
        }

        huntNPC("bird_snare") { target ->
            if (transform.endsWith("_off")) {
                return@huntNPC
            }
            val creature = Rows.getOrNull("creatures.${target.id}") ?: return@huntNPC
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (tile.distanceTo(target.tile) > 2 || target["caught", false]) {
                return@huntNPC
            }
            transform("${id}_off")
            val chance = Traps.chance(this, creature)
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            if (success) {
                target["caught"] = true
            }
            target.walkToDelay(tile)
            target.walkOverDelay(tile)
            target.face(Direction.SOUTH)
            target.anim("bird_land")
            target.delay(1)
            target.anim(if (success) "bird_catch" else "bird_fail")
            target.delay(1)
            despawn(100)
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            if (!success) {
                trap.replace("bird_snare_fail")
                // TODO is there a message for this?
                return@huntNPC
            }
            target.levels.set(Skill.Constitution, 0)
            trap.replace(Tables.obj("creatures.${target.id}.caught_obj"))
            player.message("Something has been caught in your trap!")
            areaSound("bird_caught", tile)
        }

        npcDespawn("hunting_ojibway_trap_npc") {
            Traps.despawn(this, "bird_snare", "The bird snare that you laid has fallen over.")
        }
    }

    private suspend fun Player.layTrap(floorItem: FloorItem?) {
        Traps.lay(this, "bird_snare", "set_noose", floorItem)
    }

    private suspend fun Player.dismantleTrap(target: GameObject, creature: RowDefinition?) {
        val npc = NPCs.findOrNull(target.tile, "hunting_ojibway_trap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val loot = creature?.itemList("loot") ?: emptyList()
        val size = 1 + loot.size
        if (inventory.spaces < size) {
            val slots = size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        anim("take_trap")
        sound("trap_dismantle", delay = 25)
        delay(2)
        if (GameObjects.getLayer(target.tile, ObjectLayer.GROUND)?.id != target.id) {
            return
        }
        collapse(npc, target, drop = false)
        inventory.add("bird_snare")
        message("You dismantle the trap.", ChatType.Filter)
        if (creature != null) {
            for (item in loot) {
                inventory.add(item)
            }
            exp(Skill.Hunter, creature.int("xp") / 10.0)
            message("You've caught a ${creature.rowId.toLowerSpaceCase()}!", ChatType.Filter)
        }
    }

    private fun Player.collapse(npc: NPC, target: GameObject, drop: Boolean) {
        dec("trap_count")
        NPCs.remove(npc)
        GameObjects.remove(target)
        if (drop) {
            drop(target.tile, "bird_snare")
        }
    }
}
