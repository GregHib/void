package rs.dusk.world.activity.skill.woodcutting.tree

import rs.dusk.engine.entity.definition.DefinitionsDecoder.Companion.toIdentifier
import rs.dusk.engine.entity.obj.GameObject
import rs.dusk.world.activity.skill.Id
import rs.dusk.world.activity.skill.woodcutting.log.Log

interface Tree : Id {
    val log: Log?
    val level: Int
    val xp: Double
    val depleteRate: Double
    val chance: IntRange
    val lowDifference: IntRange
    val highDifference: IntRange
    val respawnDelay: IntRange

    companion object {

        private val trees: Array<Tree> = arrayOf(
            *RegularTree.values(),
            *DungeoneeringTree.values(),
            *CursedTree.values(),
            *JadinkoRoots.values()
        )

        fun get(gameObject: GameObject): Tree? {
            val name = toIdentifier(gameObject.def.name)
            return trees.firstOrNull { tree -> tree.id == name }
        }
    }
}