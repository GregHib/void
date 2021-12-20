package world.gregs.voidps.engine.client.instruction

import world.gregs.voidps.engine.client.instruction.handle.*
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.Instruction
import world.gregs.voidps.network.instruct.*

class InstructionHandlers {
    private val interactFloorItem = FloorItemOptionHandler()
    private val interactDialogue = DialogueContinueHandler()
    private val closeInterface = InterfaceClosedHandler()
    private val interactInterface = InterfaceOptionHandler()
    private val moveContainerItem = InterfaceSwitchHandler()
    private val interactNPC = NPCOptionHandler()
    private val interactObject = ObjectOptionHandler()
    private val interactPlayer = PlayerOptionHandler()
    private val examineNPC = NPCExamineHandler()
    private val examineObject = ObjectExamineHandler()
    private val changeDisplayMode = ScreenChangeHandler()
    private val interactInterfaceNPC = InterfaceOnNPCOptionHandler()
    private val interactInterfaceObject = InterfaceOnObjectOptionHandler()
    private val interactInterfacePlayer = InterfaceOnPlayerOptionHandler()
    private val interactInterfaceItem = InterfaceOnInterfaceOptionHandler()
    private val walk = WalkHandler()
    private val finishRegionLoad = FinishRegionLoadHandler()
    private val executeCommand = ExecuteCommandHandler()
    private val enterString = EnterStringHandler()
    private val enterInt = EnterIntHandler()

    fun handle(player: Player, instruction: Instruction) {
        when (instruction) {
            is Event -> player.events.emit(instruction)
            is InteractInterfaceItem -> interactInterfaceItem.validate(player, instruction)
            is InteractInterfacePlayer -> interactInterfacePlayer.validate(player, instruction)
            is InteractInterfaceObject -> interactInterfaceObject.validate(player, instruction)
            is InteractInterfaceNPC -> interactInterfaceNPC.validate(player, instruction)
            is InteractFloorItem -> interactFloorItem.validate(player, instruction)
            is InteractDialogue -> interactDialogue.validate(player, instruction)
            is CloseInterface -> closeInterface.validate(player, instruction)
            is InteractInterface -> interactInterface.validate(player, instruction)
            is MoveContainerItem -> moveContainerItem.validate(player, instruction)
            is InteractNPC -> interactNPC.validate(player, instruction)
            is InteractObject -> interactObject.validate(player, instruction)
            is InteractPlayer -> interactPlayer.validate(player, instruction)
            is ExamineNpc -> examineNPC.validate(player, instruction)
            is ExamineObject -> examineObject.validate(player, instruction)
            is ChangeDisplayMode -> changeDisplayMode.validate(player, instruction)
            is Walk -> walk.validate(player, instruction)
            is FinishRegionLoad -> finishRegionLoad.validate(player, instruction)
            is ExecuteCommand -> executeCommand.validate(player, instruction)
            is EnterString -> enterString.validate(player, instruction)
            is EnterInt -> enterInt.validate(player, instruction)
        }
    }
}