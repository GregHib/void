package content.skill.prayer

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasMax
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class SpiritShieldBlessing : Script {

    init {
        itemOnObjectOperate("holy_elixir,spirit_shield", SARADOMIN_ALTARS) {
            bless()
        }
    }

    private fun Player.bless() {
        if (!inventory.contains("holy_elixir") || !inventory.contains("spirit_shield")) {
            message("You need a spirit shield and a holy elixir to create a blessed spirit shield.")
            return
        }
        if (!hasMax(Skill.Prayer, 85)) {
            message("You need a Prayer level of 85 to bless a spirit shield.")
            return
        }
        val success = inventory.transaction {
            remove("holy_elixir")
            remove("spirit_shield")
            add("blessed_spirit_shield")
        }
        if (!success) {
            return
        }
        anim("altar_pray")
        exp(Skill.Prayer, 1500.0)
        message("You bless the spirit shield.")
    }

    private companion object {
        private const val SARADOMIN_ALTARS = "prayer_altar_normal,prayer_altar_edgeville,prayer_altar_well_of_voyage,prayer_altar_entrana*,prayer_altar_witchaven,prayer_altar_camelot,prayer_altar_varrock_church,prayer_altar_duel_arena,prayer_altar_ardougne,prayer_altar_lumbridge*,prayer_altar_port_sarim_church,prayer_altar_*_saradomin,prayer_altar_*_saradomin_offer"
    }
}
