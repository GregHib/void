package content.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.makeAmount
import content.entity.player.dialogue.type.statement
import content.entity.sound.sound
import content.quest.quest
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.mode.interact.Interaction
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.noInterest
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue

val logger = InlineLogger()

itemOnObjectOperate("steel_bar", "furnace*") {
    if (player.quest("dwarf_cannon") != "completed") {
        player.noInterest()
        return@itemOnObjectOperate
    }
    if (!player.inventory.contains("ammo_mould")) {
        statement("You need a mould to make cannonballs with.")
        return@itemOnObjectOperate
    }
    val max = player.inventory.count("steel_bar")
    val (item, amount) = makeAmount(listOf("cannonball"), "Make", max, names = listOf("Cannonball<br>(set of 4)"))
    smelt(player, target, item, amount)
}

suspend fun Interaction<Player>.smelt(player: Player, target: GameObject, id: String, amount: Int) {
    if (amount <= 0) {
        return
    }
    if (!player.has(Skill.Smithing, 35, message = true)) {
        return
    }
    player.face(furnaceSide(player, target))
    player.anim("furnace_smelt")
    player.sound("smelt_bar")
    player.message("You heat the steel bar into a liquid state.", ChatType.Filter)
    delay(3)
    player.message("You poor the molten metal into your cannonball mould.", ChatType.Filter)
    player.anim("climb_down")
    delay(1)
    player.message("The molten metal cools slowly to form 4 cannonballs.", ChatType.Filter)
    delay(3)
    player.anim("climb_down")
    player.message("You remove the cannonballs from the mould.", ChatType.Filter)
    player.inventory.transaction {
        remove("steel_bar")
        add("cannonball", 4)
    }
    when (player.inventory.transaction.error) {
        TransactionError.None -> {
            player.exp(Skill.Smithing, 25.6)
            player.weakQueue("cannonball_smelting", 3) {
                smelt(player, target, id, amount - 1)
            }
        }
        else -> logger.warn { "Cannonball transaction error $player $id $amount ${player.inventory.transaction.error}" }
    }
}
