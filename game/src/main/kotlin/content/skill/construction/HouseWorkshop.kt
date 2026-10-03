package content.skill.construction

import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

/**
 * Workshop furniture: clockmaker's benches for crafting toys and clockwork, the helmet pluming stand and heraldry easels
 * which paint the players family crest. Workbenches and upgrading are part of [FurnitureCreation].
 */
class HouseWorkshop : Script {
    init {
        objectOperate("Craft", "crafting_table_*") { (target) ->
            val bench = target.id.removePrefix("crafting_table_").toIntOrNull() ?: return@objectOperate
            val products = Tables.get("house_clockmaking").rows().filter { it.int("bench") <= bench }.map { it.rowId }
            val (item, amount) = makeAmount(products, "Make", 28, "What would you like to make?")
            craft(target, item, amount)
        }

        objectOperate("Make-helmet", "pluming_stand") { (target) ->
            paint(target)
        }

        objectOperate("Use", "shield_easel,banner_easel") { (target) ->
            paint(target)
        }
    }

    private fun Player.craft(bench: GameObject, item: String, amount: Int) {
        if (amount <= 0) {
            return
        }
        val row = Rows.getOrNull("house_clockmaking.$item") ?: return
        if (!has(Skill.Crafting, row.int("level"), message = true)) {
            return
        }
        face(bench)
        inventory.transaction {
            for (material in row.itemList("materials")) {
                remove(material)
            }
            add(item)
        }
        if (inventory.transaction.error != TransactionError.None) {
            val materials = row.itemList("materials").joinToString(" and ") { ItemDefinitions.get(it).name.lowercase() }
            message("You need $materials to make that.") // TODO proper message
            return
        }
        anim("construction_build")
        exp(Skill.Crafting, row.int("xp") / 10.0)
        weakQueue("clockmaking", 3) {
            craft(bench, item, amount - 1)
        }
    }

    /**
     * Paints the players family crest onto the item made by the heraldry [stand]
     */
    private fun Player.paint(stand: GameObject) {
        val row = Rows.getOrNull("house_heraldry.${stand.id}") ?: return
        val crest: Int = get("heraldry_crest") ?: run {
            message("You need a family crest before you can do that.") // TODO proper message
            return
        }
        if (!has(Skill.Crafting, row.int("level"), message = true)) {
            return
        }
        val materials = row.itemList("materials")
        val product = "${row.string("product")}_${crests[crest]}"
        inventory.transaction {
            for (material in materials) {
                remove(material)
            }
            add(product)
        }
        if (inventory.transaction.error != TransactionError.None) {
            message("You need ${materials.joinToString(" and ") { ItemDefinitions.get(it).name.lowercase() }} to make that.") // TODO proper message
            return
        }
        face(stand)
        anim("construction_build")
    }

    companion object {
        // Family crests in the order they're picked
        private val crests = listOf("arrav", "asgarnia", "dorgeshuun", "dragon", "fairy", "guthix", "ham", "horse", "jogre", "kandarin", "misthalin", "money", "saradomin", "skull", "varrock", "zamorak")
    }
}
