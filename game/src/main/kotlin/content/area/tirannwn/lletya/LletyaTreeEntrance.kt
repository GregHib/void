package content.area.tirannwn.lletya

import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile

class LletyaTreeEntrance : Script {

    init {
        objectOperate("Pass", "lletya_tree_entrance") { (target) ->
            if (!questCompleted("roving_elves")) {
                statement("The trees are too dense for you to find a way through.")
                return@objectOperate
            }
            val direction = if (tile.x < target.tile.x) Direction.EAST else Direction.WEST
            val dest = Tile(target.tile.x + direction.delta.x * 2, target.tile.y, tile.level)
            face(direction)
            delay()
            anim("dense_forest_squeeze")
            exactMoveDelay(dest, delay = 90, direction = direction)
        }
    }
}
