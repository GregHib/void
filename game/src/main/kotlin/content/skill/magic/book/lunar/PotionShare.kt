package content.skill.magic.book.lunar

import content.skill.constitution.drink.potionEffects
import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class PotionShare : Script {

    private val restorePotions = setOf(
        "restore_potion",
        "super_restore",
        "prayer_potion",
        "super_prayer",
        "energy_potion",
        "super_energy",
    )

    private val boostPotions = setOf(
        "attack_potion",
        "strength_potion",
        "defence_potion",
        "combat_potion",
        "agility_potion",
        "fishing_potion",
        "hunter_potion",
        "crafting_potion",
        "fletching_potion",
        "summoning_potion",
        "magic_essence",
        "super_attack",
        "super_strength",
        "super_defence",
        "super_magic_potion",
        "super_ranging_potion",
    )

    init {
        onItem("lunar_spellbook:stat_restore_pot_share") { item, _ ->
            share(this, item, "stat_restore_pot_share", restorePotions, radius = 2)
        }

        onItem("lunar_spellbook:boost_potion_share") { item, _ ->
            share(this, item, "boost_potion_share", boostPotions, radius = 1)
        }
    }

    private fun share(player: Player, item: Item, spell: String, accepted: Set<String>, radius: Int) {
        if (player.hasClock("action_delay")) {
            return
        }
        val doses = item.id.last().digitToIntOrNull() ?: 0
        val base = item.id.substringBeforeLast("_")
        if (doses == 0 || base !in accepted) {
            player.message("You can't share that potion.")
            return
        }
        val nearby = Players
            .filter { other -> other != player && other.tile.within(player.tile, radius) && other.get("accept_aid", true) }
            .take(doses - 1)
        if (nearby.isEmpty()) {
            player.message("There is nobody around to share the potion with.")
            return
        }
        val remaining = doses - 1 - nearby.size
        val product = if (remaining == 0) "vial" else "${base}_$remaining"
        player.inventory.transaction {
            removeItems(player, spell, message = false)
            replace(item.id, product)
        }
        when (player.inventory.transaction.error) {
            TransactionError.None -> {
                player.start("action_delay", 2)
                player.anim("lunar_cast_charge")
                player.sound(spell)
                player.exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
                player.potionEffects(item.id)
                for (other in nearby) {
                    other.gfx("pot_share")
                    other.potionEffects(item.id)
                    other.message("${player.name} has shared a potion with you.")
                }
            }
            is TransactionError.Deficient -> player.message("You do not have the required items to cast this spell.")
            else -> return
        }
    }
}
