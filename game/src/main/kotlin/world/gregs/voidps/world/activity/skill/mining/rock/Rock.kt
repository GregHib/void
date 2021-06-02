package world.gregs.voidps.world.activity.skill.mining.rock

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.mining.ore.Ore

interface Rock : Id {
    val ores: List<Ore>
    val level: Int
    val respawnDelay: Int

    companion object {

        private val rocks: Array<Rock> = arrayOf(
            *RegularRock.values(),
            GraniteRock,
            SandstoneRock,
            PureEssence,
            GemRock
        )

        fun get(player: Player, gameObject: GameObject): Rock? {
            val rock = rocks.firstOrNull { rock -> gameObject.stringId.startsWith(rock.id, true) }
            if (rock == PureEssence && !player.has(Skill.Mining, rock.level, message = false)) {
                return RuneEssence
            }
            return rock
        }
    }
}