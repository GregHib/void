package content.area.tirannwn.lletya

import content.entity.player.dialogue.type.statement
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObjects
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
            val partner = GameObjects.findOrNull(Tile(target.tile.x, if (target.tile.y == 3191) 3195 else 3191, tile.level), "lletya_tree_entrance")
            target.anim("treegate_open")
            partner?.anim("treegate_open")
            sound("treedoor_open")
            walkOverDelay(dest)
        }
    }
}
