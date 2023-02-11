package world.gregs.voidps.world.activity.skill.fishing

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.contain.add
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.remove
import world.gregs.voidps.engine.entity.character.contain.transact.TransactionError
import world.gregs.voidps.engine.entity.character.event.Moved
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.inventoryFull
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.success
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.definition.data.Catch
import world.gregs.voidps.engine.entity.definition.data.Spot
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.suspend.awaitDialogues
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.engine.utility.plural

val logger = InlineLogger()

on<Moved>({ it.contains("fishers") && it.def.has("fishing") }) { npc: NPC ->
    val fishers: Set<Player> = npc.remove("fishers") ?: return@on
    for (fisher in fishers) {
        fisher.queue.clearWeak()
    }
}

on<NPCOption>({ def.has("fishing") }) { player: Player ->
    npc.getOrPut("fishers") { mutableSetOf<Player>() }.add(player)
    player.start("fishing")
    onCancel = {
        npc.get<MutableSet<Player>>("fishers").remove(player)
        player.clearAnimation()
        player.stop("fishing")
    }
    var first = true
    fishing@ while (player.awaitDialogues()) {
        if (player.inventory.isFull()) {
            player.message("Your inventory is too full to hold any more fish.")
            break
        }

        val data = npc.spot[option] ?: return@on

        if (!player.has(Skill.Fishing, data.minimumLevel, true)) {
            break
        }

        val tackle = data.tackle.firstOrNull { tackle -> player.hasItem(tackle.id) }
        if (tackle == null) {
            player.message("You need a ${data.tackle.first().id.toTitleCase()} to catch these fish.")
            break@fishing
        }

        val bait = data.bait.keys.firstOrNull { bait -> bait == "none" || player.hasItem(bait) }
        val catches = data.bait[bait]
        if (bait == null || catches == null) {
            player.message("You don't have any ${data.bait.keys.first().toTitleCase().plural(2)}.")
            break
        }

        player.face(npc)
        val rod = tackle.id == "fishing_rod" || tackle.id == "fly_fishing_rod" || tackle.id == "barbarian_rod"
        player.setAnimation("fish_${if (rod) if (first) "fishing_rod" else "rod" else tackle.id}")
        if (first) {
            player.message(tackle.def["cast", ""], ChatType.Filter)
            first = false
        }
        pause(5)
        for (item in catches) {
            val catch = item.fishing
            val level = player.levels.get(Skill.Fishing)
            if (level >= catch.level && success(level, catch.chance)) {
                if (bait != "none" && !player.inventory.remove(bait)) {
                    break@fishing
                }
                player.experience.add(Skill.Fishing, catch.xp)
                addCatch(player, item)
                break
            }
        }
    }
}

fun addCatch(player: Player, catch: Item) {
    player.inventory.add(catch.id)
    when (player.inventory.transaction.error) {
        TransactionError.None -> player.message("You catch some ${catch.id.toLowerSpaceCase()}.", ChatType.Filter)
        is TransactionError.Full -> player.inventoryFull()
        else -> logger.warn { "Error adding fish $catch ${player.inventory.transaction.error}" }
    }
}

val NPC.spot: Map<String, Spot>
    get() = def["fishing", emptyMap()]

val Item.fishing: Catch
    get() = def["fishing", Catch.EMPTY]