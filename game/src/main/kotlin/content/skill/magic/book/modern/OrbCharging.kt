package content.skill.magic.book.modern

import content.entity.player.dialogue.type.makeAmount
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.queue.weakQueue

class OrbCharging : Script {
    init {
        onObjectOperate("modern_spellbook:charge_water_orb") { (target) ->
            if (!target.id.startsWith("obelisk_of_water")) {
                message("This spell needs to be cast on a water obelisk.")
                return@onObjectOperate
            }
            val count = inventory.count("unpowered_orb")
            val (_, amount) = makeAmount(listOf("water_orb"), "Make", count, "How many would you like to charge?")
            chargeOrb("water", amount)
        }

        onObjectOperate("modern_spellbook:charge_earth_orb") { (target) ->
            if (!target.id.startsWith("obelisk_of_earth")) {
                message("This spell needs to be cast on an earth obelisk.")
                return@onObjectOperate
            }
            val count = inventory.count("unpowered_orb")
            val (_, amount) = makeAmount(listOf("earth_orb"), "Make", count, "How many would you like to charge?")
            chargeOrb("earth", amount)
        }

        onObjectOperate("modern_spellbook:charge_air_orb") { (target) ->
            if (!target.id.startsWith("obelisk_of_air")) {
                message("This spell needs to be cast on an air obelisk.")
                return@onObjectOperate
            }
            val count = inventory.count("unpowered_orb")
            val (_, amount) = makeAmount(listOf("air_orb"), "Make", count, "How many would you like to charge?")
            chargeOrb("air", amount)
        }

        onObjectOperate("modern_spellbook:charge_fire_orb") { (target) ->
            if (!target.id.startsWith("obelisk_of_fire")) {
                message("This spell needs to be cast on a fire obelisk.")
                return@onObjectOperate
            }
            val count = inventory.count("unpowered_orb")
            val (_, amount) = makeAmount(listOf("fire_orb"), "Make", count, "How many would you like to charge?")
            chargeOrb("fire", amount)
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
        exp(Skill.Magic, when (type) {
            "fire" -> 63.0
            "earth" -> 60.0
            "air" -> 76.0
            else -> 56.0
        })
        weakQueue("charge_orb", 6) {
            chargeOrb(type, amount - 1)
        }
    }
}