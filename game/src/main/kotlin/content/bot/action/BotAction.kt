package content.bot.action

import content.bot.Bot
import content.bot.bot
import content.bot.interact.navigation.navigate
import content.bot.interact.navigation.updateGraph
import content.bot.interact.path.AreaStrategy
import content.bot.interact.path.Dijkstra
import content.bot.interact.path.EdgeTraversal
import content.entity.Movement
import content.entity.combat.attackers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.InterfaceDefinitions
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.mode.interact.PlayerOnObjectInteract
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.event.wildcardEquals
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.map.Spiral
import world.gregs.voidps.network.client.instruction.InteractInterface
import world.gregs.voidps.network.client.instruction.InteractNPC
import world.gregs.voidps.network.client.instruction.Walk
import world.gregs.voidps.type.random

sealed interface BotAction {
    fun start(bot: Bot): BehaviourState = BehaviourState.Running
    fun update(bot: Bot): BehaviourState = BehaviourState.Running

    sealed class RetryableAction : BotAction {
        abstract val retryTicks: Int
        abstract val retryMax: Int
    }

    data class Clone(val id: String) : BotAction {
        override fun start(bot: Bot) = BehaviourState.Failed(Reason.Cancelled)
        override fun update(bot: Bot) = BehaviourState.Failed(Reason.Cancelled)
    }
    data class Reference(val action: BotAction, val references: Map<String, String>) : BotAction {
        override fun start(bot: Bot) = BehaviourState.Failed(Reason.Cancelled)
        override fun update(bot: Bot) = BehaviourState.Failed(Reason.Cancelled)
    }

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
            // TODO new format for nav graph
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
        override fun update(bot: Bot): BehaviourState {
            if (bot.mode is PlayerOnObjectInteract) {
                return BehaviourState.Running
            }
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
            bot.player.instructions.trySend(world.gregs.voidps.network.client.instruction.InteractObject(obj.intId, obj.x, obj.y, index + 1))
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

    data class WalkTo(val x: Int, val y: Int): BotAction {
        override fun start(bot: Bot): BehaviourState {
            bot.player.instructions.trySend(Walk(x, y))
            return BehaviourState.Success
        }
    }

    /**
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