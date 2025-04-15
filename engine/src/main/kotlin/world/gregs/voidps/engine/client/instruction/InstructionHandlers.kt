package world.gregs.voidps.engine.client.instruction

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.instruction.handle.*
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.Event
import world.gregs.voidps.network.client.Instruction
import world.gregs.voidps.network.client.instruction.*

class InstructionHandlers(
    players: Players,
    npcs: NPCs,
    items: FloorItems,
    objects: GameObjects,
    objectDefinitions: ObjectDefinitions,
    npcDefinitions: NPCDefinitions,
    interfaceDefinitions: InterfaceDefinitions,
    handler: InterfaceHandler
) {
    private val interactFloorItem = FloorItemOptionHandler(items)
    private val interactDialogue = DialogueContinueHandler(interfaceDefinitions)
    private val closeInterface = InterfaceClosedHandler()
    private val interactInterface = InterfaceOptionHandler(handler, interfaceDefinitions)
    private val moveInventoryItem = InterfaceSwitchHandler(handler)
    private val interactNPC = NPCOptionHandler(npcs, npcDefinitions)
    private val interactObject = ObjectOptionHandler(objects, objectDefinitions)
    private val interactPlayer = PlayerOptionHandler(players)
    private val interactInterfaceNPC = InterfaceOnNPCOptionHandler(npcs, handler)
    private val interactInterfaceObject = InterfaceOnObjectOptionHandler(objects, handler)
    private val interactInterfacePlayer = InterfaceOnPlayerOptionHandler(players, handler)
    private val interactInterfaceItem = InterfaceOnInterfaceOptionHandler(handler)
    private val interactInterfaceFloorItem = InterfaceOnFloorItemOptionHandler(items, handler)
    private val executeCommand = ExecuteCommandHandler()
    var finishRegionLoad: FinishRegionLoad.(Player) -> Unit = empty()
    var changeDisplayMode: ChangeDisplayMode.(Player) -> Unit = empty()
    var walk: Walk.(Player) -> Unit = empty()
    var worldMapClick: WorldMapClick.(Player) -> Unit = empty()
    var examineItem: ExamineItem.(Player) -> Unit = empty()
    var examineNPC: ExamineNpc.(Player) -> Unit = empty()
    var examineObject: ExamineObject.(Player) -> Unit = empty()
    var enterString: EnterString.(Player) -> Unit = empty()
    var enterInt: EnterInt.(Player) -> Unit = empty()
    var friendAddHandler: FriendAdd.(Player) -> Unit = empty()
    var friendDeleteHandler: FriendDelete.(Player) -> Unit = empty()
    var ignoreAddHandler: IgnoreAdd.(Player) -> Unit = empty()
    var ignoreDeleteHandler: IgnoreDelete.(Player) -> Unit = empty()
    var chatPublicHandler: ChatPublic.(Player) -> Unit = empty()
    var chatPrivateHandler: ChatPrivate.(Player) -> Unit = empty()
    var quickChatPublicHandler: QuickChatPublic.(Player) -> Unit = empty()
    var quickChatPrivateHandler: QuickChatPrivate.(Player) -> Unit = empty()
    var clanChatJoinHandler: ClanChatJoin.(Player) -> Unit = empty()
    var chatTypeChangeHandler:ChatTypeChange.(Player) -> Unit = empty()
    var clanChatKickHandler: ClanChatKick.(Player) -> Unit = empty()
    var clanChatRankHandler: ClanChatRank.(Player) -> Unit = empty()

    private fun <I : Instruction> empty(): I.(Player) -> Unit {
        val logger = InlineLogger("InstructionHandler")
        return {
            logger.warn { "Unhandled instruction: $this $it" }
        }
    }

    fun handle(player: Player, instruction: Instruction) {
        when (instruction) {
            is Event -> player.emit(instruction)
            is InteractInterfaceItem -> interactInterfaceItem.validate(player, instruction)
            is InteractInterfacePlayer -> interactInterfacePlayer.validate(player, instruction)
            is InteractInterfaceObject -> interactInterfaceObject.validate(player, instruction)
            is InteractInterfaceNPC -> interactInterfaceNPC.validate(player, instruction)
            is InteractInterfaceFloorItem -> interactInterfaceFloorItem.validate(player, instruction)
            is InteractFloorItem -> interactFloorItem.validate(player, instruction)
            is InteractDialogue -> interactDialogue.validate(player, instruction)
            is InterfaceClosedInstruction -> closeInterface.validate(player, instruction)
            is InteractInterface -> interactInterface.validate(player, instruction)
            is MoveInventoryItem -> moveInventoryItem.validate(player, instruction)
            is InteractNPC -> interactNPC.validate(player, instruction)
            is InteractObject -> interactObject.validate(player, instruction)
            is InteractPlayer -> interactPlayer.validate(player, instruction)
            is ExamineItem -> examineItem.invoke(instruction, player)
            is ExamineNpc -> examineNPC.invoke(instruction, player)
            is ExamineObject -> examineObject.invoke(instruction, player)
            is ChangeDisplayMode -> changeDisplayMode.invoke(instruction, player)
            is Walk -> walk.invoke(instruction, player)
            is WorldMapClick -> worldMapClick.invoke(instruction, player)
            is FinishRegionLoad -> finishRegionLoad.invoke(instruction, player)
            is ExecuteCommand -> executeCommand.validate(player, instruction)
            is EnterString -> enterString.invoke(instruction, player)
            is EnterInt -> enterInt.invoke(instruction, player)
            is FriendAdd -> friendAddHandler.invoke(instruction, player)
            is FriendDelete -> friendDeleteHandler.invoke(instruction, player)
            is IgnoreAdd -> ignoreAddHandler.invoke(instruction, player)
            is IgnoreDelete -> ignoreDeleteHandler.invoke(instruction, player)
            is ChatPublic -> chatPublicHandler.invoke(instruction, player)
            is ChatPrivate -> chatPrivateHandler.invoke(instruction, player)
            is QuickChatPublic -> quickChatPublicHandler.invoke(instruction, player)
            is QuickChatPrivate -> quickChatPrivateHandler.invoke(instruction, player)
            is ClanChatJoin -> clanChatJoinHandler.invoke(instruction, player)
            is ChatTypeChange -> chatTypeChangeHandler.invoke(instruction, player)
            is ClanChatKick -> clanChatKickHandler.invoke(instruction, player)
            is ClanChatRank -> clanChatRankHandler.invoke(instruction, player)
        }
    }
}