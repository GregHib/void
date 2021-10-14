package world.gregs.voidps.world.activity.skill.fishing

import com.github.michaelbull.logging.InlineLogger
import world.gregs.voidps.engine.action.ActionType
import world.gregs.voidps.engine.action.action
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.awaitDialogues
import world.gregs.voidps.engine.entity.character.Moved
import world.gregs.voidps.engine.entity.character.contain.ContainerResult
import world.gregs.voidps.engine.entity.character.contain.hasItem
import world.gregs.voidps.engine.entity.character.contain.inventory
import world.gregs.voidps.engine.entity.character.contain.inventoryFull
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCClick
import world.gregs.voidps.engine.entity.character.npc.NPCOption
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.Level.success
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.clearAnimation
import world.gregs.voidps.engine.entity.character.update.visual.player.face
import world.gregs.voidps.engine.entity.character.update.visual.setAnimation
import world.gregs.voidps.engine.entity.hasEffect
import world.gregs.voidps.engine.entity.start
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.plural
import world.gregs.voidps.engine.utility.toTitleCase
import world.gregs.voidps.world.activity.skill.fishing.fish.Catch
import world.gregs.voidps.world.activity.skill.fishing.spot.FishingSpot
import world.gregs.voidps.world.activity.skill.fishing.tackle.Bait
import world.gregs.voidps.world.activity.skill.fishing.tackle.Tackle

val logger = InlineLogger()

on<NPCClick>({ npc.name.startsWith("fishing_spot") }) { player: Player ->
    cancel = player.hasEffect("skilling_delay")
}

on<NPCOption>({ npc.name.startsWith("fishing_spot") }) { player: Player ->
    player.action(ActionType.Fishing) {
        val handler = npc.events.on<NPC, Moved> {
            cancel()
        }
        try {
            var first = true
            fishing@ while (isActive && player.awaitDialogues()) {
                if (player.inventory.isFull()) {
                    player.message("Your inventory is too full to hold any more fish.")
                    break
                }

                val spot = FishingSpot.get(npc) ?: break
                val (tackles, data) = spot.tackle[option] ?: break

                val lowestLevel = data.values.minOf { it.minOf { catch -> catch.level } }
                if (!player.has(Skill.Fishing, lowestLevel, true)) {
                    break
                }

                val tackle = tackles.firstOrNull { tackle -> player.hasItem(tackle.id) }
                if (tackle == null) {
                    player.message("You need a ${tackles.first().id.toTitleCase()} to catch these fish.")
                    break@fishing
                }

                val bait = data.keys.firstOrNull { bait -> bait == Bait.None || player.hasItem(bait.id) }
                val catches = data[bait]
                if (bait == null || catches == null) {
                    player.message("You don't have any ${data.keys.first().id.toTitleCase().plural(2)}.")
                    break
                }

                player.face(npc)
                val rod = tackle == Tackle.FishingRod || tackle == Tackle.FlyFishingRod || tackle == Tackle.BarbarianRod
                player.setAnimation("fish_${if (rod) if (first) "fishing_rod" else "rod" else tackle.id}")
                if (first) {
                    player.message(when (tackle) {
                        Tackle.SmallFishingNet, Tackle.BigFishingNet -> "You cast out your net..."
                        Tackle.FishingRod, Tackle.FlyFishingRod, Tackle.BarbarianRod -> "You cast out your line..."
                        Tackle.CrayfishCage -> "You attempt to catch a crayfish."
                        Tackle.LobsterPot -> "You attempt to catch a lobster."
                        Tackle.Harpoon, Tackle.BarbTailHarpoon -> "You start harpooning fish."
                    }, ChatType.GameFilter)
                    player.start("skilling_delay", 5)
                    first = false
                }
                delay(5)
                for (catch in catches) {
                    val level = player.levels.get(Skill.Fishing)
                    if (level >= catch.level && success(level, catch.chance)) {
                        if (bait != Bait.None && !player.inventory.remove(bait.id)) {
                            break@fishing
                        }
                        player.experience.add(Skill.Fishing, catch.xp)

                        addCatch(player, catch)
                        break
                    }
                }
            }
        } finally {
            npc.events.remove(handler)
            player.clearAnimation()
        }
    }
}

fun addCatch(player: Player, catch: Catch) {
    if (player.inventory.add("raw_${catch.id}")) {
        player.message("You catch some ${catch.id.toTitleCase().toLowerCase()}.", ChatType.GameFilter)
    } else {
        when (player.inventory.result) {
            ContainerResult.Full -> player.inventoryFull()
            else -> logger.warn { "Error adding fish $catch ${player.inventory.result}" }
        }
    }
}