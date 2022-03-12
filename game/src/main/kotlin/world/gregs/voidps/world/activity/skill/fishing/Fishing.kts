package world.gregs.voidps.world.activity.skill.fishing

import com.github.michaelbull.logging.InlineLogger
import net.pearx.kasechange.toLowerSpaceCase
import net.pearx.kasechange.toTitleCase
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.*
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.contain.ContainerResult
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
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
import world.gregs.voidps.engine.utility.plural

val logger = InlineLogger()

on<NPCClick>({ npc.def.has("fishing") }) { player: Player ->
    cancelled = player.hasEffect("skilling_delay")
}

on<Moved>({ it.contains("fishers") && it.def.has("fishing") }) { npc: NPC ->
    val fishers: Set<Player> = npc.remove("fishers") ?: return@on
    for (fisher in fishers) {
        fisher.action.cancel()
    }
}

on<NPCOption>({ npc.def.has("fishing") }) { player: Player ->
    player.action(ActionType.Fishing) {
        npc.getOrPut("fishers") { mutableSetOf<Player>() }.add(player)
        try {
            var first = true
            fishing@ while (isActive && player.awaitDialogues()) {
                if (player.inventory.isFull()) {
                    player.message("Your inventory is too full to hold any more fish.")
                    break
                }

                val data = npc.spot[option!!] ?: return@action

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
                    player.start("skilling_delay", 5)
                    first = false
                }
                delay(5)
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
        } finally {
            npc.get<MutableSet<Player>>("fishers").remove(player)
            player.clearAnimation()
        }
    }
}

fun addCatch(player: Player, catch: Item) {
    if (player.inventory.add(catch.id)) {
        player.message("You catch some ${catch.id.toLowerSpaceCase()}.", ChatType.Filter)
    } else {
        when (player.inventory.result) {
            ContainerResult.Full -> player.inventoryFull()
            else -> logger.warn { "Error adding fish $catch ${player.inventory.result}" }
        }
    }
}

val NPC.spot: Map<String, Spot>
    get() = def["fishing", emptyMap()]

val Item.fishing: Catch
    get() = def["fishing", Catch.EMPTY]