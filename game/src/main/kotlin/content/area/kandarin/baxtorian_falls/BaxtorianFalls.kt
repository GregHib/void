package content.area.kandarin.baxtorian_falls

import content.entity.combat.hit.damage
import content.entity.player.inv.item.addOrDrop
import content.quest.quest
import content.quest.questStage
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.inv.equipment
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class BaxtorianFalls : Script {
    init {
        objectOperate("Board", "waterfall_raft") {
            if (quest("waterfall_quest") == "unstarted") {
                message("The raft is not yet sea worthy.")
                return@objectOperate
            }
            message("You board the small raft.")
            delay(2)
            message("and push off down stream.")
            delay(3)
            message("The raft is pulled down stream by strong currents.")
            tele(2511, 3481)
            delay(2)
            message("You crash into a small land mound.")
        }

        objectApproach("Swim", "waterfall_river") { (target) ->
            if (target.tile.x in 2511..2513 && target.tile.y in 3475..3476) swimDownstream()
        }

        itemOnObjectApproach("rope", "waterfall_rock*") { (target) ->
            walkOverDelay(Tile(2512, 3476))
            face(target.tile)
            anim("waterfall_rope_throw")
            // The throw holds a rope during the wind-up; its flying effect starts at release.
            gfx("waterfall_rope_throw", delay = 65, height = 0)
            delay(3)
            GameObjects.replace(target, "waterfall_rock_rope", ticks = 10)
            for (y in 3469..3475) {
                GameObjects.add("waterfall_crossing_rope", Tile(2512, y), ticks = 10, collision = false)
            }
            delay()
            message("You throw the rope around the rock and pull yourself across.")
            anim("waterfall_rope_pull", override = true)
            // The client sequence lasts 72 frames; keep the movement inside that animation.
            exactMove(Tile(2512, 3469), delay = 72, direction = Direction.SOUTH)
            // Round up: two server ticks would clear the pull before the client finishes moving.
            delay(3)
            clearAnim()
            walkOverDelay(Tile(2513, 3468))
        }

        objectApproach("Swim to", "waterfall_rock*") { swimDownstream() }

        itemOnObjectApproach("rope", "dead_tree_13") {
            message("You tie the rope to the tree and climb down to the ledge below.")
            tele(2511, 3463)
        }

        objectApproach("Climb", "dead_tree_13") {
            message("You try to use the tree to climb down...")
            delay(2)
            message("...but you slip and fall into the water.")
            washOut()
        }

        objectOperate("Get in", "waterfall_barrel") {
            message("You climb into the barrel and push off the edge. You are carried down the river.")
            tele(2527, 3413)
        }

        objectOperate("Enter", "waterfall_entrance*") {
            if (questStage("waterfall_quest") < 4 || !equipment.contains("glarials_amulet")) {
                message("You try to open the door, but the ledge is suddenly flooded with water...")
                delay(2)
                message("...you are pushed over the waterfall and into the river.")
                washOut()
            } else {
                if (quest("waterfall_quest") == "found_urn") {
                    set("waterfall_quest", "entered_dungeon")
                }
                tele(2575, 9861)
                message("You enter the waterfall.")
            }
        }

        objectOperate("Open", "waterfall_exit") { (target) ->
            if (target.tile == Tile(2574, 9860)) {
                tele(2511, 3463)
                message("You exit the dungeon.")
            }
        }

        objectOperate("Search", "crate_10") {
            if (inventory.contains("a_key")) {
                // TODO nothing of interest
                return@objectOperate
            }
            addOrDrop("a_key")
            message("You find a key in the crate.")
        }
    }

    private fun Player.washOut() {
        damage(80)
        tele(2527, 3413)
    }

    private suspend fun Player.swimDownstream() {
        message("You swim out into the water...")
        delay(2)
        washOut()
        message("...but the current is too strong, washing you downstream.")
    }
}
