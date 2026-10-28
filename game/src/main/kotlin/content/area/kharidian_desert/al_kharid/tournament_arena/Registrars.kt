package content.area.kharidian_desert.al_kharid.tournament_arena

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.ui.close
import world.gregs.voidps.engine.client.ui.open
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.timer.Timer
import world.gregs.voidps.engine.timer.toTicks
import java.util.concurrent.TimeUnit

class Registrars : Script {

    init {
        npcSpawn("registrar_*") {
            softTimers.start("registrar_overhead")
        }

        npcTimerStart("registrar_overhead") {
            TimeUnit.SECONDS.toTicks(10)
        }

        npcTimerTick("registrar_overhead") {
            say("This registration office is closed.")
            Timer.CONTINUE
        }

        npcOperate("Talk-to", "registrar_*,head_registrar") {
            open(INTERFACE)
            refresh()
        }

        interfaceOption("Close", "$INTERFACE:close") {
            close(INTERFACE)
        }
    }

    companion object {
        private const val INTERFACE = "tournament_arena_registrar"

    private fun Player.refresh() {
        interfaces.sendText(INTERFACE, "tournament_value", "20")
        interfaces.sendText(INTERFACE, "entry_fee", "6000 Coins")
        interfaces.sendText(INTERFACE, "potions", yesNo(true))
        interfaces.sendText(INTERFACE, "food", yesNo(true))
        interfaces.sendText(INTERFACE, "prayer", yesNo(true))
        interfaces.sendText(INTERFACE, "special_attacks", yesNo(true))
        interfaces.sendText(INTERFACE, "obstacles", yesNo(false))
        interfaces.sendText(INTERFACE, "ranged", yesNo(true))
        interfaces.sendText(INTERFACE, "melee", yesNo(true))
        interfaces.sendText(INTERFACE, "magic", yesNo(true))
        interfaces.sendText(INTERFACE, "heal_between_rounds", yesNo(false))
        interfaces.sendText(INTERFACE, "weapon_slots_only", yesNo(false))
        interfaces.sendText(INTERFACE, "summoning", yesNo(true))
        interfaces.sendVisibility(INTERFACE, "rank_required_label", false)
        interfaces.sendVisibility(INTERFACE, "rank_required", false)
        interfaces.sendVisibility(INTERFACE, "entry_fee_label", true)
        interfaces.sendVisibility(INTERFACE, "entry_fee", true)
        interfaces.sendVisibility(INTERFACE, "resign", false)
    }

    private fun yesNo(enabled: Boolean): String = if (enabled) "Yes" else "No"
}

private data class TournamentRegistrarRules(
    val tournamentValue: String,
    val entryFee: String,
    val potions: Boolean,
    val food: Boolean,
    val prayer: Boolean,
    val specialAttacks: Boolean,
    val obstacles: Boolean,
    val ranged: Boolean,
    val melee: Boolean,
    val magic: Boolean,
    val healBetweenRounds: Boolean,
    val weaponSlotsOnly: Boolean,
    val summoning: Boolean,
)
