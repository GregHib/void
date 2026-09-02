package content.area.kandarin.tree_gnome_stronghold.training_camp

import content.entity.npc.shop.openShop
import content.entity.obj.door.Gate
import content.entity.player.dialogue.Angry
import content.entity.player.dialogue.Confused
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.player
import content.quest.questCompleted
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.type.Tile

class CombatTrainingCamp : Script {

    init {
        objectOperate("Open", "lathas_training_gate_left_closed,lathas_training_gate_right_closed") { (target) ->
            val entering = tile.y <= target.tile.y
            if (entering && !questCompleted("biohazard")) {
                npc<Neutral>("guard_combat_training_camp", "This is a restricted area, you can only enter under the authority of King Lathas.")
                return@objectOperate
            }
            if (entering) {
                npc<Neutral>("guard_combat_training_camp", "The king has granted you access to this training area. Make good use of it, soon all your strength will be needed.")
            }
            openGate(target, entering)
        }

        npcOperate("Talk-to", "guard_combat_training_camp") {
            player<Happy>("Hello there.")
            npc<Angry>("What do you want? Leave us be.")
        }

        npcOperate("Talk-to", "guard_combat_training_camp_2") {
            player<Happy>("Hello.")
            npc<Neutral>("Well hello brave warrior. These ogres have been terrorising the area, they've eaten four children this week alone.")
            player<Angry>("Brutes!")
            npc<Neutral>("So we decided to use them for target practice. A fair punishment.")
            player<Neutral>("Indeed.")
        }

        npcOperate("Talk-to", "guard_combat_training_camp_3") {
            player<Happy>("Hello.")
            npc<Neutral>("Hello soldier.")
            player<Confused>("I'm more of an adventurer really.")
            npc<Neutral>("In this day and age we're all soldiers. No time to waste gassing, Fight! Fight!")
        }

        npcOperate("Trade", "shopkeeper_combat_training_camp") { (target) ->
            openShop(target.def["shop"])
        }

        npcOperate("Talk-to", "shopkeeper_combat_training_camp") { (target) ->
            player<Happy>("Hello.")
            npc<Neutral>("So, are you looking to buy weapons? King Lathas keeps us very well stocked.")
            choice {
                option<Neutral>("What do you have?") {
                    npc<Happy>("Take a look.")
                    openShop(target.def["shop"])
                }
                option<Neutral>("No thanks.")
            }
        }
    }

    private suspend fun Player.openGate(gate: GameObject, entering: Boolean) {
        val column = if (tile.x == GATE_LEFT.x || tile.x == GATE_RIGHT.x) tile.x else gate.tile.x
        val near = Tile(column, if (entering) GATE_LEFT.y else GATE_LEFT.y + 1)
        val far = Tile(column, if (entering) GATE_LEFT.y + 1 else GATE_LEFT.y)
        if (tile != near) {
            walkOverDelay(near)
        }
        val left = GameObjects.findOrNull(GATE_LEFT, "lathas_training_gate_left_closed")
        val right = GameObjects.findOrNull(GATE_RIGHT, "lathas_training_gate_right_closed")
        if (left == null || right == null) {
            walkOverDelay(far)
            return
        }
        Gate.replaceTogether(
            this, left, right,
            flip = false,
            ticks = GATE_TICKS,
            collision = false,
            current = "_closed",
            next = "_opened",
            objRotation = 3,
            hingeTileRotation = 1,
        )
        walkOverDelay(far)
    }

    private companion object {
        val GATE_LEFT = Tile(2517, 3356)
        val GATE_RIGHT = Tile(2518, 3356)
        const val GATE_TICKS = 3
    }
}
