package content.skill.magic.book.modern

import content.skill.magic.spell.SpellRunes.removeItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Areas
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.operation.AddItemLimit.addToLimit
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace

class BonesTo : Script {
    init {
        interfaceOption("Cast", "modern_spellbook:bones_to_*") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (it.component == "bones_to_peaches" && !get("bones_to_peaches", false)) {
                message("You can only learn this spell from the Mage Training Arena.")
                return@interfaceOption
            }
            convert(this, it.component, tabletSlot = -1)
        }

        itemOption("Break", "bones_to_bananas,bones_to_peaches") { (item, slot) ->
            if (hasClock("action_delay")) {
                return@itemOption
            }
            if (inGraveyard(this)) {
                message("You can not use this tablet in the Mage Training Arena.")
                return@itemOption
            }
            convert(this, item.id, tabletSlot = slot)
        }
    }

    private fun convert(player: Player, spell: String, tabletSlot: Int) {
        val produce = if (spell == "bones_to_peaches") "peach" else "banana"
        val bones = if (inGraveyard(player)) graveyardBones() else mapOf("bones" to 1, "big_bones" to 1)
        if (bones.keys.none { player.inventory.contains(it) }) {
            player.message("You don't have any bones to cast this spell on.")
            return
        }
        val success = player.inventory.transaction {
            if (tabletSlot == -1) {
                removeItems(player, spell)
            } else {
                remove(tabletSlot, spell, 1)
            }
            for ((bone, multiplier) in bones) {
                var next = player.inventory.indexOf(bone)
                while (next != -1 && !failed) {
                    replace(next, bone, produce)
                    if (multiplier > 1) {
                        addToLimit(produce, multiplier - 1)
                    }
                    next = player.inventory.indexOf(bone)
                }
            }
        }
        if (!success) {
            return
        }
        player.start("action_delay", 1)
        player.anim("bones_to_spell")
        player.gfx("bones_to_spell")
        player.sound("bones_to_spell")
        if (tabletSlot == -1) {
            player.exp(Skill.Magic, Tables.int("spells.$spell.xp") / 10.0)
        }
    }

    private fun inGraveyard(player: Player): Boolean = player.tile in Areas["mage_training_arena_creature_graveyard"]

    /**
     * Each type of animals' bones in the Creature Graveyard yields a different amount of fruit.
     */
    private fun graveyardBones(): Map<String, Int> = Tables.get("mta_bones").rows().associate { it.item("item") to it.int("multiplier") }
}
