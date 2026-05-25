package content.skill.dungeoneering

import content.quest.smallInstance
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.zone.DynamicZones
import world.gregs.voidps.type.Tile

class Dungeoneering : Script {
    init {
        adminCommand("dungeon") {
            println("Generating structured dungeon...")

            val generator = DungeonGenerator(
                size = DungeonSize.Small,
                floor = 1,
                complexity = 1
            )
            val start = System.currentTimeMillis()
            val dungeon = generator.generate()
            println("Took ${System.currentTimeMillis() - start}ms")

            dungeon.prettyPrint()

            println("\nLegend:")
            println("  S   : Start Room")
            println("  B   : Boss Room (Goal)")
            println("  P   : Puzzle Room (Guaranteed to have an exit)")
            println("  .   : Normal Room")
            println("  a-t : Critical Keys (lowercase)")
            println("  u-z : Bonus Keys (lowercase)")
            println("  A-T : Critical Locked Doors (uppercase)")
            println("  U-Z : Bonus Locked Doors (uppercase)")
            println("  G   : Outward Guardian Door")
            println("  K   : Outward Skill Door")
            println("  + - | : Walls and normal corridors")

            val instance = smallInstance()
            val zones = get<DynamicZones>()
            val origin = instance.tile.zone
            var tile = Tile.EMPTY
            for (room in dungeon.grid) {
                if (room == null) {
                    continue
                }
                if (room.type == DungeonRoomType.Base) {
                    tile = room.tile
                }
                val zone = room.zone ?: continue
                val target = origin.add(room.tile.x * 2, room.tile.y * 2)
                // Iterate through the 2x2 zones of the room template
                for (sx in 0..1) {
                    for (sy in 0..1) {
                        // Calculate the target zone offset (tx, ty) based on CW rotation
                        val tx = when (room.rotation) {
                            0 -> sx
                            1 -> 1 - sy
                            2 -> 1 - sx
                            3 -> sy
                            else -> sx
                        }
                        val ty = when (room.rotation) {
                            0 -> sy
                            1 -> sx
                            2 -> 1 - sy
                            3 -> 1 - sx
                            else -> sy
                        }
                        val clientRotation = (4 - room.rotation) % 4
                        zones.copy(zone.add(sx, sy), target.add(tx, ty), clientRotation)
                    }
                }
            }
            tele(instance.tile.add(tile.x * 16 + 8, tile.y * 16 + 8))
        }
    }
}