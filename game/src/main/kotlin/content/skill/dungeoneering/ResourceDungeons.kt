package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.type.Tile

class ResourceDungeons : Script {

    init {
        objectOperate("Enter") { (target) ->
            val entrance = findResourceDungeonEntrance(target.intId) ?: return@objectOperate
            teleport(entrance.resourceDungeon, entering = entrance.entering)
        }
        objectOperate("Exit") { (target) ->
            val entrance = findResourceDungeonEntrance(target.intId) ?: return@objectOperate
            teleport(entrance.resourceDungeon, entering = entrance.entering)
        }
    }

    private suspend fun world.gregs.voidps.engine.entity.character.player.Player.teleport(resourceDungeon: ResourceDungeon?, entering: Boolean) {
        if (hasClock("resource_dungeon_teleporting")) {
            return
        }
        val dungeon = resourceDungeon ?: return
        val entrance = if (entering) dungeon.enter else dungeon.exit
        if (!has(Skill.Dungeoneering, dungeon.level)) {
            message("You need a Dungeoneering level of ${dungeon.level} to venture in there.")
            return
        }
        start("resource_dungeon_teleporting", 5)
        anim("teleport_dungeoneering")
        sound("teleport")
        gfx("teleport_dungeoneering")
        delay(1)
        tele(entrance.destination)
        anim("teleport_land_dungeoneering")
        sound("teleport_land")
        gfx("teleport_land_dungeoneering")
        sound("land_flatter", delay = 50)
        if (entering && !get(dungeon.firstTimeKey, false)) {
            set(dungeon.firstTimeKey, true)
            exp(Skill.Dungeoneering, dungeon.xp)
            message("You receive ${dungeon.xp.toInt()} Dungeoneering experience for uncovering a new dungeon for the first time.")
        }
    }

    data class MysteriousEntrance(val id: Int, val destination: Tile, val level: Int)

    data class ResourceDungeon(
        val level: Int,
        val xp: Double,
        val enter: MysteriousEntrance,
        val exit: MysteriousEntrance,
    ) {
        val firstTimeKey = "resource_dungeon_${enter.id}_xp"
    }

    companion object {
        private data class ResourceDungeonEntrance(val resourceDungeon: ResourceDungeon, val entering: Boolean)

        private val resourceDungeons = listOf(
            ResourceDungeon(10, 1100.0, MysteriousEntrance(52849, Tile(991, 4585), 10), MysteriousEntrance(52867, Tile(3132, 9933), 10)),
            ResourceDungeon(15, 1500.0, MysteriousEntrance(52855, Tile(1041, 4575), 15), MysteriousEntrance(52864, Tile(3034, 9772), 15)),
            ResourceDungeon(20, 1600.0, MysteriousEntrance(52853, Tile(1135, 4589), 20), MysteriousEntrance(52868, Tile(3104, 9826), 20)),
            ResourceDungeon(25, 2100.0, MysteriousEntrance(52850, Tile(1186, 4598), 25), MysteriousEntrance(52869, Tile(2845, 9557), 25)),
            ResourceDungeon(30, 2400.0, MysteriousEntrance(52862, Tile(3513, 3666), 30), MysteriousEntrance(52861, Tile(3498, 3633), 30)),
            ResourceDungeon(35, 3000.0, MysteriousEntrance(52857, Tile(1256, 4592), 35), MysteriousEntrance(52873, Tile(2578, 9898), 35)),
            ResourceDungeon(45, 4400.0, MysteriousEntrance(52856, Tile(1052, 4521), 45), MysteriousEntrance(52866, Tile(3022, 9741), 45)),
            ResourceDungeon(55, 6200.0, MysteriousEntrance(52851, Tile(1394, 4588), 55), MysteriousEntrance(52870, Tile(2854, 9841), 55)),
            ResourceDungeon(60, 7000.0, MysteriousEntrance(52852, Tile(1000, 4522), 60), MysteriousEntrance(52865, Tile(2912, 9810), 60)),
            ResourceDungeon(65, 8500.0, MysteriousEntrance(52863, Tile(1312, 4590), 65), MysteriousEntrance(52876, Tile(3164, 9878), 65)),
            ResourceDungeon(70, 9600.0, MysteriousEntrance(52858, Tile(1238, 4524), 70), MysteriousEntrance(52874, Tile(3160, 5521), 70)),
            ResourceDungeon(75, 11400.0, MysteriousEntrance(52860, Tile(1182, 4515), 75), MysteriousEntrance(52872, Tile(3298, 3307), 75)),
            ResourceDungeon(80, 12800.0, MysteriousEntrance(52854, Tile(1140, 4499), 80), MysteriousEntrance(52871, Tile(2697, 9442), 80)),
            ResourceDungeon(85, 15000.0, MysteriousEntrance(52859, Tile(1297, 4510), 85), MysteriousEntrance(52875, Tile(3033, 9599), 85)),
        )

        private fun findResourceDungeonEntrance(id: Int): ResourceDungeonEntrance? = resourceDungeons.firstNotNullOfOrNull { dungeon ->
            when (id) {
                dungeon.enter.id -> ResourceDungeonEntrance(dungeon, entering = true)
                dungeon.exit.id -> ResourceDungeonEntrance(dungeon, entering = false)
                else -> null
            }
        }
    }
}
