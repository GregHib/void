package content.area.realm.corporeal_beasts_lair

import content.entity.combat.hit.Damage
import content.entity.combat.hit.hit
import content.entity.gfx.areaGfx
import content.entity.proj.shoot
import content.entity.proj.shootNearest
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class CorporealBeast : Script {
    init {
        npcSpawn("corporeal_beast") {
            softTimers.start("corp_stomp")
        }

        npcTimerStart("corp_stomp") { 7 }

        npcTimerTick("corp_stomp") {
            var found = false
            for (tile in tile.toCuboid(size, size)) {
                for (player in Players.at(tile)) {
                    found = true
                    hit(player, offensiveType = "damage", damage = Damage.roll(this, player, offensiveType = "damage", range = 300..510))
                }
            }
            if (found) {
                anim("corporeal_beast_stomp")
                gfx("corporeal_beast_stomp")
            }

            val lair = Areas["corporeal_beasts_lair"]
            val count = Players.count { it.tile in lair }
            if (count >= 8) {
                levels.restore(Skill.Constitution, 250 + (count * 50))
            } else if (count == 0) {
                levels.restore(Skill.Constitution, 250)
            }
            Timer.CONTINUE
        }

        // TODO stat regen every 20t

        npcCombatDamage("corporeal_beast") {
            if (it.damage < 320) {
                return@npcCombatDamage
            }
            spawnDarkCore(this, it.source)
        }

        npcCombatAttack("corporeal_beast") {
            if (levels.get(Skill.Constitution) >= 10000) {
                return@npcCombatAttack
            }
            spawnDarkCore(this, it.target)
        }

        npcAttack("corporeal_beast", "magic_area") { target ->
            val tile = target.tile
            val time = shootNearest("corporeal_beast_area_travel", target.tile)
            areaGfx("corporeal_beast_magic_impact", tile.add(1, -1), delay = time)
            val cuboid = tile.toCuboid(3)
            for (i in 1..6) {
                val target = cuboid.random()
                val delay = tile.shoot("corporeal_beast_area_travel", target, delay = time)
                areaGfx("corporeal_beast_magic_impact", target.add(1, -1), delay = time + delay)
                areaOfEffect(this, i, target, delay, 300)
            }
            areaOfEffect(this, 0, tile, time, 400)
        }
    }

    fun areaOfEffect(source: NPC, i: Int, tile: Tile, time: Int, damage: Int) {
        source.queue("area_of_effect_$i", CLIENT_TICKS.toTicks(time)) {
            for (player in Players.at(tile)) {
                source.hit(player, delay = 0, offensiveType = "magic", damage = Damage.roll(source, player, offensiveType = "magic", range = 0..damage))
            }
            for (dir in Direction.all) {
                for (player in Players.at(tile.add(dir))) {
                    source.hit(player, delay = 0, offensiveType = "magic", damage = Damage.roll(source, player, offensiveType = "magic", range = 0..(damage - 100)))
                }
            }
        }
    }

    fun spawnDarkCore(source: NPC, target: Character) {
        if (random.nextInt(8) != 0) {
            return
        }
        val core = NPCs.findOrNull(source.tile.regionLevel, "dark_energy_core")
        if (core != null) {
            return
        }
        val tile = target.tile
        val delay = source.shoot("dark_energy_core_travel", target)
        source.queue("core_spawn", CLIENT_TICKS.toTicks(delay)) {
            NPCs.add("dark_energy_core", tile)
        }
    }
}
