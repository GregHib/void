package content.skill.hunter

import content.entity.combat.attacker
import content.entity.combat.dead
import content.entity.combat.target
import content.entity.combat.underAttack
import content.skill.melee.weapon.attackSpeed
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.combat.CombatApi
import world.gregs.voidps.engine.entity.character.mode.combat.CombatMovement
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class Pitfall : Script {
    init {
        npcOperate("Tease") {
            val target = it.target
            val level = Tables.int("creatures.${target.id}.level")
            if (!has(Skill.Hunter, level)) {
                message("You need a hunter level of at least $level to catch a ${target.def.name.lowercase()}.")
                return@npcOperate
            }
            if (!inventory.contains("teasing_stick")) {
                message("I don't want to lose a finger by poking it. Maybe I should get a professional teasing implement instead.")
                return@npcOperate
            }
            anim("tease")
            sound("scythe_slash")
            if (!target.dead && !target.underAttack) {
                attacker = target
                this.target = target
                target.mode = CombatMovement(target, this)
                target.target = this
                target.attacker = this
                val delay = target.attackSpeed / 2
                target.start("action_delay", delay)
                target.start("under_attack", delay + 8)
            }
        }

        objectOperate("Trap", "pitfall") {
            layTrap(it.target)
        }

        objectOperate("Jump", "pitfall_spiked") { (target) ->
            val dir = jumpDirection(target, this.tile)
            if (dir == Direction.EAST || dir == Direction.WEST) {
                walkToDelay(tile.copy(y = tile.y.coerceIn(target.tile.y..target.tile.y + 1)))
            } else if (dir == Direction.NORTH || dir == Direction.SOUTH) {
                walkToDelay(tile.copy(x = tile.x.coerceIn(target.tile.x..target.tile.x + 1)))
            }
            anim("agility_pyramid_gap_jump")
            areaSound("hunting_jump", target.tile)
            exactMoveDelay(tile.add(dir).add(dir).add(dir), delay = 53, direction = dir)
            val attacker = attacker
            if (attacker !is NPC || attacker.id != "spined_larupia" && attacker.id != "horned_graahk" && attacker.id != "sabre_toothed_kyatt") {
                return@objectOperate
            }
            val offset = when (dir) {
                Direction.WEST -> target.tile.addX(2)
                Direction.EAST -> target.tile.addX(-2)
                Direction.NORTH -> target.tile.addY(-2)
                Direction.SOUTH -> target.tile.addY(2)
                else -> return@objectOperate
            }
            if (attacker.tile.within(offset, 3)) {
                attacker.walkToDelay(offset)
            } else {
                delay(2)
            }
            if (attacker.tile != offset) {
                return@objectOperate
            }
            attacker.walkOverDelay(attacker.tile.add(dir).add(dir))
            attacker.levels.set(Skill.Constitution, 0)
            attacker.gfx("pitfall_collapse_${random.nextInt(4)}")
            areaSound("pitfall_collapse", target.tile)
            set(target.id, "collapsed")
            delay(2)
            val flip = when {
                attacker.id == "horned_graahk" -> dir == Direction.SOUTH || dir == Direction.EAST
                attacker.id == "sabre_toothed_kyatt" -> dir == Direction.SOUTH || dir == Direction.WEST
                else -> dir == Direction.NORTH || dir == Direction.EAST
            }
            set(target.id, if (flip) "inverse" else "caught")
            CombatApi.stop(attacker, this)
        }

        objectOperate("Dismantle", "pitfall_*") { (target) ->
            dismantleTrap(target)
        }

        for (i in 0..16) {
            timerStart("collapse_pitfall_$i") { 100 } // TODO check collapse ticks
            timerTick("collapse_pitfall_$i") {
                clear("pitfall_$i")
                message("The pitfall trap that you constructed has collapsed.")
                Timer.CANCEL
            }
        }

        objectOperate("Trap", "pitfall_*") {
            // Temp to avoid #1059
        }
    }

    private fun jumpDirection(target: GameObject, tile: Tile): Direction = if (target.rotation == 1 || target.rotation == 3) {
        if (tile.x > target.tile.x) Direction.WEST else Direction.EAST
    } else {
        if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
    }

    private suspend fun Player.layTrap(obj: GameObject) {
        val trap = Rows.getOrNull("traps.${obj.id.substringBeforeLast("_")}") ?: return
        val level = trap.int("level")
        if (!has(Skill.Hunter, level, message = false)) {
            message("You need a hunter level of at least $level to set a pitfall trap here.")
            return
        }
        if (get(obj.id, "empty") != "empty") {
            return
        }
        val max = Traps.max(levels.get(Skill.Hunter), 5)
        val trapCount = get("trap_count", 0)
        if (trapCount >= max) {
            message("You may setup only $max ${"trap".plural(max)} at a time at your Hunter level.")
            return
        }
        if (!inventory.contains("knife") || !inventory.contains("logs")) {
            message("You need some logs and a knife to set a pitfall trap.")
            return
        }
        arriveDelay()
        anim("lay_trap_small")
        inventory.remove("logs")
        delay(1)
        sound("place_branches")
        inc("trap_count")
        set(obj.id, "spiked")
        softTimers.start("collapse_pitfall_${obj.id.substringAfterLast("_")}")
    }

    private suspend fun Player.dismantleTrap(target: GameObject) {
        val state = get(target.id, "empty")
        val creature = Rows.get("creatures.${target.id.removePrefix("pitfall_").substringBeforeLast("_")}")
        val items = creature.itemList("loot")
        val loot = state == "caught" || state == "inverse"
        if (loot && inventory.spaces < items.size) {
            val slots = items.size - inventory.spaces
            message("You don't have enough inventory space. You need $slots more free ${"slot".plural(slots)}.")
            return
        }
        message("You dismantle the trap.", ChatType.Filter)
        anim("lay_trap_small")
        delay(1)
        sound("take_branches")
        dec("trap_count")
        set(target.id, "empty")
        softTimers.clear("collapse_pitfall_${creature.rowId}")
        if (loot) {
            message("You've caught a ${creature.rowId.toLowerSpaceCase()}!", type = ChatType.Filter)
            for (item in items) {
                // TODO lerp chance of replacing tatty with full fur
                inventory.add(item)
            }
            exp(Skill.Hunter, creature.int("xp") / 10.0)
        }
    }
}
