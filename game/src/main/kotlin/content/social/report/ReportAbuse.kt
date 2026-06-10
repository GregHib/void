package content.social.report

import content.social.chat.ChatHistory
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.hasMenuOpen
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.AbuseReport
import world.gregs.voidps.engine.data.Reports
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.PlayerRights
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.hasRights
import world.gregs.voidps.engine.entity.character.player.isMod
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.client.instruction.ReportAbuse

class ReportAbuse(val reports: Reports, val accounts: AccountDefinitions) : Script {

    init {
        interfaceOption("Report Abuse", "filter_buttons:report") {
            openReportAbuse(this)
        }

        // Chat line ops are set at runtime by client scripts over placeholder strings in the cache,
        // so the option resolves to "8" rather than "Report abuse"; match by index instead.
        // The client keeps the reported name in a varc string for the report abuse interface.
        interfaceOption("*", "chat_background:chat_line*") {
            if (it.optionIndex != 7) {
                return@interfaceOption
            }
            openReportAbuse(this)
        }

        interfaceOption("Report Abuse", "private_chat:line*") {
            openReportAbuse(this)
        }

        interfaceOpened("report_abuse") {
            if (hasRights(PlayerRights.Mod)) {
                interfaces.sendVisibility("report_abuse", "mute_confirm", true)
                interfaces.sendVisibility("report_abuse", "mute_entry", true)
                interfaces.sendVisibility("report_abuse", "mute_select", true)
            }
        }

        instruction<ReportAbuse> { player ->
            val rule = Rule.byId(type) ?: return@instruction
            val name = name.trim()
            if (name.isEmpty()) {
                return@instruction
            }
            if (name.equals(player.name, true)) {
                player.message("You can't report yourself.")
                return@instruction
            }
            if (player.hasClock("report_abuse_delay")) {
                player.message("You can only submit one report per minute.")
                return@instruction
            }
            val target = Players.find(name)
            val account = target?.accountName ?: accounts.get(name)?.accountName
            if (account == null) {
                player.message("Unable to find player '$name'.")
                return@instruction
            }
            player.start("report_abuse_delay", 100)
            val muted = mute != 0 && player.hasRights(PlayerRights.Mod)
            reports.queue(
                AbuseReport(
                    reporter = player.accountName,
                    reported = name,
                    rule = rule.id,
                    ruleName = rule.title,
                    mute = muted,
                    suggestion = suggestion,
                    time = System.currentTimeMillis(),
                    evidence = ChatHistory.recent(account),
                ),
            )
            AuditLog.event(player, "report_abuse", target ?: name, rule.name, muted)
            if (muted) {
                target?.mute(rule = rule)
            }
            for (mod in Players) {
                if (mod.isMod()) {
                    mod.message("${player.name} reported $name for ${rule.title}.")
                }
            }
            player.message("Thank-you, your abuse report has been received.")
        }
    }

    fun openReportAbuse(player: Player) {
        if (player.hasMenuOpen()) {
            player.message("Please finish what you're doing first.")
            return
        }
        player.open("report_abuse")
    }
}
