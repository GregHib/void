package content.area.kharidian_desert.kalphite_lair

import content.area.wilderness.inMultiCombat
import content.entity.combat.Target
import content.entity.combat.attackers
import content.entity.combat.hit.hit
import content.entity.combat.target
import content.entity.effect.clearTransform
import content.entity.effect.transform
import content.entity.proj.shoot
import org.rsmod.game.pathfinder.LineValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.areaSound
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.move.hasLineOfSight
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Distance
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class KalphiteQueen(val lineOfSight: LineValidator) : Script {
    init {
        npcCombatDamage("kalphite_queen") {
            if (random.nextInt(20) != 0) {
                return@npcCombatDamage
            }
            spawnWorker(it.source)
        }

        npcCombatPrepare("kalphite_queen*") {
            clear("chain_hits")
            true
        }

        npcCombatAttack("kalphite_queen*") {
            if (it.type == "range" && it.damage > 0) {
                it.target.levels.drain(Skill.Prayer, 1)
            } else if (it.type == "magic") {
                set("chain_hits", mutableSetOf(it.target.index))
                chainGlow(this, it.target)
            }
        }

        npcCanDie("kalphite_queen") {
            transform == "kalphite_queen_airborne"
        }

        npcLevelChanged(Skill.Constitution, "kalphite_queen") { _, _, to ->
            if (to > 10) {
                return@npcLevelChanged
            }
            if (transform == "kalphite_queen_airborne") {
                return@npcLevelChanged
            }
            val target = target
            levels.restore(Skill.Constitution, 2550)
            for (attacker in attackers) {
                attacker.mode = EmptyMode
            }
            mode = PauseMode
            steps.clear()
            clearFace()
            clearWatch()
            start("movement_delay", 10)
            clearAnim()
            anim("kalphite_queen_death")
            areaSound("kalphite_queen_death", tile, radius = 20)
            queue("emerging", 14) {
                if (target is Player) {
                    interactPlayer(target, "Attack")
                } else {
                    mode = EmptyMode
                }
            }
            queue("emerge", 2) {
                anim("kalphite_queen_emerging")
                gfx("kalphite_queen_emerging")
                GameObjects.add("kalphite_queen_emerging_legs", tile, ticks = 8)
                transform("kalphite_queen_airborne")
            }
            softTimers.start("kalphite_queen_revert")
        }

        npcTimerStart("kalphite_queen_revert") {
            TimeUnit.MINUTES.toTicks(20)
        }

        npcTimerTick("kalphite_queen_revert") {
            // TODO what if in combat?
            clearTransform()
            Timer.CANCEL
        }
    }

    fun chainGlow(source: NPC, target: Character) {
        if (target !is Player || !target.inMultiCombat) {
            return
        }
        val chain: MutableSet<Int> = source.getOrPut("chain_hits") { mutableSetOf() }
        for (tile in target.tile.spiral(4)) {
            for (player in Players.at(tile)) {
                if (chain.contains(player.index) || !Target.attackable(source, player)) {
                    continue
                }

                if (!lineOfSight.hasLineOfSight(target, player)) {
                    continue
                }
                chain.add(player.index)
                val time = target.shoot(id = "kalphite_queen_lightning_travel", target = player)
                source.hit(player, Item.EMPTY, "magic", special = true, delay = time)
                return
            }
        }
    }

    private fun NPC.spawnWorker(source: Character?) {
        if (queue.contains("cocoon_idle")) {
            return
        }
        for (objTile in listOf(
            Tile(3482, 9502),
            Tile(3486, 9498),
            Tile(3490, 9502),
            Tile(3486, 9506),
            Tile(3493, 9483),
            Tile(3480, 9481),
            Tile(3477, 9481),
            Tile(3464, 9495),
            Tile(3472, 9508),
            Tile(3486, 9518),
            Tile(3488, 9518),
            Tile(3502, 9504),
            Tile(3502, 9501),
        )) {
            val nearest = Distance.nearest(tile, size, size, objTile)
            val distance = nearest.distanceTo(objTile)
            if (distance > 4) { // TODO actual distance
                continue
            }
            val cocoon = GameObjects.findOrNull(objTile, "kalphite_cocoon") ?: continue
            areaSound("kalphite_cocoon_break", cocoon.tile, radius = 10)
            cocoon.anim("kalphite_cocoon_break")
            val direction = when (cocoon.rotation) {
                1 -> Direction.NORTH
                2 -> Direction.EAST
                3 -> Direction.SOUTH
                else -> Direction.WEST
            }
            val npc = NPCs.add("kalphite_worker", cocoon.tile.add(direction), direction)
            if (source is Player) {
                npc.interactPlayer(source, "Attack")
            }
            queue("cocoon_respawn", 10) {
                cocoon.anim("kalphite_cocoon_return")
            }
            queue("cocoon_idle", 12) {
                cocoon.anim("kalphite_cocoon_idle")
            }
            return
        }
    }
}
