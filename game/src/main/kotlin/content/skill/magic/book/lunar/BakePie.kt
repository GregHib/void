package content.skill.magic.book.lunar

import content.skill.magic.spell.removeSpellItems
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.ReplaceItem.replace
import world.gregs.voidps.engine.queue.weakQueue

class BakePie : Script {

    init {
        interfaceOption("Cast", "lunar_spellbook:bake_pie") {
            if (hasClock("action_delay")) {
                return@interfaceOption
            }
            if (findPie(this) == null) {
                message("You have no pies which you have the level to bake.")
                return@interfaceOption
            }
            bake(this)
        }
    }

    private fun findPie(player: Player): Pair<Int, String>? {
        for (index in player.inventory.indices) {
            val item = player.inventory[index]
            if (!item.id.startsWith("raw_") || !item.id.endsWith("_pie")) {
                continue
            }
            val row = Rows.getOrNull("cooking.${item.id}") ?: continue
            if (player.levels.get(Skill.Cooking) < row.int("level")) {
                continue
            }
            return index to item.id
        }
        return null
    }

    private fun bake(player: Player) {
        val (index, id) = findPie(player) ?: return
        if (!player.removeSpellItems("bake_pie")) {
            return
        }
        val row = Rows.get("cooking.$id")
        player.inventory.transaction {
            replace(index, id, row.item("cooked"))
        }
        if (player.inventory.transaction.error != TransactionError.None) {
            return
        }
        player.start("action_delay", 4)
        player.anim("lunar_cast_charge")
        player.gfx("bake_pie")
        player.sound("bake_pie")
        player.exp(Skill.Magic, Tables.int("spells.bake_pie.xp") / 10.0)
        player.exp(Skill.Cooking, row.int("xp") / 10.0)
        player.message(row.string("cooked_message"))
        player.weakQueue("bake_pie", 4) {
            bake(player)
        }
    }
}
