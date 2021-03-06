package world.gregs.voidps.network.codec

import world.gregs.voidps.network.ClientSession

abstract class Handler {

    open fun apCoordinate(session: ClientSession, first: Int, second: Int, third: Int, fourth: Int, fifth: Int) {}

    /**
     * Notified the type of message before a message is sent
     * @param type The type of message sent (0 = public, 1 = friends chat)
     */
    open fun changeChatType(session: ClientSession, type: Int) {}

    /**
     * Attempt to kick a clan mate
     * @param owner Whether is the players clan - aka myClan
     * @param equals Whether the name is a match
     * @param member The display name of the member to kick
     */
    open fun kickClanMember(session: ClientSession, owner: Boolean, equals: Int, member: String) {}

    /**
     * Requests a change to the players clans forum thread
     * @param string The clans forum thread
     */
    open fun requestClanForumThread(session: ClientSession, string: String) {}

    /**
     * Requests a change to the players clans name
     * @param name The new clan name
     */
    open fun requestClanName(session: ClientSession, name: String) {}

    /**
     * @param first Unknown value
     * @param string Unknown value
     */
    open fun updateClanSettings(session: ClientSession, first: Int, string: String) {}

    /**
     * A command typed out in the client console
     * @param command The command sent by the player
     */
    open fun consoleCommand(session: ClientSession, command: String) {}

    open fun cutsceneAction(session: ClientSession) {}

    /**
     * Notification that the "Click here to continue" button was pressed on a dialogue
     * @param hash The interface and component id combined
     * @param button
     */
    open fun continueDialogue(session: ClientSession, hash: Int, button: Int) {}

    open fun handleDialogue(session: ClientSession, hash: Int, button: Int) {}

    /**
     * An option selection on a floor item
     * @param id The item id
     * @param run Whether the player should force run
     * @param y The items y coordinate
     * @param x The items x coordinate
     * @param optionIndex The option id - 3 = Take
     */
    open fun floorItemOption(session: ClientSession, id: Int, run: Boolean, y: Int, x: Int, optionIndex: Int) {}

    /**
     * Player wants to join a friends chat
     * @param name The display name of the friend who's chat to join
     */
    open fun joinFriendsChat(session: ClientSession, name: String) {}

    /**
     * Player wants to kick a player from their friends chat
     * @param name The display name of the player to kick
     */
    open fun kickFriendsChat(session: ClientSession, name: String) {}

    /**
     * Player wants to change the rank of a friend on their friend list
     * @param name The display name of the player who's rank to change
     * @param rank The rank to give their friend
     */
    open fun rankFriendsChat(session: ClientSession, name: String, rank: Int) {}

    /**
     * Player wants to add another player to their friend list
     * @param name The display name of the player to add
     */
    open fun addFriend(session: ClientSession, name: String) {}

    /**
     * Player wants to remove a player from their friend list
     * @param name The display name of the player to remove
     */
    open fun removeFriend(session: ClientSession, name: String) {}

    /**
     * Request to open a hyperlink
     * @param name Readable name
     * @param script Windows script name
     * @param third Unknown value
     */
    open fun hyperlink(session: ClientSession, name: String, script: String, third: Int) {}

    /**
     * Player wants to add a player to their ignore list
     * Note: temporary ignores optional after report abuse
     * @param name The display name of the player to add
     * @param temporary Whether the ignore will be removed after logout
     */
    open fun addIgnore(session: ClientSession, name: String, temporary: Boolean) {}

    /**
     * Player wants to remove a player from their ignore list
     * @param name The display name of the player to remove
     */
    open fun removeIgnore(session: ClientSession, name: String) {}

    /**
     * An integer entered in the entry box dialogue
     * @param integer The value entered
     */
    open fun integerEntered(session: ClientSession, integer: Int) {}

    /**
     * Notification that the player clicked an X button on a screen interface
     */
    open fun interfaceClosed(session: ClientSession) {}

    /**
     * Interface container action applied to a floor item
     * @param x The floor x coordinate
     * @param y The floor x coordinate
     * @param floorType The item type of the floor item
     * @param hash The interface id & component spell id hash
     * @param slot The interface item slot
     * @param run Force run
     * @param item The item type of the interface item
     */
    open fun interfaceOnFloorItem(session: ClientSession, x: Int, y: Int, floorType: Int, hash: Int, slot: Int, run: Boolean, item: Int) {}

