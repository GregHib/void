package content.entity.npc.combat.melee

import content.entity.combat.hit.npcCombatDamage
import content.entity.gfx.areaGfx
import content.entity.sound.areaSound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.entity.Id
import world.gregs.voidps.engine.entity.character.mode.PauseMode
import world.gregs.voidps.engine.entity.character.mode.Retreat
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.map.collision.random
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.timer.Key
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

@Script
class Imp : Api {

    @Id("imp")
    override fun spawn(npc: NPC) {
        npc.softTimers.start("teleport_timer")
    }

    @Key("teleport_timer")
    override fun start(npc: NPC, timer: String, restart: Boolean) = tick(npc, timer)

    @Key("teleport_timer")
    override fun tick(npc: NPC, timer: String): Int {
        teleportImp(npc, teleportChance)
        // https://x.com/JagexAsh/status/1711280844504007132
        return random.nextInt(50, 200)
    }

    init {
        npcCombatDamage("imp") { npc ->
            val player = source
            if (npc.levels.get(Skill.Constitution) - damage > 0) {
                if (random.nextDouble() < retreatChance) {
                    if (npc.levels.get(Skill.Constitution) - damage < 10) {
                        npc.softQueue("imp_retreat") {
                            npc.mode = Retreat(npc, player)
                        }
                    } else if (npc.mode !is Retreat) {
                        teleportImp(npc, teleportChanceHit)
                    }
                }
            }
        }
    }

    private val teleportRadiusMax = 20
    private val teleportRadiusMin = 5
    private val teleportChance = 0.25
    private val teleportChanceHit = 0.10
    private val telePoofVfxRadius = 5
    private val retreatChance = 0.50

    fun randomValidTile(npc: NPC): Tile {
        repeat(10) {
            val dest = npc.tile.toCuboid(teleportRadiusMax).random(npc) ?: return@repeat
            if (dest.region == npc.tile.region && dest != npc.tile && npc.tile.distanceTo(dest) >= teleportRadiusMin) {
                return dest
            }
        }
        return npc.tile
    }

    fun teleportImp(npc: NPC, chance: Double) {
        if (npc.queue.contains("death")) {
            return
        }
        if (random.nextDouble() > chance) {
            return
        }
        npc.softTimers.restart("teleport_timer")
        val destination = randomValidTile(npc)
        if (destination == npc.tile) {
            return
        }

        npc.softQueue("imp_teleport") {
            areaSound("smoke_puff", npc.tile, telePoofVfxRadius)
            areaGfx("imp_puff", npc.tile)
            npc.steps.clear()
            val mode = npc.mode
            npc.mode = PauseMode
            npc.tele(destination)
            delay(1)
            npc.gfx("imp_puff")
            npc.mode = mode
        }
    }
}
