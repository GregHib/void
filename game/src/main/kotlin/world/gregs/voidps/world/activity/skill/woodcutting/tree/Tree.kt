package world.gregs.voidps.world.activity.skill.woodcutting.tree

import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.world.activity.skill.Id
import world.gregs.voidps.world.activity.skill.woodcutting.log.Log

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
            return trees.firstOrNull { tree -> tree.id == gameObject.id }
        }
    }
}