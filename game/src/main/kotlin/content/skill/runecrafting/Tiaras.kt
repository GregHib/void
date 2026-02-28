package content.skill.runecrafting

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove

class Tiaras : Script {

    val logger = InlineLogger()

    init {
        itemOnObjectOperate("tiara", "*_altar") { (target) ->
            val id = target.id.replace("_altar", "_tiara")
            bindTiara(this, id)
        }
    }

    fun Tiaras.bindTiara(player: Player, id: String) {
        val xp = EnumDefinitions.intOrNull("tiara_xp", id) ?: return
        player.softTimers.start("runecrafting")
        val tiaraId = "tiara"
        val talismanId = id.replace("_tiara", "_talisman")
        player.inventory.transaction {
            remove(tiaraId, 1)
            remove(talismanId, 1)
            add(id, 1)
        }
        player.start("movement_delay", 3)
        when (player.inventory.transaction.error) {
            is TransactionError.Deficient, is TransactionError.Invalid -> {
                player.message("You don't have a talisman to bind.")
            }
            TransactionError.None -> {
                player.exp(Skill.Runecrafting, xp / 10.0)
                player.anim("bind_runes")
                player.gfx("bind_runes")
                player.sound("bind_runes")
            }
            else -> logger.warn { "Error binding talisman with tiara $player $id ${player.levels.get(Skill.Runecrafting)} $talismanId" }
        }
        player.softTimers.stop("runecrafting")
    }
}
