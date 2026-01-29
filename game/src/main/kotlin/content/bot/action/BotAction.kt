package content.bot.action

import content.bot.Bot
import content.bot.bot
import content.bot.interact.navigation.navigate
import content.bot.interact.navigation.updateGraph
import content.bot.interact.path.AreaStrategy
import content.bot.interact.path.Dijkstra
import content.bot.interact.path.EdgeTraversal
import content.entity.combat.attackers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.type.random

sealed interface BotAction {
    fun start(bot: Bot): BehaviourState = BehaviourState.Failed(Reason.Cancelled)
    fun update(): BehaviourState = BehaviourState.Running

    sealed class RetryableAction : BotAction {
        abstract val retryTicks: Int
        abstract val retryMax: Int
    }

    data class Clone(val id: String) : BotAction
    data class Reference(val action: BotAction, val references: Map<String, String>) : BotAction

    data class Wait(val ticks: Int) : BotAction {
        override fun start(bot: Bot): BehaviourState {
            bot.frame().state = BehaviourState.Wait(ticks)
            return BehaviourState.Running
        }
    }

    data class GoTo(val target: String) : BotAction {
        override fun start(bot: Bot): BehaviourState {
            val def = Areas.getOrNull(target) ?: return BehaviourState.Failed(Reason.NoRoute)
            if (bot.tile in def.area) {
                return BehaviourState.Success
            }
            updateGraph(bot)
            val strategy = AreaStrategy(def.area)
            val result = get<Dijkstra>().find(bot.player, strategy, EdgeTraversal())
            bot["navigating"] = result == null
            if (result != null) {
                bot["area"] = def
                GlobalScope.launch { bot.navigate() }
                return BehaviourState.Running
            } else {
                return BehaviourState.Failed(Reason.NoRoute)
            }
        }
    }

    data class InteractNpc(
        val option: String,
        val id: String,
        override val retryTicks: Int = 0,
        override val retryMax: Int = 0,
        val radius: Int = 10,
    ) : RetryableAction() {
        override fun start(bot: Bot): BehaviourState {
            val npcs = mutableListOf<NPC>()
            for (tile in Spiral.spiral(bot.player.tile, radius)) {
                for (npc in NPCs.at(tile)) {
                    if (!wildcardEquals(id, npc.id)) {
                        continue
                    }
                    if (option == "Attack" && npc.attackers.isNotEmpty() && !npc.attackers.contains(bot.player)) {
                        continue
                    }
                    npcs.add(npc)
                }
            }
            val npc = npcs.randomOrNull(random) ?: return BehaviourState.Failed(Reason.NoTarget)
            val index = npc.def(bot.player).options.indexOf(option)
            bot.player.instructions.trySend(InteractNPC(npc.index, index))
            return BehaviourState.Running
        }
    }

    data class InteractObject(
        val option: String,
        val id: String,
        override val retryTicks: Int = 0,
        override val retryMax: Int = 0,
        val radius: Int = 10,
    ) : RetryableAction() {
        override fun start(bot: Bot): BehaviourState {
            val objects = mutableListOf<GameObject>()
            for (tile in Spiral.spiral(bot.player.tile, radius)) {
                for (obj in GameObjects.at(tile)) {
                    if (wildcardEquals(id, obj.id)) {
                        objects.add(obj)
                    }
                }
            }
            val obj = objects.randomOrNull(random) ?: return BehaviourState.Failed(Reason.NoTarget)
            val index = obj.def(bot.player).options?.indexOf(option) ?: return BehaviourState.Failed(Reason.NoTarget)
            bot.player.instructions.trySend(world.gregs.voidps.network.client.instruction.InteractObject(obj.intId, obj.x, obj.y, index))
            return BehaviourState.Running
        }
    }

    data class InterfaceOption(val id: String, val option: String) : BotAction {
        override fun start(bot: Bot): BehaviourState {
            val definitions = get<InterfaceDefinitions>()
            val split = id.split(":")
            val (id, component) = split
            val item = split.getOrNull(2)
            val def = definitions.getOrNull(id) ?: return BehaviourState.Failed(Reason.NoTarget)
            val componentId = definitions.getComponentId(id, component) ?: return BehaviourState.Failed(Reason.NoTarget)
            val componentDef = definitions.getComponent(id, component) ?: return BehaviourState.Failed(Reason.NoTarget)
            val index = componentDef.options?.indexOf(option) ?: return BehaviourState.Failed(Reason.NoTarget)
            bot.player.instructions.trySend(
                InteractInterface(
                    interfaceId = def.id,
                    componentId = componentId,
                    itemId = -1,
                    itemSlot = -1,
                    option = index
                )
            ) // TODO could await actual response, or something to get actual feedback
            return BehaviourState.Success
        }
    }

    data class WaitFullInventory(val timeout: Int) : BotAction

    /**
     * TODO how to handle repeat actions e.g. repeat Chop-down trees until inv is full
     *      more resolvers like bank all, drop cheap items
     *      how to handle combat, one task or multiple?
     *          frames should have tick(): State methods
     *          Combat should be an action which has a state machine for eating, retargeting, looting etc..
     *          GatheringActivity
     *          TravelActivity
     *      how to handle navigation in a non-hacky way
     *          navigation behaviours
     *          make nav-graph points only?
     *          combine nav-graph requirements with facts
 *          Goal generators
     *         Rather than check all req for all activties do it reactively
     *         Received an item recently? Add relevant activties to that item to the list of posibilities
     *         Been too long since you picked up an item, now remove that goal from the list
     *         No posibilities? Now expand search wider
     */
}