package content.activity.evil_tree

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Neutral
import content.entity.player.dialogue.Quiz
import content.entity.player.dialogue.type.choice
import content.entity.player.dialogue.type.npc
import content.entity.player.dialogue.type.statement
import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItemLimit.removeToLimit
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit

class EvilTreeLeprechaun : Script {

    private val logger = InlineLogger()

    init {
        npcOperate("Talk-to", "leprechaun_evil_tree,leprechaun_panic") {
            npc<Happy>("leprechaun_evil_tree", "Top o' the mornin' to yez! Terrible business, this evil tree.")
            menu()
        }

        objectOperate("Take-rewards", "evil_tree_*_stump_reward") {
            claim()
        }
    }

    private suspend fun Player.menu() {
        choice("What would you like to say?") {
            option<Quiz>("What is this evil tree?") {
                npc<Neutral>("leprechaun_evil_tree", "'Tis a tree grown from the taint in the land. Chop it, burn it, do whatever yez can - just be rid of it.")
                npc<Neutral>("leprechaun_evil_tree", "Chop the roots for kindling, then set the kindling alight against the trunk. The fires burn it far faster than any hatchet.")
                menu()
            }
            option<Quiz>("Could I borrow some tools?") {
                loan()
                menu()
            }
            option<Quiz>("I'd like my reward.") {
                claim()
            }
            option<Neutral>("Nothing, thanks.")
        }
    }

    private suspend fun Player.loan() {
        val missing = TOOLS.filterNot { inventory.contains(it) }
        if (missing.isEmpty()) {
            npc<Neutral>("leprechaun_evil_tree", "Yez've got everythin' yez need already.")
            return
        }
        if (inventory.spaces < missing.size) {
            npc<Neutral>("leprechaun_evil_tree", "Yez've no room for anythin' else.")
            return
        }
        for (tool in missing) {
            inventory.transaction { add(tool) }
        }
        npc<Happy>("leprechaun_evil_tree", "Here yez go - mind yez don't lose 'em.")
        statement("The leprechaun hands you a ${missing.joinToString(" and a ") { it.toLowerSpaceCase() }}.")
    }

    private suspend fun Player.claim() {
        val state = EvilTreeState
        if (!state.active) {
            npc<Neutral>("leprechaun_evil_tree", "There's no evil tree about just now.")
            return
        }
        val row = Rows.get("evil_tree_type.${state.type}")
        if (!has(Skill.Woodcutting, row.int("woodcutting"), message = true)) {
            return
        }
        val limit = Settings["events.evilTree.dailyKindlingLimit", 200]
        resetDaily()
        val handIn = minOf(inventory.count("evil_tree_kindling"), limit - this["evil_tree_kindling_handed", 0])
        if (handIn <= 0) {
            npc<Neutral>("leprechaun_evil_tree", "Yez've no kindling for me to be tradin' today.")
            return
        }
        val scale = handIn.toDouble() / limit
        val index = random.nextInt(row.intList("reward_amounts").size)
        val logs = "${row.itemList("reward_logs")[index]}_noted"
        val amount = (row.intList("reward_amounts")[index] * scale).toInt()
        val coins = (row.int("reward_coins") * scale).toInt()
        inventory.transaction {
            removeToLimit("evil_tree_kindling", handIn)
            if (coins > 0) {
                add("coins", coins)
            }
            if (amount > 0) {
                add(logs, amount)
            }
        }
        if (inventory.transaction.error != TransactionError.None) {
            logger.warn { "Issue rewarding evil tree kindling $handIn" }
            statement("You don't have enough inventory space to claim your reward.")
            return
        }
        inc("evil_tree_kindling_handed", handIn)
        this["evil_tree_rewards"] = false
        AuditLog.event(this, "evil_tree_reward", state.type, handIn)
        grantMagic(row.int("buff"), scale)
        statement("You hand over $handIn kindling and receive your reward.")
    }

    private fun Player.grantMagic(minutes: Int, scale: Double) {
        val seconds = (TimeUnit.MINUTES.toSeconds(minutes.toLong()) * scale).toInt()
        if (seconds <= 0) {
            return
        }
        inc("evil_tree_buff", seconds)
        timers.startIfAbsent("evil_tree_buff")
        message("You feel the evil tree's magic settle on you.")
    }

    companion object {
        val TOOLS = listOf("bronze_hatchet", "tinderbox")
    }
}

/**
 * Rolls the players daily evil tree counters over when the day changes.
 */
fun Player.resetDaily() {
    val day = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())
    if (this["evil_tree_day", -1L] == day) {
        return
    }
    this["evil_tree_day"] = day
    this["evil_tree_trees"] = 0
    this["evil_tree_kindling_handed"] = 0
    this["evil_tree_spawn_id"] = 0
}

/**
 * Whether the player is allowed to interact with the current evil tree, counting it
 * towards their daily limit the first time they do.
 */
fun Player.interact(): Boolean {
    resetDaily()
    if (this["evil_tree_spawn_id", 0] == EvilTreeState.spawnId) {
        return true
    }
    if (this["evil_tree_trees", 0] >= Settings["events.evilTree.dailyTreeLimit", 2]) {
        message("You've already helped with as many evil trees as you can today.")
        return false
    }
    this["evil_tree_spawn_id"] = EvilTreeState.spawnId
    inc("evil_tree_trees")
    this["evil_tree_rewards"] = true
    return true
}
