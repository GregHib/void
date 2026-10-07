package content.bot.behaviour.action

import content.bot.Bot
import content.bot.behaviour.BehaviourFrame
import content.bot.behaviour.BehaviourState
import content.bot.behaviour.BotWorld
import content.bot.behaviour.Reason
import content.bot.behaviour.condition.BotHasQueue
import content.bot.behaviour.condition.BotInterfaceOpen
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.setup.Resolver
import content.skill.fletching.fletchableProducts
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.inv.inventory

/** Fletches a batch, retrying interrupted fletching without returning to woodcutting. */
data class BotFletchLogs(val wood: String, val product: String) : BotAction {
    override fun update(bot: Bot, world: BotWorld, frame: BehaviourFrame): BehaviourState {
        val player = bot.player
        if (!player.inventory.contains(wood)) {
            return BehaviourState.Success
        }
        val choice = fletchableProducts(wood).indexOf(product) + 1
        if (choice == 0) {
            return BehaviourState.Failed(Reason.Invalid("Cannot fletch '$product' from '$wood'."))
        }
        val levelPath = if (product == "arrow_shaft") "arrow_shafts.$wood.level" else "fletching_unf.$product.level"
        val level = Tables.intOrNull(levelPath)
            ?: return BehaviourState.Failed(Reason.Invalid("No fletching level for '$product' from '$wood'."))
        if (player.levels.get(Skill.Fletching) < level) {
            return BehaviourState.Failed(Reason.Invalid("Fletching '$product' requires level $level."))
        }
        if (!player.inventory.contains("knife")) {
            return BehaviourState.Failed(Reason.Invalid("No inventory item 'knife'."))
        }
        if (player.queue.contains("fletching")) {
            return BehaviourState.Running
        }
        bot.queue(
            BehaviourFrame(
                Resolver(
                    id = "fletch_${wood}_$product",
                    weight = 0,
                    actions = listOf(
                        BotItemOnItem("knife", wood, BotInterfaceOpen("dialogue_skill_creation")),
                        BotInterfaceOption("All", "skill_creation_amount:all"),
                        BotDialogueContinue("", "dialogue_skill_creation:choice$choice"),
                        BotRestart(
                            wait = listOf(BotHasQueue("fletching")),
                            success = BotInventorySetup(listOf(BotItem(setOf(wood), max = 0))),
                        ),
                    ),
                    produces = setOf("item:$product", "skill:fletching"),
                ),
            ),
        )
        return BehaviourState.Running
    }
}
