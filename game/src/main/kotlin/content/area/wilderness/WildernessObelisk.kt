package content.area.wilderness

import content.skill.magic.book.modern.teleBlocked
import content.skill.magic.spell.Teleport
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectOperate
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.random

@Script
class WildernessObelisk {

    val areas: AreaDefinitions by inject()
    val objects: GameObjects by inject()
    val players: Players by inject()

    val obelisks = areas.getTagged("obelisk")

    init {
        objectOperate("Activate", "wilderness_obelisk_*") {
            if (World.containsQueue(target.id)) {
                return@objectOperate
            }
            val definition = areas.getOrNull(target.id) ?: return@objectOperate
            val rectangle = (definition.area as Rectangle)
            replace(target, Tile(rectangle.minX - 1, rectangle.minY - 1))
            replace(target, Tile(rectangle.maxX + 1, rectangle.minY - 1))
            replace(target, Tile(rectangle.minX - 1, rectangle.maxY + 1))
            replace(target, Tile(rectangle.maxX + 1, rectangle.maxY + 1))
            World.queue(target.id, 7) {
                val obelisk = obelisks.random(random)
                for (player in players[target.tile.zone]) {
                    if (player.tile !in rectangle || player.teleBlocked) {
                        continue
                    }
                    player.message("Ancient magic teleports you somewhere in the Wilderness!")
                    Teleport.teleport(player, obelisk.area.random(), "wilderness")
                }
            }
        }
    }

    fun replace(obj: GameObject, tile: Tile) {
        val sw = objects[tile, obj.id] ?: return
        objects.replace(sw, "wilderness_obelisk_glow", ticks = 8)
    }
}
