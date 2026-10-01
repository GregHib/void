package content.area.misthalin.tutorial_island

import content.entity.obj.ObjectTeleports
import content.entity.obj.door.openDoor
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.obj.GameObject

/**
 * Doors, gates and ladders that only open once the tutorial has reached the stage they
 * lead on from. A handler registered for a concrete object id replaces the wildcard
 * handlers in [content.entity.obj.door.Doors] and
 * [content.entity.obj.ObjectTeleporting] for that object, so both are re-invoked here
 * after the stage check passes.
 */
class TutorialObjects(val teleports: ObjectTeleports) : Script {

    init {
        gatedDoor("door_87_closed", 3)
        gatedDoor("gate_72_closed,gate_73_closed", 13)
        gatedDoor("door_88_closed", 14)
        gatedDoor("door_89_closed", 19)
        gatedDoor("door_90_closed", 23)
        gatedDoor("gate_74_closed,gate_75_closed", 38)
        gatedDoor("gate_76_closed,gate_77_closed", 46)
        gatedDoor("door_91_closed", 53)
        gatedDoor("door_92_closed", 55)
        gatedDoor("door_93_closed", 62)

        gatedLadder("ladder_tutorial_island_cave_down", "Climb-down", 27)
        gatedLadder("ladder_tutorial_island_rat_pit_up", "Climb-up", 51)

        // Registered on the wildcard so the Mining script's own prospect handler still runs;
        // a handler bound to the concrete id would replace it instead.
        objectApproach("Prospect") { (target) ->
            when (target.id) {
                "tin_rocks_tutorial_island_1" -> advanceTutorial(29)
                "copper_rocks_tutorial_island_1" -> advanceTutorial(30)
            }
        }

        itemOnObjectOperate("bronze_bar", "anvil") {
            advanceTutorial(36)
        }

        objectOperate("Use", "bank_booth_tutorial_island") {
            advanceTutorial(52)
        }
    }

    /**
     * Registers [stage] as the gate an object guards; the player passes through only once
     * they've been told to.
     */
    private fun gatedDoor(ids: String, stage: Int) {
        objectOperate("Open", ids) { (target) ->
            if (!allowed(stage, target)) {
                return@objectOperate
            }
            // Advance first: opening a double door replaces both halves, and despawning the
            // object being interacted with cancels this coroutine before it could resume.
            advanceTutorial(stage)
            openDoor(target)
        }
    }

    private fun gatedLadder(id: String, option: String, stage: Int) {
        objectOperate(option, id) { (target) ->
            if (!allowed(stage, target)) {
                return@objectOperate
            }
            advanceTutorial(stage)
            teleports.teleport(this, target, option)
        }
    }

    private fun Player.allowed(stage: Int, target: GameObject): Boolean {
        if (!inTutorial || tutorialStage >= stage) {
            return true
        }
        message("You should talk to your instructor before going through the ${target.def.name.lowercase()}.")
        return false
    }
}
