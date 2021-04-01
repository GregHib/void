package world.gregs.voidps.world.activity.skill.woodcutting.log

import world.gregs.voidps.world.activity.skill.Id

interface Log : Id {

    companion object {

        private val logs: Array<Log> = arrayOf(
            *RegularLog.values(),
            *ColouredLog.values(),
            *CursedLog.values(),
            *DungeoneeringBranch.values(),
            *JadinkoRoot.values(),
            *MiscLog.values(),
            *PyreLog.values()
        )

        fun get(id: String): Log? {
            return logs.firstOrNull { tree -> tree.id == id }
        }
    }
}