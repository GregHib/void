package content.area.kandarin.baxtorian_falls.ancient_cavern

import content.entity.player.dialogue.*
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.skill.slayer.slayerMaster
import content.skill.slayer.slayerTaskRemaining
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class KuradalsDungeon : Script {
    init {
        objTeleportTakeOff("Enter", "kuradal_dungeon_cave") { _, _ ->
            if (inventory.items.any { it.id == "cannon_barrels" || it.id == "cannon_furnace" || it.id == "cannon_stand" || it.id == "cannon_base" }) {
                queue("kuradal_cannon_ban") {
                    npc<Neutral>("kuradal", "No cannons are allowed in there.") // TODO proper message
                }
                return@objTeleportTakeOff Teleport.CANCEL
            }
            if (slayerTaskRemaining <= 0 || slayerMaster != "kuradal") {
                queue("kuradal_task_check") {
                    npc<Neutral>("kuradal", "Sorry, my dungeon is exclusive only to those who need to go in there.")
                    player<Quiz>("Exclusive?")
                    npc<Neutral>("kuradal", "Yes, I only allow pupils of Slayer into this dungeon and only if they need to slayer the creatures I've caught inside.")
                    player<Quiz>("I see. So what creatures do I need to be assigned to kill; what creatures are inside?")
                    npc<Happy>("kuradal", "Well, I took a holiday tour of Gielinor and brought back some 'souvenirs'. Of course, as a Slayer master, 'souvenirs' means hellhounds, greater demons, gargoyles, abyssal demons, airut, dark beasts, and blue, iron, and steel dragons.")
                    player<Laugh>("I'd usually keep it simple with a postcard.")
                    npc<Quiz>("kuradal", "What was that?")
                    player<Unamused>("I said that must have been hard.")
                }
                return@objTeleportTakeOff Teleport.CANCEL
            }
            Teleport.CONTINUE
        }

        objectOperate("Pass", "kuradal_barrier") { (target) ->
            if (target.rotation == 2) { // vertical
                val x = if (tile.x <= target.tile.x) target.tile.x + 1 else target.tile.x
                val y = tile.y.coerceIn(target.tile.y, target.tile.y + 1)
                walkOverDelay(tile.copy(y = y))
                anim("pass_through_barrier")
                exactMoveDelay(Tile(x, y))
            } else if (target.rotation == 3) { // horizontal
                val x = tile.x.coerceIn(target.tile.x, target.tile.x + 1)
                val y = if (tile.y >= target.tile.y) target.tile.y - 1 else target.tile.y
                walkOverDelay(tile.copy(x = x))
                anim("pass_through_barrier")
                exactMoveDelay(Tile(x, y))
            }
        }

        objectOperate("Climb-over", "kuradal_dungeon_low_wall") { (target) ->
            if (!has(Skill.Agility, 86, message = true)) {
                return@objectOperate
            }
            val end = if (tile.y < target.tile.y) Tile(1633, 5294) else Tile(1633, 5292)
            val start = if (tile.y < target.tile.y) Tile(1633, 5292) else Tile(1633, 5294)
            val direction = if (tile.y < target.tile.y) Direction.NORTH else Direction.SOUTH
            walkOverDelay(start)
            face(direction)
            delay()
            anim("rocks_pile_climb") // TODO proper anim
            exactMoveDelay(end, 30, direction = direction)
        }
    }
}
