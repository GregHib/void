package content.skill.dungeoneering

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
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

    data class MysteriousEntrance(val id: Int, val destination: Tile)

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

        private val resourceDungeons by lazy {
            Tables.get("resource_dungeons").rows().map { row ->
                ResourceDungeon(
                    level = row.int("level"),
                    xp = row.int("xp").toDouble(),
                    enter = MysteriousEntrance(row.int("enter_id"), row.tile("enter")),
                    exit = MysteriousEntrance(row.int("exit_id"), row.tile("exit")),
                )
            }
        }

        private fun findResourceDungeonEntrance(id: Int): ResourceDungeonEntrance? = resourceDungeons.firstNotNullOfOrNull { dungeon ->
            when (id) {
                dungeon.enter.id -> ResourceDungeonEntrance(dungeon, entering = true)
                dungeon.exit.id -> ResourceDungeonEntrance(dungeon, entering = false)
                else -> null
            }
        }
    }
}