    /**
     * Interface container action applied to another interface container
     * @param fromHash The first interface and component id combined
     * @param fromItem Item id of the first slot
     * @param from The slot being used
     * @param toHash The second interface and component id combined
     * @param toItem Item id of the second slot
     * @param to The slot being applied too
     */
    open fun interfaceOnInterface(session: ClientSession, fromHash: Int, fromItem: Int, from: Int, toHash: Int, toItem: Int, to: Int) {}

    /**
     * Interface container action applied to a npc
     * @param slot The interface item slot
     * @param type The interface item type
     * @param npc The npc client index
     * @param hash The interface and component id
     * @param run Force run
     */
    open fun interfaceOnNPC(session: ClientSession, slot: Int, type: Int, npc: Int, hash: Int, run: Boolean) {}

    /**
     * Interface container action applied to an object
     * @param run Force run
     * @param y The objects y coordinate
     * @param slot The interface item slot
     * @param hash The interface and component id
     * @param type The item id
     * @param y The objects y coordinate
     * @param id The objects id
     */
    open fun interfaceOnObject(session: ClientSession, run: Boolean, y: Int, slot: Int, hash: Int, type: Int, x: Int, id: Int) {}

    /**
     * Interface container action applied to a player
     * @param player The player index to apply on
     * @param hash The interface and component id
     * @param type The interface item type
     * @param run Force run
     * @param slot The component item slot
     */
    open fun interfaceOnPlayer(session: ClientSession, player: Int, hash: Int, type: Int, run: Boolean, slot: Int) {}

    /**
     * When a interface button is clicked directly or using a right click option choice
     * @param hash The interface and component id combined
     * @param itemId Optional starting slot index
     * @param itemSlot Optioning finishing slot index
     * @param option The menu option index
     */
    open fun interfaceOption(session: ClientSession, hash: Int, itemId: Int, itemSlot: Int, option: Int) {}

    /**
     * Action of one component dragged to another
     * @param toType The first item type
     * @param fromSlot The first item slot
     * @param fromType The second item type
     * @param fromHash The first interface and component ids hash
     * @param toSlot The second item slot
     * @param toHash The second interface and component ids hash
     */
    open fun interfaceSwitch(session: ClientSession, toType: Int, fromSlot: Int, fromType: Int, fromHash: Int, toSlot: Int, toHash: Int) {}

    /**
     * @param keys key's pressed - Pair<Key, Time>
     */
    open fun keysPressed(session: ClientSession, keys: List<Pair<Int, Int>>) {}

    open fun latency(session: ClientSession, value: Int) {}

    /**
     * Player has changed their online status while in the lobby
     * @param first Unknown
     * @param status The players online status
     * @param second Unknown
     */
    open fun lobbyOnlineStatus(session: ClientSession, first: Int, status: Int, second: Int) {}

    open fun cameraMoved(session: ClientSession, pitch: Int, yaw: Int) {}

    open fun mouseMoved(session: ClientSession) {}

    /**
     * An option selection on a npc
     * @param run Whether the player should force run
     * @param npcIndex The npc client index
     * @param option The option id - 2 = Attack, 6 = Examine
     */
    open fun npcOption(session: ClientSession, run: Boolean, npcIndex: Int, option: Int) {}

    /**
     * An option selection on an object
     * @param objectId The type id of the object selected
     * @param x The object's x coordinate
     * @param y The object's y coordinate
     * @param run Whether the player should force run
     * @param option The option id - 6 = Examine
     */
    open fun objectOption(session: ClientSession, objectId: Int, x: Int, y: Int, run: Boolean, option: Int) {}

    open fun ping(session: ClientSession) {}

    /**
     * The two values sent the client by packet 19
     */
    open fun pingReply(session: ClientSession, first: Int, second: Int) {}

    /**
     * An option selection on another player
     * @param index The selected player's index
     * @param optionIndex The option id - 3 = Trade, 4 = Attack
     */
    open fun playerOption(session: ClientSession, index: Int, optionIndex: Int) {}

    /**
     * Private message sent to another player
     * @param name The friends display name
     * @param message The message sent
     */
    open fun privateMessage(session: ClientSession, name: String, message: String) {}

    /**
     * Quick chat private message sent to another player
     * @param name The friends display name
     * @param file The quick chat file id
     * @param data Any additional display data required (skill levels etc...)
     */
    open fun privateQuickChat(session: ClientSession, name: String, file: Int, data: ByteArray) {}

    /**
     * Public chat message
     * @param message The message sent
     * @param effects The colour and move effect combined
     */
    open fun publicMessage(session: ClientSession, message: String, effects: Int) {}

    /**
     * Public quick chat message
     * @param script The quick chat script
     * @param file The quick chat file id
     * @param data Any additional display data required (skill levels etc...)
     */
    open fun publicQuickChat(session: ClientSession, script: Int, file: Int, data: ByteArray) {}

