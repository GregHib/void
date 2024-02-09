package world.gregs.voidps.world.activity.skill.smithing

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.interact.itemOnObjectOperate
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.queue.weakQueue
import world.gregs.voidps.world.activity.quest.quest
import world.gregs.voidps.world.interact.dialogue.type.makeAmount
import world.gregs.voidps.world.interact.dialogue.type.statement
import world.gregs.voidps.world.interact.entity.sound.playSound

val logger = InlineLogger()

itemOnObjectOperate("steel_bar", "furnace*") {
    if (player.quest("dwarf_cannon") != "completed") {
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

fun smelt(player: Player, target: GameObject, id: String, amount: Int) {
    if (amount <= 0) {
        return
    }
    if (!player.has(Skill.Smithing, 35, message = true)) {
        return
    }
    player.face(furnaceSide(player, target))
    player.setAnimation("furnace_smelt")
    player.playSound("smelt_bar")
    player.message("You heat the steel bar into a liquid state.", ChatType.Filter)
    player.weakQueue("cannonball_melt", 3) {
        player.message("You poor the molten metal into your cannonball mould.", ChatType.Filter)
        player.weakQueue("cannonball_poor", 1) {
            player.message("The molten metal cools slowly to form 4 cannonballs.", ChatType.Filter)
        }
        player.setAnimation("climb_down")
        player.weakQueue("cannonball_remove", 4) {
            player.setAnimation("climb_down")
            player.message("You remove the cannonballs from the mould.", ChatType.Filter)
            player.inventory.transaction {
                remove("steel_bar")
                add("cannonball", 4)
            }
            when (player.inventory.transaction.error) {
                TransactionError.None -> {
                    player.exp(Skill.Smithing, 25.6)
                    player.weakQueue("cannonball_make", 3) {
                        smelt(player, target, id, amount - 1)
                    }
                }
                else -> logger.warn { "Cannonball transaction error $player $id $amount ${player.inventory.transaction.error}" }
            }
        }
    }
}