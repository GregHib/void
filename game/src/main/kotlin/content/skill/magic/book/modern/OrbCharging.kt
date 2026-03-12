package content.skill.magic.book.modern

import content.entity.player.dialogue.type.makeAmount
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.an
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.queue.weakQueue

class OrbCharging : Script {
    init {
        onObjectOperate("modern_spellbook:charge_*_orb") { (target, id) ->
            val type = id.substringAfter("charge_").removeSuffix("_orb")
            if (!target.id.startsWith("obelisk_of_$type")) {
                message("This spell needs to be cast on${type.an()} $type obelisk.")
                return@onObjectOperate
            }
            val count = inventory.count("unpowered_orb")
            val (_, amount) = makeAmount(listOf("${type}_orb"), "Make", count, "How many would you like to charge?")
            chargeOrb(type, amount)
        }
    }

    private fun Player.chargeOrb(type: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        if (hasClock("action_delay")) {
            return
        }
        inventory.transaction {
            removeItems(this@chargeOrb, "charge_${type}_orb")
            add("${type}_orb")
        }
        if (inventory.transaction.error != TransactionError.None) {
            return
        }
        start("action_delay", 3)
        anim("charge_orb")
        gfx("charge_${type}_orb")
        sound("charge_${type}_orb")
        exp(
            Skill.Magic,
            when (type) {
                "fire" -> 63.0
                "earth" -> 60.0
                "air" -> 76.0
                else -> 56.0
            },
        )
        weakQueue("charge_orb", 6) {
            chargeOrb(type, amount - 1)
        }
    }
}
