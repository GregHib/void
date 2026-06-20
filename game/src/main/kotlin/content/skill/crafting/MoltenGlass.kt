package content.skill.crafting

import com.github.michaelbull.logging.InlineLogger
import content.entity.player.dialogue.type.makeAmount
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.contains
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.transact.TransactionError
import world.gregs.voidps.engine.inv.transact.operation.AddItem.add
import world.gregs.voidps.engine.inv.transact.operation.RemoveItem.remove
import world.gregs.voidps.engine.queue.weakQueue
import kotlin.math.min

class MoltenGlass : Script {

    val logger = InlineLogger()

    init {
        itemOnObjectOperate("bucket_of_sand", "furnace*", arrive = false) { (target) ->
            make(target)
        }

        itemOnObjectOperate("soda_ash", "furnace*", arrive = false) { (target) ->
            make(target)
        }
    }

    suspend fun Player.make(target: GameObject) {
        if (!inventory.contains("bucket_of_sand") || !inventory.contains("soda_ash")) {
            message("You need soda ash and buckets of sand to make molten glass.")
            return
        }
        val max = min(inventory.count("bucket_of_sand"), inventory.count("soda_ash"))
        val (_, amount) = makeAmount(listOf("molten_glass"), "Make", max)
        softTimers.start("molten_glass")
        make(target, amount)
    }

    fun Player.make(target: GameObject, amount: Int) {
        if (amount <= 0) {
            softTimers.stop("molten_glass")
            return
        }
        if (!inventory.contains("bucket_of_sand") || !inventory.contains("soda_ash")) {
            softTimers.stop("molten_glass")
            return
        }
        face(target)
        anim("furnace_smelt")
        sound("smelt_bar")
        weakQueue("molten_glass", 3) {
            inventory.transaction {
                remove("bucket_of_sand")
                remove("soda_ash")
                add("molten_glass")
                add("bucket")
            }
            when (inventory.transaction.error) {
                TransactionError.None -> {
                    exp(Skill.Crafting, 20.0)
                    message("You heat the sand and soda ash in the furnace to make glass.", ChatType.Filter)
                    make(target, amount - 1)
                }
                else -> {
                    logger.warn { "Molten glass transaction error $this $amount ${inventory.transaction.error}" }
                    softTimers.stop("molten_glass")
                }
            }
        }
    }
}
