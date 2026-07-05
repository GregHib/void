package content.skill.hunter

import content.entity.combat.Combat
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction

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
            it.updateInteraction {
                Combat.combat(it.character, target)
            }
        }

        objectOperate("Trap", "pitfall") {
            layTrap(it.target)
        }

        objectOperate("Trap", "pitfall_*") {
            // Temp to avoid #1059
        }

        objectOperate("Jump", "pitfall_spiked") { (target) ->
            val dir = if (target.rotation == 1 || target.rotation == 3) {
                if (tile.x > target.tile.x) Direction.WEST else Direction.EAST
            } else {
                if (tile.y > target.tile.y) Direction.SOUTH else Direction.NORTH
            }
            anim("agility_pyramid_gap_jump")
            exactMove(tile.add(dir).add(dir).add(dir), delay = 53, direction = dir)
            areaSound("hunting_jump", target.tile)
        }

        objectOperate("Dismantle", "pitfall_*") { (target) ->
            dismantleTrap(target)
        }

        for (i in 0..16) {
            timerStart("collapse_pitfall_$i") { 100 }
            timerTick("collapse_pitfall_$i") {
                clear("pitfall_$i")
                message("The pitfall trap that you constructed has collapsed.")
                Timer.CANCEL
            }
        }
    }

    private suspend fun Player.layTrap(obj: GameObject) {
        val trap = Rows.getOrNull("traps.pitfall") ?: return
        val level = trap.int("level") // TODO different pits have different levels
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
        softTimers.start("collapse_${obj.id}")
    }

    private suspend fun Player.dismantleTrap(target: GameObject) {
        message("You dismantle the trap.", ChatType.Filter)
        anim("lay_trap_small")
        delay(1)
        sound("take_branches")
        dec("trap_count")
        set(target.id, "empty")
        softTimers.clear("collapse_${target.id}")
    }

}