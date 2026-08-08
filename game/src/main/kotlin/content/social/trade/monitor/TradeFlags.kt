package content.social.trade.monitor

import world.gregs.voidps.engine.GameLoop
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

/**
 * Flags suspicious player-to-player trades for moderator investigation:
 * single trades over [economy.monitor.trade.valueThreshold] coins and repeated
 * large trades between the same pair of accounts within a sliding window.
 * Flags are written to [AuditLog] only.
 */
object TradeFlags {

    private val pairs = mutableMapOf<String, ArrayDeque<Int>>()

    fun check(requester: Player, acceptor: Player, requesterValue: Long, acceptorValue: Long, tick: Int = GameLoop.tick) {
        if (!Settings["economy.monitor.enabled", false] || !Settings["economy.monitor.trade.enabled", false]) {
            return
        }
        val valueThreshold = Settings["economy.monitor.trade.valueThreshold", 10_000_000L]
        if (maxOf(requesterValue, acceptorValue) >= valueThreshold) {
            AuditLog.event(requester, "trade_flag_value", acceptor, requesterValue, acceptorValue)
            EconomySink.flag("trade_value", requester.accountName, acceptor.accountName, value = maxOf(requesterValue, acceptorValue), details = "requester=$requesterValue acceptor=$acceptorValue")
        }
        val pairThreshold = Settings["economy.monitor.trade.pair.valueThreshold", 1_000_000L]
        if (requesterValue + acceptorValue < pairThreshold) {
            return
        }
        val window = TimeUnit.MINUTES.toTicks(Settings["economy.monitor.trade.pair.windowMinutes", 60])
        val key = pairKey(requester, acceptor)
        val trades = pairs.getOrPut(key) { ArrayDeque() }
        while (trades.isNotEmpty() && tick - trades.first() > window) {
            trades.removeFirst()
        }
        trades.addLast(tick)
        if (trades.size >= Settings["economy.monitor.trade.pair.count", 5]) {
            AuditLog.event(requester, "trade_flag_pair", acceptor, trades.size, requesterValue + acceptorValue)
            EconomySink.flag("trade_pair", requester.accountName, acceptor.accountName, value = requesterValue + acceptorValue, details = "count=${trades.size}")
            pairs.remove(key)
        }
    }

    private fun pairKey(one: Player, two: Player): String = if (one.accountName < two.accountName) {
        "${one.accountName}|${two.accountName}"
    } else {
        "${two.accountName}|${one.accountName}"
    }

    fun clear() {
        pairs.clear()
    }
}
