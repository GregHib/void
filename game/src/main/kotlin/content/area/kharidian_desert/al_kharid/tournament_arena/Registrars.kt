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

        private val DEFAULT_RULES = TournamentRegistrarRules(
            tournamentValue = "20",
            entryFee = "6000 Coins",
            potions = true,
            food = true,
            prayer = true,
            specialAttacks = true,
            obstacles = false,
            ranged = true,
            melee = true,
            magic = true,
            healBetweenRounds = false,
            weaponSlotsOnly = false,
            summoning = true,
        )
    }

    private fun Player.refresh() {
        interfaces.sendText(INTERFACE, "tournament_value", DEFAULT_RULES.tournamentValue)
        interfaces.sendText(INTERFACE, "entry_fee", DEFAULT_RULES.entryFee)
        interfaces.sendText(INTERFACE, "potions", yesNo(DEFAULT_RULES.potions))
        interfaces.sendText(INTERFACE, "food", yesNo(DEFAULT_RULES.food))
        interfaces.sendText(INTERFACE, "prayer", yesNo(DEFAULT_RULES.prayer))
        interfaces.sendText(INTERFACE, "special_attacks", yesNo(DEFAULT_RULES.specialAttacks))
        interfaces.sendText(INTERFACE, "obstacles", yesNo(DEFAULT_RULES.obstacles))
        interfaces.sendText(INTERFACE, "ranged", yesNo(DEFAULT_RULES.ranged))
        interfaces.sendText(INTERFACE, "melee", yesNo(DEFAULT_RULES.melee))
        interfaces.sendText(INTERFACE, "magic", yesNo(DEFAULT_RULES.magic))
        interfaces.sendText(INTERFACE, "heal_between_rounds", yesNo(DEFAULT_RULES.healBetweenRounds))
        interfaces.sendText(INTERFACE, "weapon_slots_only", yesNo(DEFAULT_RULES.weaponSlotsOnly))
        interfaces.sendText(INTERFACE, "summoning", yesNo(DEFAULT_RULES.summoning))
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
