package content.skill.hunter

import content.entity.effect.transform
import content.quest.questCompleted
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.cache.definition.Params
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
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
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add

class BoxTrap : Script {
    init {
        itemOption("Lay", "box_trap") {
            layTrap(null)
        }

        floorItemOperate("Lay") { (item) ->
            if (item.id == "box_trap") {
                layTrap(item)
            }
        }

        objectOperate("Dismantle", "box_trap,box_trap_fail") { (target) ->
            dismantleTrap(target, null)
        }

        objectOperate("Check", "box_trap_ferret,box_trap_chinchompa,box_trap_carnivorous_chinchompa,box_trap_pawya,box_trap_grenwall") { (target) ->
            dismantleTrap(target, creature = Rows.get("creatures.${target.id.removePrefix("box_trap_")}"))
        }

        objectOperate("Investigate", "box_trap") { (target) ->
            val id = Tables.npc("traps.box_trap.npc")
            val npc = NPCs.find(target.tile, id)
            Traps.investigate(this, npc)
        }

        itemOnObjectOperate("*", "box_trap") {
            when {
                it.item.id == "unlit_torch" -> message("I should light the torch before using it to smoke the trap.")
                it.item.id == "lit_torch" -> Traps.smoke(this, "box_trap", it.target.tile)
                it.item.id == "papaya_fruit" || it.item.id == "raw_pawya_meat" -> bait(it.item, it.target)
                it.item.def.contains(Params.HEALS) -> message("I don't think I'd catch much using that as bait.")
                else -> noInterest()
            }
        }

        huntNPC("box_trap") { target ->
            if (transform.endsWith("_off")) {
                return@huntNPC
            }
            val creature = Rows.getOrNull("creatures.${target.id}") ?: return@huntNPC
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (target.id == "ferret" && !player.questCompleted("eagles_peak")) {
                return@huntNPC
            }
            val required = creature.itemOrNull("bait")
            if (required != null) {
                val bait: String? = get("bait")
                if (bait != required) {
                    return@huntNPC
                }
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
            despawn(100)
            val trap = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            val catching = trap.replace("box_trap_catching")
            target.anim(if (success) creature.anim("catch_anim") else creature.anim("fail_anim"))
            target.delay(1)
            if (!success) {
                catching.replace("box_trap_fail")
                return@huntNPC
            }
            target.levels.set(Skill.Constitution, 0)
            clear("bait")
            catching.replace(Tables.obj("creatures.${target.id}.caught_obj"))
            player.message("Something has been caught in your trap!")
            areaSound("box_trap_catch", tile)
        }

        npcDespawn("hunting_box_trap_npc") {
            Traps.despawn(this, "box_trap", "The box trap that you laid has fallen over.")
        }
    }

    private fun Player.bait(item: Item, trap: GameObject) {
        val npc = NPCs.find(trap.tile, "hunting_box_trap_npc")
        if (npc["owner", ""] != accountName) {
            message("This isn't your trap.")
            return
        }
        if (npc.contains("bait")) {
            message("You've already baited this trap.")
            return
        }
        if (!inventory.remove(item.id)) {
            return
        }
        anim("lay_trap_small")
        sound("drop_item", delay = 25)
        npc["bait"] = item.id
        message("You bait the trap with ${item.id.toLowerSpaceCase()}.")
    }

    private suspend fun Player.layTrap(floorItem: FloorItem?) {
        Traps.lay(this, "box_trap", "lay_box_trap", floorItem)
    }

    private suspend fun Player.dismantleTrap(target: GameObject, creature: RowDefinition?) {
        val npc = NPCs.findOrNull(target.tile, "hunting_box_trap_npc") ?: return
        if (npc["owner", ""] != accountName) {
            message("This is not your trap.")
            return
        }
        val loot = creature?.itemList("loot") ?: emptyList()
        val bait: String? = npc["bait"]
        val items = mutableListOf("box_trap")
        if (loot.isEmpty() && bait != null) {
            items.add(bait)
        }
        anim("take_trap")
        sound("trap_dismantle", delay = 25)
        delay(2)
        if (GameObjects.getLayer(target.tile, ObjectLayer.GROUND)?.id != target.id) {
            return
        }
        val added = inventory.transaction {
            for (item in items + loot) {
                add(item)
            }
        }
        if (!added) {
            message("You don't have enough inventory space.")
            return
        }
        collapse(npc, target)
        message("You dismantle the trap.", ChatType.Filter)
        if (creature != null) {
            exp(Skill.Hunter, creature.int("xp") / 10.0)
            message("You've caught a ${creature.rowId.toLowerSpaceCase()}!", ChatType.Filter)
        }
    }

    private fun Player.collapse(npc: NPC, target: GameObject) {
        dec("trap_count")
        NPCs.remove(npc)
        GameObjects.remove(target)
    }
}
