package world.gregs.voidps.network

import world.gregs.voidps.engine.client.handle.*
import world.gregs.voidps.engine.client.ui.dialogue.IntEntered
import world.gregs.voidps.engine.client.ui.dialogue.StringEntered
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Command
import world.gregs.voidps.network.instruct.*

class InstructionHandler {
     private val interactFloorItem = FloorItemOptionHandler()
     private val command = ConsoleCommandHandler()
     private val interactDialogue = DialogueContinueHandler()
     private val intEntered = IntEntryHandler()
     private val closeInterface = InterfaceClosedHandler()
     private val interactInterface = InterfaceOptionHandler()
     private val moveContainerItem = InterfaceSwitchHandler()
     private val interactNPC = NPCOptionHandler()
     private val interactObject = ObjectOptionHandler()
     private val interactPlayer = PlayerOptionHandler()
     private val changeDisplayMode = ScreenChangeHandler()
     private val stringEntered = StringEntryHandler()
     private val walk = WalkHandler()
     private val finishRegionLoad = FinishRegionLoadHandler()

    fun handle(player: Player, instruction: Instruction) {
        when(instruction) {
            is InteractFloorItem -> interactFloorItem.validate(player, instruction)
            is Command -> command.validate(player, instruction)
            is InteractDialogue -> interactDialogue.validate(player, instruction)
            is IntEntered -> intEntered.validate(player, instruction)
            is CloseInterface -> closeInterface.validate(player, instruction)
            is InteractInterface -> interactInterface.validate(player, instruction)
            is MoveContainerItem -> moveContainerItem.validate(player, instruction)
            is InteractNPC -> interactNPC.validate(player, instruction)
            is InteractObject -> interactObject.validate(player, instruction)
            is InteractPlayer -> interactPlayer.validate(player, instruction)
            is ChangeDisplayMode -> changeDisplayMode.validate(player, instruction)
            is StringEntered -> stringEntered.validate(player, instruction)
            is Walk -> walk.validate(player, instruction)
            is FinishRegionLoad -> finishRegionLoad.validate(player, instruction)
        }
    }
}