    open fun receiveCount(session: ClientSession, count: Int) {}

    open fun reflectionResponse(session: ClientSession) {}

    open fun regionLoaded(session: ClientSession) {}

    /**
     * When the players client is starting to load a region
     */
    open fun regionLoading(session: ClientSession) {}

    /**
     * Client report about another player
     * @param name The display name of the accused player
     * @param type The type of offence supposedly committed
     * @param integer Unknown
     * @param string Unknown
     */
    open fun reportAbuse(session: ClientSession, name: String, type: Int, integer: Int, string: String) {}

    /**
     * Called by script 580 - G.E item search clearing
     * @param value Unknown value
     */
    open fun resumeObjectDialogue(session: ClientSession, value: Int) {}

    /**
     * Notification that the player has changed their screen mode and might need a gameframe refresh
     * @param displayMode The client display mode
     * @param width The client window width
     * @param height The client window height
     * @param antialiasLevel The client antialias level
     */
    open fun changeScreen(session: ClientSession, displayMode: Int, width: Int, height: Int, antialiasLevel: Int) {}

    /**
     * Teleport request send when attempted using action 11 (unknown) and isn't a mod
     */
    open fun secondaryTeleport(session: ClientSession, x: Int, y: Int) {}

    open fun skillCapeColour(session: ClientSession, colour: Int) {}

    /**
     * Information entered in a enter string dialogue pop-up
     * @param text The string entered
     */
    open fun stringEntered(session: ClientSession, text: String) {}

    open fun toolkitPreferences(session: ClientSession) {}

    /**
     * @param value Some kind of colour value
     */
    open fun unknown(session: ClientSession, value: Int) {}

    /**
     * String from script 4701, game state must not be 7 and length must be less than or equal to 20
     * Might be kicking or banning from clan chat via interface
     * @param string Unknown value
     */
    open fun unknownScript(session: ClientSession, string: String) {}

    /**
     * Request for a player to move from current position to a new position on the map
     * @param x The target tile x coordinate
     * @param y The target tile y coordinate
     * @param running Whether the client is displaying the player as running
     */
    open fun walk(session: ClientSession, x: Int, y: Int, running: Boolean) {}

    /**
     * Request for player player to move from current position to a new position via mini-map
     * @param x The target tile x coordinate
     * @param y The target tile y coordinate
     * @param running Whether the client is displaying the player as running
     */
    open fun minimapWalk(session: ClientSession, x: Int, y: Int, running: Boolean) {}

    /**
     * A click on the game window
     * @param hash Hash of last time since last click (max 32767) & right click boolean (time | rightClick << 15)
     * @param position Position hash (x | y << 16)
     */
    open fun windowClick(session: ClientSession, hash: Int, position: Int) {}

    /**
     * Called when the client window changes status
     * @param focused Whether the client is focused or not
     */
    open fun windowFocus(session: ClientSession, focused: Boolean) {}

    /**
     * Called when the users mouse enters or exits the client area
     * @param over Whether the mouse is over the client or not
     */
    open fun windowHovered(session: ClientSession, over: Boolean) {}

    open fun refreshWorldList(session: ClientSession, full: Boolean) {}

    /**
     * Notification that the world map orb has been pressed
     */
    open fun closeWorldMap(session: ClientSession) {}

    open fun loginGame(session: ClientSession, username: String, password: String, isaacKeys: IntArray, mode: Int, width: Int, height: Int, antialias: Int, settings: String, affiliate: Int, sessionId: Int, os: Int, is64Bit: Int, versionType: Int, vendorType: Int, javaRelease: Int, javaVersion: Int, javaUpdate: Int, isUnsigned: Int, heapSize: Int, processorCount: Int, totalMemory: Int) {}

    open fun loginLobby(session: ClientSession, username: String, password: String, hd: Boolean, resize: Boolean, settings: String, affiliate: Int, isaacSeed: IntArray, crcMap: MutableMap<Int, Pair<Int, Int>>) {}

    open fun gameHandshake(session: ClientSession) {}

    open fun updateHandshake(session: ClientSession, version: Int) {}

    /**
     * @param id connection id
     */
    open fun updateConnection(session: ClientSession, id: Int) {}

    /**
     * @param id disconnect id
     */
    open fun updateDisconnect(session: ClientSession, id: Int) {}

    open fun updateLoginStatus(session: ClientSession, online: Boolean, value: Int) {}

    open fun updateRequest(session: ClientSession, indexId: Int, archiveId: Int, priority: Boolean) {}

}