package content.area.kandarin.baxtorian_falls.ancient_cavern

import content.entity.combat.hit.directHit
import content.entity.player.inv.item.addOrDrop
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.remove
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class AncientCavern(val drops: DropTables) : Script {
    init {
        objectApproach("Dive in", "ancient_cavern_whirlpool") {
            walkToDelay(Tile(tile.x.coerceIn(2511..2512), 3516))
            face(Direction.SOUTH)
            delay(1)
            anim("jump_whirlpool")
            exactMove(Tile(2512, 3508), startDelay = 15, delay = 255, direction = Direction.SOUTH)
            delay(4)
            open("fade_out")
            delay(4)
            message("You dive into the swirling maelstrom of the whirlpool.", type = ChatType.Filter)
            tele(1763, 5365, 1)
            delay(1)
            message("You are swirled beneath the water, the darkness and pressure are overwhelming.", type = ChatType.Filter)
            delay(1)
            message("Mystical forces guide you into a cavern below the whirlpool.", type = ChatType.Filter)
            delay(3)
            open("fade_in")
        }

        objectOperate("Ride", "ancient_cavern_aged_log") {
            message("You jump on the log and dislodge it. You guide your makeshift vessel through the caves to an unknown destination.", type = ChatType.Filter)
            open("fade_out")
            delay(5)
            // TODO: Canoe cutscene would go here
            open("fade_in")
            tele(2531, 3446)
            delay(4)
            message("You find yourself on the banks of the river, far below the lake.", type = ChatType.Filter)
        }

        objectOperate("Rummage", "ancient_cavern_skeleton") { (target) ->
            val table = drops.get("ancient_cavern_skeleton") ?: return@objectOperate
            val item = table.roll().single()
            message("You rummage in the sharp, slimy pile of bones in search of something useful...")
            if (random.nextInt(11) < 8) {
                directHit(4 + (levels.get(Skill.Constitution) * (random.nextInt(20) / 100)))
            }
            target.remove(ticks = TimeUnit.MINUTES.toTicks(2))
            when (item.id) {
                "skeleton_guard" -> {
                    val npc = NPCs.add("skeleton_heavy", tile)
                    npc.interactPlayer(this, "Attack")
                    message("... the bones object.")
                }
                "guard_dog" -> {
                    val npc = NPCs.add("skeleton_hero", tile)
                    npc.interactPlayer(this, "Attack")
                    message("... the bones object.")
                }
                "nothing" -> message("...but there's nothing remotely valuable.")
                else -> if (addOrDrop(item.id, 1)) {
                    message("...you find something and stow it in your pack.")
                } else {
                    message("...you find something, but it drops to the floor.")
                }
            }
        }

        objectSpawn("ancient_cavern_skeleton") {
            val spawn = NPCs.findOrNull(tile.regionLevel) {
                (it.id == "skeleton_hero" || it.id == "skeleton_heavy") && it.tile.distanceTo(tile) <= 8
            } ?: return@objectSpawn
            NPCs.remove(spawn)
        }
    }
}
