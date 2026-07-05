package content.skill.hunter

import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.ObjectLayer
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.entity.obj.replace

class NetTrap : Script {
    init {
        huntNPC("net_trap") { target ->
            if (transform.endsWith("_off")) {
                return@huntNPC
            }
            val creature = Rows.getOrNull("creatures.${target.id}") ?: return@huntNPC
            val account: String = get("owner") ?: return@huntNPC
            val player = Players.findByAccount(account) ?: return@huntNPC
            if (!player.has(Skill.Hunter, creature.int("level"))) {
                return@huntNPC
            }
            if (tile.distanceTo(target.tile) > 2) {
                return@huntNPC
            }
            transform("${id}_off")
            var chance = Traps.chance(this, creature)
            val success = Level.success(player.levels.get(Skill.Hunter), chance)
            val trapId = creature.string("trap")
            target.walkToDelay(tile)
            target.delay(2)
            despawn(100)
            val net = GameObjects.getLayer(tile, ObjectLayer.GROUND) ?: return@huntNPC
            net.remove()
            val trap = GameObjects.getLayer(tile.add(net.direction().inverse()), ObjectLayer.GROUND) ?: return@huntNPC
            val replaced = trap.replace("${trapId}_${if (success) "catching" else "failing"}")
            delay(2)
            replaced.replace("${trapId}_${if (success) "caught" else "failed"}")
        }
    }
}