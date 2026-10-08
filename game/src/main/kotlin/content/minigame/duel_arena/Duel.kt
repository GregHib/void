package content.minigame.duel_arena

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.inv.Inventory

enum class DuelStage {
    Rules,
    Confirm,
    Countdown,
    Fighting,
    Finished,
}

/**
 * Shared state for one duel, stored on both players as "duel".
 */
class Duel(
    val requester: Player,
    val acceptor: Player,
    val staked: Boolean,
) {
    val id: Int = ++counter
    val rules: MutableSet<String> = mutableSetOf()
    val accepted: MutableSet<String> = mutableSetOf()
    var stage: DuelStage = DuelStage.Rules

    val players: List<Player>
        get() = listOf(requester, acceptor)

    val screen: String
        get() = if (staked) "stake" else "duel_confirm"

    val confirmScreen: String
        get() = if (staked) "stake_confirm" else "duel_rules_confirm"

    val active: Boolean
        get() = stage == DuelStage.Countdown || stage == DuelStage.Fighting

    fun opponent(player: Player): Player = if (player === requester) acceptor else requester

    fun hasRule(rule: String): Boolean = rules.contains(rule)

    companion object {
        private var counter = 0
    }
}

val Player.duel: Duel?
    get() = get("duel")

val Player.stake: Inventory
    get() = inventories.inventory("dueloffer", false)

val Player.otherStake: Inventory
    get() = inventories.inventory("dueloffer", true)

val Player.winnings: Inventory
    get() = inventories.inventory("duelwinnings", false)
