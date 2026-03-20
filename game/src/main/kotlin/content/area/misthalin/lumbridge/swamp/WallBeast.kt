package content.area.misthalin.lumbridge.swamp

import content.entity.combat.hit.directHit
import content.entity.combat.target
import content.entity.combat.underAttack
import content.entity.effect.clearTransform
import content.entity.effect.transform
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.handle.interactPlayer
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.AreaDefinition
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.queue.strongQueue
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class WallBeast : Script {
    init {
        entered("wall_beast_spot_1", ::grab)
        entered("wall_beast_spot_2", ::grab)
        entered("wall_beast_spot_3", ::grab)
        entered("wall_beast_spot_4", ::grab)
        entered("wall_beast_spot_5", ::grab)
        entered("wall_beast_spot_6", ::grab)
        entered("wall_beast_spot_7", ::grab)

        npcTimerStart("wall_beast_revert") {
            TimeUnit.SECONDS.toTicks(8) // TODO this is triggered on combat stop not a timer
        }

        npcTimerTick("wall_beast_revert") {
            if (underAttack) { // FIXME should be: attacking but doesn't check if swinging or not
                return@npcTimerTick Timer.CONTINUE
            }
            clearTransform()
            mode = EmptyMode
            Timer.CANCEL
        }
    }

    fun grab(player: Player, definition: AreaDefinition) {
        if (random.nextInt(4) != 0) {
            return
        }
        val tile = definition.area.random()
        val beast = NPCs.find(tile.addY(1), "hole_in_the_wall")
        if (beast.transform == "wall_beast") {
            return
        }
        if (beast.softTimers.contains("wall_beast_revert")) {
            return
        }
        if (beast.target != null) {
            return
        }
        if (beast.queue.contains("grab_player")) {
            return
        }
        player.steps.clear()
        player.strongQueue("grab_player", 2) {
            player.tele(tile)
            player.face(Direction.NORTH)
            beast["movement_delay"] = Int.MAX_VALUE
            if (player.equipped(EquipSlot.Hat).id == "spiny_helmet") {
                player.message("Your helmet repels the wall beast!")
                beast.anim("wall_beast_repelled_attack")
                player.sound("wall_beast_foiled")
                beast.transform("wall_beast")
                pause(4)
                beast.interactPlayer(player, "Attack")
                beast.softTimers.start("wall_beast_revert")
                return@strongQueue
            }
            player.message("A giant hand appears and grabs your head!")
            player.anim("grabbed_by_wall_beast")
            player.sound("wall_beast_attack")
            beast.anim("wall_beast_attack")
            pause(1)
            player.anim("held_by_wall_beast")
            beast.anim("wall_beast_hold")
            pause(8)
            beast.anim("released_by_wall_beast")
            player.anim("wall_beast_release")
            player.sound("male_defend_0")
            player.directHit(160, source = beast)
            beast.softTimers.start("wall_beast_revert")
        }
    }
}
