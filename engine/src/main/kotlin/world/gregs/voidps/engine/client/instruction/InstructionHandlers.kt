package world.gregs.voidps.engine.client.instruction

import world.gregs.voidps.engine.client.instruction.handle.*
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
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
    itemDefinitions: ItemDefinitions,
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
    private val examineItem = ItemExamineHandler(itemDefinitions)
    private val examineNPC = NPCExamineHandler(npcDefinitions)
    private val examineObject = ObjectExamineHandler(objectDefinitions)
    private val changeDisplayMode = ScreenChangeHandler()
    private val interactInterfaceNPC = InterfaceOnNPCOptionHandler(npcs, handler)
    private val interactInterfaceObject = InterfaceOnObjectOptionHandler(objects, handler)
    private val interactInterfacePlayer = InterfaceOnPlayerOptionHandler(players, handler)
    private val interactInterfaceItem = InterfaceOnInterfaceOptionHandler(handler)
    private val interactInterfaceFloorItem = InterfaceOnFloorItemOptionHandler(items, handler)
    private val walk = WalkHandler()
    private val worldMapClick = WorldMapClickHandler()
    private val finishRegionLoad = FinishRegionLoadHandler()
    private val executeCommand = ExecuteCommandHandler()
    private val enterString = EnterStringHandler()
    private val enterInt = EnterIntHandler()
    private val friendAddHandler = FriendAddHandler()
    private val friendDeleteHandler = FriendDeleteHandler()
    private val ignoreAddHandler = IgnoreAddHandler()
    private val ignoreDeleteHandler = IgnoreDeleteHandler()
    private val chatPublicHandler = ChatPublicHandler()
    lateinit var chatPrivateHandler: (ChatPrivate.(Player) -> Unit)
    private val quickChatPublicHandler = QuickChatPublicHandler()
    private val quickChatPrivateHandler = QuickChatPrivateHandler()
    private val clanChatJoinHandler = ClanChatJoinHandler()
    private val chatTypeChangeHandler = ChatTypeChangeHandler()
    private val clanChatKickHandler = ClanChatKickHandler()
    private val clanChatRankHandler = ClanChatRankHandler()

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
            is ExamineItem -> examineItem.validate(player, instruction)
            is ExamineNpc -> examineNPC.validate(player, instruction)
            is ExamineObject -> examineObject.validate(player, instruction)
            is ChangeDisplayMode -> changeDisplayMode.validate(player, instruction)
            is Walk -> walk.validate(player, instruction)
            is WorldMapClick -> worldMapClick.validate(player, instruction)
            is FinishRegionLoad -> finishRegionLoad.validate(player, instruction)
            is ExecuteCommand -> executeCommand.validate(player, instruction)
            is EnterString -> enterString.validate(player, instruction)
            is EnterInt -> enterInt.validate(player, instruction)
            is FriendAdd -> friendAddHandler.validate(player, instruction)
            is FriendDelete -> friendDeleteHandler.validate(player, instruction)
            is IgnoreAdd -> ignoreAddHandler.validate(player, instruction)
            is IgnoreDelete -> ignoreDeleteHandler.validate(player, instruction)
            is ChatPublic -> chatPublicHandler.validate(player, instruction)
            is ChatPrivate -> chatPrivateHandler.invoke(instruction, player)
            is QuickChatPublic -> quickChatPublicHandler.validate(player, instruction)
            is QuickChatPrivate -> quickChatPrivateHandler.validate(player, instruction)
            is ClanChatJoin -> clanChatJoinHandler.validate(player, instruction)
            is ChatTypeChange -> chatTypeChangeHandler.validate(player, instruction)
            is ClanChatKick -> clanChatKickHandler.validate(player, instruction)
            is ClanChatRank -> clanChatRankHandler.validate(player, instruction)
        }
    }
}