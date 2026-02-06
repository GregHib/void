package content.bot.action

import content.bot.Bot
import content.bot.BotManager
import content.bot.fact.Condition
import content.bot.interact.path.Graph
import content.entity.combat.attackers
import content.entity.combat.dead
import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.client.instruction.InstructionHandlers
import world.gregs.voidps.engine.client.instruction.InterfaceHandler
import world.gregs.voidps.engine.client.instruction.handle.ObjectOptionHandler
import world.gregs.voidps.engine.client.ui.menu
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.mode.EmptyMode
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnFloorItemInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnNPCInteract
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.*

sealed interface BotAction {
    fun start(bot: Bot, frame: BehaviourFrame): BehaviourState = BehaviourState.Running
    fun update(bot: Bot, frame: BehaviourFrame): BehaviourState? = null

    data class Clone(val id: String) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame) = BehaviourState.Failed(Reason.Cancelled)
        override fun update(bot: Bot, frame: BehaviourFrame) = BehaviourState.Failed(Reason.Cancelled)
    }

    data class Reference(val action: BotAction, val references: Map<String, String>) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame) = BehaviourState.Failed(Reason.Cancelled)
        override fun update(bot: Bot, frame: BehaviourFrame) = BehaviourState.Failed(Reason.Cancelled)
    }

    data class Wait(val ticks: Int, val state: BehaviourState = BehaviourState.Success) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame) = BehaviourState.Wait(ticks, state)
    }

    data class GoTo(val target: String) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            val def = Areas.getOrNull(target) ?: return BehaviourState.Failed(Reason.Invalid("No areas found with id '$target'."))
            if (bot.tile in def.area) {
                return BehaviourState.Success
            }
            return BehaviourState.Running
        }

        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState {
            if (bot.steps.isNotEmpty()) {
                return BehaviourState.Running
            }
            val def = Areas.getOrNull(target) ?: return BehaviourState.Failed(Reason.Invalid("No areas found with id '$target'."))
            if (bot.tile in def.area) {
                return BehaviourState.Success
            }

            val manager = get<BotManager>()
            val list = mutableListOf<Int>()
            val graph = manager.graph
            val success = graph.find(bot.player, list, target)
            return queueRoute(success, list, graph, bot, target)
        }

        companion object {
            internal fun queueRoute(success: Boolean, list: MutableList<Int>, graph: Graph, bot: Bot, target: String): BehaviourState {
                if (!success) {
                    return BehaviourState.Failed(Reason.NoRoute)
                }
                val actions = mutableListOf<BotAction>()
                var nav: NavigationShortcut? = null
                for (edge in list) {
                    val shortcut = graph.shortcuts[edge]
                    if (shortcut != null) {
                        nav = shortcut
                    } else {
                        actions.addAll(graph.actions[edge] ?: continue)
                    }
                }
                if (actions.isNotEmpty()) {
                    bot.queue(BehaviourFrame(Resolver("go_to_${target}", 0, actions = actions)))
                }
                if (nav != null) {
                    bot.queue(BehaviourFrame(nav))
                }
                if (bot.frames.isEmpty()) {
                    return BehaviourState.Failed(Reason.NoRoute)
                }
                return BehaviourState.Running
            }
        }
    }

    data class GoToNearest(val tag: String) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            val set = Areas.tagged(tag)
            if (set.isEmpty()) {
                return BehaviourState.Failed(Reason.Invalid("No areas tagged with tag '$tag'."))
            }
            if (set.any { bot.tile in it.area }) {
                return BehaviourState.Success
            }
            return BehaviourState.Running
        }

        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState {
            if (bot.steps.isNotEmpty()) {
                return BehaviourState.Running
            }
            val set = Areas.tagged(tag)
            if (set.isEmpty()) {
                return BehaviourState.Failed(Reason.Invalid("No areas tagged with tag '$tag'."))
            }
            if (set.any { bot.tile in it.area }) {
                return BehaviourState.Success
            }
            val manager = get<BotManager>()
            val list = mutableListOf<Int>()
            val graph = manager.graph
            val success = graph.findNearest(bot.player, list, tag)
            return GoTo.queueRoute(success, list, graph, bot, tag)
        }
    }

    data class InteractNpc(
        val option: String,
        val id: String,
        val delay: Int = 0,
        val success: Condition? = null,
        val radius: Int = 10,
    ) : BotAction {

        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            frame.timeout = 0
            return BehaviourState.Running
        }

        override fun update(bot: Bot, frame: BehaviourFrame) = when {
            success?.check(bot.player) == true -> BehaviourState.Success
            bot.mode is PlayerOnNPCInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
            bot.mode is EmptyMode -> search(bot)
            else -> null
        }

        private fun search(bot: Bot): BehaviourState {
            val player = bot.player
            val ids = if (id.contains(",")) id.split(",") else listOf(id)
            for (tile in Spiral.spiral(player.tile, radius)) {
                for (npc in NPCs.at(tile)) {
                    if (ids.none { wildcardEquals(it, npc.id) }) {
                        continue
                    }
                    val index = npc.def(player).options.indexOf(option)
                    if (index == -1) {
                        continue
                    }
                    val valid = get<InstructionHandlers>().handle(bot.player, InteractNPC(npc.index, index + 1))
                    if (!valid) {
                        return BehaviourState.Failed(Reason.Invalid("Invalid npc interaction: ${npc.index} ${index + 1}"))
                    }
                    return BehaviourState.Running
                }
            }
            return handleNoTarget()
        }

        private fun handleNoTarget(): BehaviourState {
            if (success == null) {
                return BehaviourState.Failed(Reason.NoTarget)
            }
            if (delay > 0) {
                return BehaviourState.Wait(delay, BehaviourState.Running)
            }
            return BehaviourState.Running
        }
    }

    data class FightNpc(
        val id: String,
        val delay: Int = 0,
        val success: Condition? = null,
        val radius: Int = 10,
        val healPercentage: Int = 20,
        val lootOverValue: Int = 0,
    ) : BotAction {

        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            frame.timeout = 0
            return BehaviourState.Running
        }

        override fun update(bot: Bot, frame: BehaviourFrame) = when {
            healPercentage > 0 && bot.levels.get(Skill.Constitution) <= bot.levels.getMax(Skill.Constitution) / healPercentage -> eat(bot)
            success?.check(bot.player) == true -> BehaviourState.Success
            bot.mode is PlayerOnNPCInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
            bot.mode is PlayerOnFloorItemInteract -> BehaviourState.Running
            bot.mode is EmptyMode -> search(bot)
            else -> null
        }

        private fun eat(bot: Bot): BehaviourState {
            val inventory = bot.player.inventory
            for (index in inventory.indices) {
                val item = inventory[index]
                val option = item.def.options.indexOf("Eat")
                if (option == -1) {
                    continue
                }
                val valid = get<InstructionHandlers>().handle(bot.player, InteractInterface(149, 0, item.def.id, index, option))
                if (!valid) {
                    return BehaviourState.Failed(Reason.Invalid("Invalid inventory interaction: ${item.def.id} $index $option"))
                }
                return BehaviourState.Wait(1, BehaviourState.Running)
            }
            return BehaviourState.Running
        }

        private fun search(bot: Bot): BehaviourState {
            val player = bot.player
            for (tile in Spiral.spiral(player.tile, radius)) {
                for (item in FloorItems.at(tile)) {
                    if (item.owner != player.accountName) {
                        continue
                    }
                    if (item.def.cost <= lootOverValue) {
                        continue
                    }
                    val index = item.def.floorOptions.indexOf("Take")
                    val valid = get<InstructionHandlers>().handle(bot.player, InteractFloorItem(item.def.id, item.tile.x, item.tile.y, index))
                    if (!valid) {
                        return BehaviourState.Failed(Reason.Invalid("Invalid floor item interaction: $item $index"))
                    }
                    return BehaviourState.Running
                }
                for (npc in NPCs.at(tile)) {
                    if (!wildcardEquals(id, npc.id)) {
                        continue
                    }
                    val index = npc.def(player).options.indexOf("Attack")
                    if (index == -1) {
                        continue
                    }
                    if (npc.dead || npc.attackers.isNotEmpty() && !npc.attackers.contains(player)) {
                        continue
                    }
                    val valid = get<InstructionHandlers>().handle(bot.player, InteractNPC(npc.index, index + 1))
                    if (!valid) {
                        return BehaviourState.Failed(Reason.Invalid("Invalid npc interaction: ${npc.index} ${index + 1}"))
                    }
                    return BehaviourState.Running
                }
            }
            return handleNoTarget()
        }

        private fun handleNoTarget(): BehaviourState {
            if (success == null) {
                return BehaviourState.Failed(Reason.NoTarget)
            }
            if (delay > 0) {
                return BehaviourState.Wait(delay, BehaviourState.Running)
            }
            return BehaviourState.Running
        }
    }

    data class InteractObject(
        val option: String,
        val id: String,
        val delay: Int = 0,
        val success: Condition? = null,
        val radius: Int = 10,
    ) : BotAction {

        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            frame.timeout = 0
            return BehaviourState.Running
        }

        override fun update(bot: Bot, frame: BehaviourFrame) = when {
            success?.check(bot.player) == true -> BehaviourState.Success
            bot.mode is PlayerOnObjectInteract -> if (success == null) BehaviourState.Success else BehaviourState.Running
            bot.mode is EmptyMode -> search(bot)
            else -> null
        }

        private fun search(bot: Bot): BehaviourState {
            val player = bot.player
            for (tile in Spiral.spiral(player.tile, radius)) {
                for (obj in GameObjects.at(tile)) {
                    if (!wildcardEquals(id, obj.id)) {
                        continue
                    }
                    val index = obj.def(player).options?.indexOf(option)
                    if (index == null || index == -1) {
                        continue
                    }
                    val valid = get<InstructionHandlers>().handle(bot.player, InteractObject(obj.intId, obj.x, obj.y, index + 1))
                    if (!valid) {
                        return BehaviourState.Failed(Reason.Invalid("Invalid object interaction: $obj ${index + 1}"))
                    }
                    return BehaviourState.Running
                }
            }
            return handleNoTarget()
        }

        private fun handleNoTarget(): BehaviourState {
            if (success == null) {
                return BehaviourState.Failed(Reason.NoTarget)
            }
            if (delay > 0) {
                return BehaviourState.Wait(delay, BehaviourState.Running)
            }
            return BehaviourState.Running
        }
    }

    data class ItemOnItem(val item: String, val on: String, val success: Condition? = null) : BotAction {
        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState? {
            if (success != null && success.check(bot.player)) {
                return BehaviourState.Success
            }
            val inventory = bot.player.inventory
            val fromSlot = inventory.indexOf(item)
            if (fromSlot == -1) {
                return BehaviourState.Failed(Reason.Invalid("No inventory item '$item'."))
            }
            val toSlot = inventory.indexOf(on)
            if (toSlot == -1) {
                return BehaviourState.Failed(Reason.Invalid("No inventory item '$on'."))
            }
            val from = inventory[fromSlot]
            val to = inventory[toSlot]
            val valid = get<InstructionHandlers>().handle(bot.player, InteractInterfaceItem(from.def.id, to.def.id, fromSlot, toSlot, 149, 0, 149, 0))
            return when {
                !valid -> BehaviourState.Failed(Reason.Invalid("Invalid item on item: ${from.def.id}:${fromSlot} -> ${to.def.id}:${toSlot}."))
                success == null -> BehaviourState.Wait(1, BehaviourState.Success)
                success.check(bot.player) -> BehaviourState.Success
                else -> BehaviourState.Running
            }
        }
    }

    data class ItemOnObject(val item: String, val id: String, val success: Condition? = null) : BotAction {
        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState {
            if (success != null && success.check(bot.player)) {
                return BehaviourState.Success
            }
            val inventory = bot.player.inventory
            val slot = inventory.indexOf(this@ItemOnObject.item)
            if (slot == -1) {
                return BehaviourState.Failed(Reason.Invalid("No inventory item '${this@ItemOnObject.item}'."))
            }
            val item = inventory[slot]
            return search(bot, item, slot)
        }

        private fun search(bot: Bot, item: Item, slot: Int): BehaviourState {
            val player = bot.player
            val ids = if (id.contains(",")) id.split(",") else listOf(id)
            for (tile in Spiral.spiral(player.tile, 10)) {
                for (obj in GameObjects.at(tile)) {
                    if (ids.none { wildcardEquals(it, obj.id) }) {
                        continue
                    }
                    val valid = get<InstructionHandlers>().handle(bot.player, InteractInterfaceObject(obj.intId, obj.x, obj.y, 149, 0, item.def.id, slot))
                    if (!valid) {
                        return BehaviourState.Failed(Reason.Invalid("Invalid item on object: ${item.def.id}:${slot} -> ${obj}."))
                    }
                    return BehaviourState.Running
                }
            }
            if (success == null) {
                return BehaviourState.Failed(Reason.NoTarget)
            }
            if (success.check(bot.player)) {
                return BehaviourState.Success
            }
            return BehaviourState.Running
        }
    }

    data class InterfaceOption(val option: String, val id: String, val success: Condition? = null) : BotAction {
        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState? {
            if (success != null && success.check(bot.player)) {
                return BehaviourState.Success
            }
            val definitions = get<InterfaceDefinitions>()
            val split = id.split(":")
            if (split.size < 2) {
                return BehaviourState.Failed(Reason.Invalid("Invalid interface id '$id'."))
            }
            val (id, component) = split
            val item = split.getOrNull(2)
            val def = definitions.getOrNull(id) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface id $id:${component}:${item}."))
            val componentId = definitions.getComponentId(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component $id:${component}:${item}."))
            val componentDef = definitions.getComponent(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component definition $id:${component}:${item}."))
            var options = componentDef.options
            if (options == null) {
                options = componentDef.getOrNull("options") ?: emptyArray()
            }
            val index = options.indexOf(option)
            if (index == -1) {
                return BehaviourState.Failed(Reason.Invalid("No interface option $option for $id:$component:${item} options=${options.contentToString()}."))
            }
            val itemDef = if (item != null) ItemDefinitions.getOrNull(item) else null

            val inv = InterfaceHandler.getInventory(bot.player, id, component, componentDef)
            var itemSlot = if (item != null && inv != null) bot.player.inventories.inventory(inv).indexOf(item) else -1
            if (id == "shop") {
                itemSlot *= 6
            }
            val valid = get<InstructionHandlers>().handle(
                bot.player, InteractInterface(
                    interfaceId = def.id,
                    componentId = componentId,
                    itemId = itemDef?.id ?: -1,
                    itemSlot = itemSlot,
                    option = index
                )
            )
            return when {
                !valid -> BehaviourState.Failed(Reason.Invalid("Invalid interaction: ${def.id}:${componentId}:${itemDef?.id} slot $itemSlot option ${index}."))
                success == null -> BehaviourState.Wait(1, BehaviourState.Success)
                success.check(bot.player) -> BehaviourState.Success
                else -> BehaviourState.Running
            }
        }
    }

    data class DialogueContinue(val option: String, val id: String, val success: Condition? = null) : BotAction {
        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState {
            if (success != null && success.check(bot.player)) {
                return BehaviourState.Success
            }
            val definitions = get<InterfaceDefinitions>()
            val split = id.split(":")
            if (split.size < 2) {
                return BehaviourState.Failed(Reason.Invalid("Invalid interface id '$id'."))
            }
            val (id, component) = split
            val item = split.getOrNull(2)
            val def = definitions.getOrNull(id) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface id $id:${component}:${item}."))
            val componentId = definitions.getComponentId(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component $id:${component}:${item}."))
            val componentDef = definitions.getComponent(id, component) ?: return BehaviourState.Failed(Reason.Invalid("Invalid interface component definition $id:${component}:${item}."))
            var options = componentDef.options
            if (options == null) {
                options = componentDef.getOrNull("options") ?: emptyArray()
            }
            val index = options.indexOf(option)
            val valid = get<InstructionHandlers>().handle(
                bot.player, InteractDialogue(
                    interfaceId = def.id,
                    componentId = componentId,
                    option = index
                )
            )
            if (!valid) {
                return BehaviourState.Failed(Reason.Invalid("Invalid interaction: ${def.id}:${componentId} option=${index}."))
            }
            return BehaviourState.Wait(1, BehaviourState.Success)
        }
    }

    data class IntEntry(val value: Int) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            bot.player.instructions.trySend(EnterInt(value))
            return BehaviourState.Wait(1, BehaviourState.Success)
        }
    }

    object CloseInterface : BotAction {
        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState {
            if (bot.player.menu == null) {
                return BehaviourState.Success
            }
            if (get<InstructionHandlers>().handle(bot.player, InterfaceClosedInstruction)) {
                return BehaviourState.Success
            }
            return BehaviourState.Failed(Reason.NoTarget)
        }
    }

    /**
     * Restarts the current action when [check] doesn't hold true (or bot has no mode) and success state isn't matched.
     */
    data class Restart(
        val wait: List<Condition>,
        val success: Condition,
    ) : BotAction {
        override fun update(bot: Bot, frame: BehaviourFrame): BehaviourState {
            if (success.check(bot.player)) {
                return BehaviourState.Success
            }
            if (wait.isEmpty() && bot.mode !is EmptyMode) {
                return BehaviourState.Running
            } else if (wait.any { it.check(bot.player) }) {
                return BehaviourState.Running
            }
            frame.index = 0
            return BehaviourState.Running
        }
    }

    data class StringEntry(val value: String) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            bot.player.instructions.trySend(EnterString(value))
            return BehaviourState.Wait(1, BehaviourState.Success)
        }
    }

    data class WalkTo(val x: Int, val y: Int, val radius: Int = 4) : BotAction {
        override fun start(bot: Bot, frame: BehaviourFrame): BehaviourState {
            bot.player.instructions.trySend(Walk(x, y))
            return BehaviourState.Running
        }

        override fun update(bot: Bot, frame: BehaviourFrame) = when {
            bot.tile.within(x, y, bot.tile.level, radius) -> BehaviourState.Success
            bot.mode is EmptyMode && GameLoop.tick - bot.steps.last > 10 -> BehaviourState.Failed(Reason.Stuck)
            else -> BehaviourState.Running
        }
    }

    /**
     * TODO
     *      firemaking bot
     *      cooking bot
     *      rune mysteries quest bot
     *      bot saving?
     *      bot setups
     *      bot spawning in other locations
     *
     *  TODO need a better way of managing inventory. Removing unneeded items in favour of required ones vs emptying full inventory every time etc...
     *
    TODO how to handle repeat actions e.g. repeat Chop-down trees until inv is full - These are actions Gathering, Skilling etc..
    more resolvers like bank all, drop cheap items
    how to handle combat, one task or multiple? - One Fight action
    frames should have tick(): State methods
    Combat should be an action which has a state machine for eating, retargeting, looting etc..
    GatheringActivity
    TravelActivity
    how to handle navigation in a non-hacky way
    navigation behaviours
    make nav-graph points only?
    combine nav-graph requirements with facts
    Goal generators
    Rather than check all req for all activities do it reactively
    Received an item recently? Add relevant activities to that item to the list of posibilities
    Been too long since you picked up an item, now remove that goal from the list
    No possibilities? Now expand search wider

    Open questions:
    - Complex activities like minigames, quests
    Minigames:
    They are closed mechanical systems so they are actions.
    JoinMinigameLobby - Success, Timeout, Kicked etc..
    PlayMinigame - Roll selection, objectives, movement, combat, scoring. Fails on game end or leaving/disconnect
    Trading with players:
    Outcomes are non-deterministic, waiting on player timing
    SellingAction
    TradeAction
    inits trade
    trade rules
    max wait
    accepted items
    price bounds
    reacts to
    offer chances
    cancellation
    terminates with success/failure
    Quests:
    Some complex quest mechanics might need custom actions
    Activities:
    TalkToCook
    GetBucket
    GetMilk
    GetEgg
    ReturnToCook
    - navigation + actions
    Virtual nodes
    Create a temp node
    link the current tile as a weight = 0
    link any applicable teleports
    run traversal from temp source node
    - targeting
    policy
    - activity generators - reactive loading
    Separate mandatory and resolvable requirements
    mandatory requirements become gates for if activities are in the current pool
    Listeners wait for state changes on specific mandatory requirements (level up, skill changes) and revaluate adding to activity pool
    These listeners also check current activity requirements and fail it if no longer gated

     */
}
