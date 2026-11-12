package content.area.misthalin.tutorial_island

import content.entity.obj.door.enterDoor
import content.entity.player.dialogue.type.statement
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Teleport
import world.gregs.voidps.engine.entity.obj.GameObject

/** Stage-gated passages using ordinary door movement and object teleport hooks. */
class TutorialObjects : Script {

    init {
        objectOperate("Open", "door_87_closed") { (target) -> openFirstDoor(target) }

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

        // Keep the Mining script's concrete prospect handlers active.
        objectApproach("Prospect") { (target) -> prospect(target) }

        itemOnObjectOperate("bronze_bar", "anvil") {
            advanceTutorial(36)
        }

        objectOperate("Use", "bank_booth_tutorial_island") {
            advanceTutorial(52)
        }
    }

    private suspend fun Player.openFirstDoor(target: GameObject) {
        val leaving = tile.x < target.tile.x
        if (leaving && !allowed(3, target)) {
            return
        }
        if (leaving) {
            advanceTutorial(3)
        }
        enterDoor(target)
    }

    private fun Player.prospect(target: GameObject) {
        when (target.id) {
            "tin_rocks_tutorial_island_1" -> advanceTutorial(29)
            "copper_rocks_tutorial_island_1" -> advanceTutorial(30)
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
            // Keep collision closed for everyone else, including both halves of double gates.
            // Advance before the temporary replacement cancels the object's interaction.
            advanceTutorial(stage)
            enterDoor(target)
        }
    }

    private fun gatedLadder(id: String, option: String, stage: Int) {
        objTeleportTakeOff(option, id) { target, _ ->
            if (!allowed(stage, target)) {
                return@objTeleportTakeOff Teleport.CANCEL
            }
            Teleport.CONTINUE
        }
        objTeleportLand(option, id) { _, _ -> advanceTutorial(stage) }
    }

    private suspend fun Player.allowed(stage: Int, target: GameObject): Boolean {
        if (!inTutorial || tutorialStage >= stage) {
            return true
        }
        val text = "You should talk to your instructor before going through the ${target.def.name.lowercase()}."
        message(text)
        // Tutorial instructions occupy the chat box and hide ordinary chat-history lines.
        // Use the standard statement interface too, so both clients show the refusal.
        statement(text)
        return false
    }
}
