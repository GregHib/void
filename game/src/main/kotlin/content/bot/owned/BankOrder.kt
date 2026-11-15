package content.bot.owned

/**
 * One thing an owned bot does at a bank booth on its owner's behalf; queued on the bot in `owned_errand`
 * and carried out in one trip by [content.bot.behaviour.action.BotOwnedErrand].
 */
data class BankOrder(val kind: Kind, val item: String = "", val amount: Int = 1) {
    enum class Kind { Withdraw, Deposit, DepositAll, Equip, TakeOff }

    fun describe(): String = when (kind) {
        Kind.Withdraw -> "withdraw ${amount.ifMany()}${item.replace('_', ' ')}"
        Kind.Deposit -> "bank ${amount.ifMany()}${item.replace('_', ' ')}"
        Kind.DepositAll -> "bank everything"
        Kind.Equip -> "wear ${item.replace('_', ' ')}"
        Kind.TakeOff -> "bank its $item"
    }

    private fun Int.ifMany(): String = if (this > 1) "$this x " else ""
}
