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

/**
 * Manages and processes a variety of instruction handlers for the game world interactions.
 *
 * This class handles various types of player interactions and game instructions by delegating
 * them to the appropriate handlers. Each type of instruction is managed by its corresponding
 * handler, providing a modular and maintainable structure for handling in-game actions and events.
 *
 * @constructor Creates a new instance of InstructionHandlers with several game-related components
 * required for initializing instruction handlers.
 *
 * @param players The collection of active players in the game world.
 * @param npcs The collection of non-player characters (NPCs) in the game world.
 * @param items The collection of floor items available in the game world.
 * @param objects The collection of objects in the game world.
 * @param itemDefinitions Definitions and metadata describing all game items.
 * @param objectDefinitions Definitions and metadata describing all game objects.
 * @param npcDefinitions Definitions and metadata describing all NPCs.
 * @param interfaceDefinitions Definitions for user interfaces within the game.
 * @param handler Interface handler for user interaction with game interface elements.
 */
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
    /**
     * Represents a handler for managing interactions with floor items in the game.
     * The variable encapsulates a FloorItemOptionHandler which provides functionality
     * to handle specific options or actions related to items located on the ground.
     * It operates on a list of items that can be interacted with directly from the game floor.
     */
    private val interactFloorItem = FloorItemOptionHandler(items)
    /**
     * A private instance of DialogueContinueHandler used to manage and control the continuation of dialogue interactions.
     * It leverages interfaceDefinitions to handle specific interaction rules or configurations.
     */
    private val interactDialogue = DialogueContinueHandler(interfaceDefinitions)
    /**
     * This variable represents an instance of InterfaceClosedHandler
     * that is used to handle the closure of an interface. It encapsulates
     * logic or actions to be performed when the interface is closed.
     */
    private val closeInterface = InterfaceClosedHandler()
    /**
     * A private variable used to handle interface interactions.
     * interactInterface is instantiated with an instance of InterfaceOptionHandler.
     * It provides an abstraction layer to manage specific options or operations
     * related to the interface interaction in the application logic.
     */
    private val interactInterface = InterfaceOptionHandler(handler)
    /**
     * Handles the movement of an inventory item within the system.
     * This variable is assigned an instance of InterfaceSwitchHandler,
     * which provides the functionality to manage the switching behavior
     * for handling inventory item relocation.
     */
    private val moveInventoryItem = InterfaceSwitchHandler(handler)
    /**
     * Handles interactions with NPCs, utilizing the provided NPCs and NPCDefinitions to manage
     * and validate NPC interaction instructions. This field is an instance of `NPCOptionHandler`,
     * responsible for determining the appropriate game logic based on the interaction details
     * such as the NPC index, selected option, and player state.
     */
    private val interactNPC = NPCOptionHandler(npcs, npcDefinitions)
    /**
     * Handles interaction events with in-game objects, processing and validating instructions
     * for interacting with specific game objects based on their definitions and interaction options.
     *
     * This variable is responsible for:
     * - Determining the targeted game object using a combination of tile and object ID.
     * - Validating the interaction by checking the existence of the target object and its
     *   interaction options.
     * - Assigning the corresponding interaction mode to the player if the interaction is valid.
     *
     * Part of the instruction handling system within the game, utilized to process player actions
     * involving objects in the game world.
     */
    private val interactObject = ObjectOptionHandler(objects, objectDefinitions)
    /**
     * Holds an instance of `PlayerOptionHandler`, initialized with the provided `players` parameter.
     * This variable is used for managing player interactions and handling player-specific options
     * within the associated functionality.
     *
     * It encapsulates the logic related to player interaction events and operations, ensuring
     * appropriate behavior during these processes.
     */
    private val interactPlayer = PlayerOptionHandler(players)
    /**
     * A private variable responsible for handling item examination functionality.
     * Manages the retrieval of item details by utilizing an ItemExamineHandler instance.
     * The handler processes item definitions provided via the `itemDefinitions` parameter.
     */
    private val examineItem = ItemExamineHandler(itemDefinitions)
    /**
     * Handler for examining Non-Player Characters (NPCs) in the game.
     * This variable is initialized with a reference to the `NPCExamineHandler`,
     * which utilizes the provided `npcDefinitions` to retrieve metadata or
     * descriptions related to specific NPC entities.
     */
    private val examineNPC = NPCExamineHandler(npcDefinitions)
    /**
     * A handler instance responsible for examining objects using a predefined set of object definitions.
     *
     * This variable is used to process and analyze objects based on the logic encapsulated in the
     * ObjectExamineHandler. It leverages `objectDefinitions` for defining the characteristics or
     * criteria required for examination.
     *
     * @property examineObject The handler instance for object examination logic.
     */
    private val examineObject = ObjectExamineHandler(objectDefinitions)
    /**
     * Represents a handler responsible for managing and toggling the display mode of the screen.
     * This variable is intended to be used to facilitate changes in screen presentation states.
     */
    private val changeDisplayMode = ScreenChangeHandler()
    /**
     * Defines a handler for interacting with an NPC through a specific interface.
     * This variable assigns an instance of `InterfaceOnNPCOptionHandler` to handle
     * player interactions with NPCs when a particular interface option is activated.
     *
     * @property npcs The list of NPCs that can be interacted with via this handler.
     * @property handler The specific interaction logic to process interactions between
     * the interface and the associated NPCs.
     */
    private val interactInterfaceNPC = InterfaceOnNPCOptionHandler(npcs, handler)
    /**
     * A private variable that initializes an instance of `InterfaceOnObjectOptionHandler`.
     * It manages interactions with objects by utilizing the provided `objects` and `handler`.
     * This variable is used to handle interface-related functionalities for object options.
     */
    private val interactInterfaceObject = InterfaceOnObjectOptionHandler(objects, handler)
    /**
     * A handler for managing interactions between the user interface and player-specific options.
     *
     * The `interactInterfacePlayer` variable is used to assign functionality for handling
     * events when an interface element interacts with a player. This typically involves
     * executing specific logic provided by the handler whenever such interactions occur.
     *
     * @property players Represents the group of players targeted for this interaction.
     * @property handler Defines the logic to be executed upon interaction between the
     * user interface and a player.
     */
    private val interactInterfacePlayer = InterfaceOnPlayerOptionHandler(players, handler)
    /**
     * A private variable that holds an instance of InterfaceOnInterfaceOptionHandler.
     * This is used to manage interactions between interfaces by handling specific events or actions.
     */
    private val interactInterfaceItem = InterfaceOnInterfaceOptionHandler(handler)
    /**
     * A private variable representing the interface interaction handler for floor item options.
     * This handler manages the interactions triggered when performing actions on items placed on the floor.
     * It is initialized with a specified set of items and a corresponding handler that performs the required logic.
     */
    private val interactInterfaceFloorItem = InterfaceOnFloorItemOptionHandler(items, handler)
    /**
     * Represents an instance of the `WalkHandler` that manages or handles functionality
     * related to walking behavior within the application.
     *
     * This variable is declared as private and is not accessible outside the scope
     * of its containing class. It is typically used internally for processing walk-related
     * operations or logic.
     */
    private val walk = WalkHandler()
    /**
     * A private variable that handles click actions on the world map.
     * This variable is an instance of WorldMapClickHandler, which is used to manage
     * and process user interactions with the world map, such as detecting and responding
     * to clicks on specific map regions or elements.
     */
    private val worldMapClick = WorldMapClickHandler()
    /**
     * A handler responsible for managing the completion of a region load operation.
     * This variable is used to trigger or manage actions that need to occur
     * after a specific region load process is finalized.
     */
    private val finishRegionLoad = FinishRegionLoadHandler()
    /**
     * A private variable that holds an instance of the ExecuteCommandHandler.
     * Used to handle the execution of specific commands within the application.
     */
    private val executeCommand = ExecuteCommandHandler()
    /**
     * A private variable that holds an instance of EnterStringHandler.
     * The purpose of this handler is to manage and process string input
     * within the application. It provides functionality to handle user-entered
     * string data appropriately.
     */
    private val enterString = EnterStringHandler()
    /**
     * A private instance of the `EnterIntHandler` responsible for handling integer input operations.
     * Encapsulates the logic related to managing or processing integer input in the system.
     */
    private val enterInt = EnterIntHandler()
    /**
     * Handler for processing friend addition events.
     * This variable is responsible for managing the logic
     * required when a new friend is added within the application.
     */
    private val friendAddHandler = FriendAddHandler()
    /**
     * A private instance of FriendDeleteHandler used to manage the deletion of friends.
     * It encapsulates the logic and operations required to handle friend removal processes.
     */
    private val friendDeleteHandler = FriendDeleteHandler()
    /**
     * A private variable used to manage the state or instance of an IgnoreAddHandler.
     * The handler may be responsible for selectively ignoring or managing the addition
     * of certain elements or operations, depending on its implementation.
     *
     * It is likely used internally within the context of the class or component
     * to enforce specific rules or behaviors related to addition processes.
     */
    private val ignoreAddHandler = IgnoreAddHandler()
    /**
     * A private instance of the `IgnoreDeleteHandler` class.
     *
     * This variable is utilized to manage and handle scenarios where
     * delete operations should be ignored. Typically used to prevent
     * specific objects or records from being processed for deletion
     * based on predefined logic determined by the handler implementation.
     */
    private val ignoreDeleteHandler = IgnoreDeleteHandler()
    /**
     * Instance of ChatPublicHandler used to manage and handle public chat-related operations.
     */
    private val chatPublicHandler = ChatPublicHandler()
    /**
     * An instance of `ChatPrivateHandler` used to manage private chat-related functionalities.
     * This encapsulates logic specific to handling private message interactions and operations.
     */
    private val chatPrivateHandler = ChatPrivateHandler()
    /**
     * A private instance of `QuickChatPublicHandler` used to manage or handle public chat-related operations.
     * This handler is responsible for executing tasks specific to the quick chat functionality within a public context.
     */
    private val quickChatPublicHandler = QuickChatPublicHandler()
    /**
     * Handler responsible for managing private quick chat interactions.
     * It facilitates operations related to private communication in a quick chat context.
     */
    private val quickChatPrivateHandler = QuickChatPrivateHandler()
    /**
     * Represents a handler responsible for managing actions when a player
     * joins a clan chat. This object facilitates the execution of specific
     * logic or triggers to ensure appropriate behavior upon a clan chat join event.
     */
    private val clanChatJoinHandler = ClanChatJoinHandler()
    /**
     * Handles changes related to the chat type in the application.
     * This variable is responsible for managing and executing the logic required
     * when the type of chat is altered, ensuring the application responds
     * appropriately to such changes.
     */
    private val chatTypeChangeHandler = ChatTypeChangeHandler()
    /**
     * A private variable that handles the functionality related to kicking a member
     * from the clan chat. It encapsulates the logic required to manage and enforce
     * kick actions within the context of clan chats.
     */
    private val clanChatKickHandler = ClanChatKickHandler()
    /**
     * An instance of ClanChatRankHandler used to manage and handle
     * functionalities related to clan chat ranking within the application.
     */
    private val clanChatRankHandler = ClanChatRankHandler()

    /**
     * Handles the given instruction for the specified player. The function processes different types of
     * instructions and delegates their handling to the appropriate handlers or operations based on the
     * instruction type.
     *
     * @param player The player for whom the instruction is being handled.
     * @param instruction The instruction to handle, determining the specific logic to execute.
     */
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
            is ChatPrivate -> chatPrivateHandler.validate(player, instruction)
            is QuickChatPublic -> quickChatPublicHandler.validate(player, instruction)
            is QuickChatPrivate -> quickChatPrivateHandler.validate(player, instruction)
            is ClanChatJoin -> clanChatJoinHandler.validate(player, instruction)
            is ChatTypeChange -> chatTypeChangeHandler.validate(player, instruction)
            is ClanChatKick -> clanChatKickHandler.validate(player, instruction)
            is ClanChatRank -> clanChatRankHandler.validate(player, instruction)
        }
    }
}