package world.gregs.voidps.world.activity.dnd.shootingstar

import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.forceWalk
import world.gregs.voidps.engine.entity.character.move.walkTo
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.entity.obj.objectApproach
import world.gregs.voidps.engine.entity.obj.replace
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.dnd.shootingstar.ShootingStarHandler.cleanseEvent
import world.gregs.voidps.world.activity.dnd.shootingstar.ShootingStarHandler.currentActiveObject
import world.gregs.voidps.world.activity.dnd.shootingstar.ShootingStarHandler.currentStarTile
import world.gregs.voidps.world.activity.dnd.shootingstar.ShootingStarHandler.startEvent
import world.gregs.voidps.world.activity.dnd.shootingstar.ShootingStarHandler.totalCollected
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Sad
import world.gregs.voidps.world.interact.dialogue.type.npc
import world.gregs.voidps.world.interact.entity.combat.hit.damage
import kotlin.math.roundToInt

val objects: GameObjects by inject()
val npcs: NPCs by inject()
val players: Players by inject()

worldSpawn {
    eventUpdate()
}

fun eventUpdate() {
    World.queue("shooting_star_event_timer", startEvent) {
       if (isPlayersPresent()) {
            eventUpdate()
            return@queue
       }
       if (currentStarTile != Tile.EMPTY) {
            cleanseEvent(true)
            println("There was already an active star, deleted it and started a new event")
       }
       startCrashedStarEvent()
       eventUpdate()
    }
}

fun isPlayersPresent(): Boolean {
    return false
}

fun startCrashedStarEvent() {
    currentStarTile = StarLocationData.entries.random().location
    val shootingStarShadow: NPC? = npcs.add("shooting_star_shadow", Tile(currentStarTile.x, currentStarTile.y + 6), Direction.NONE)
    shootingStarShadow?.walkTo(currentStarTile, true, true)
    World.queue("awaiting_shadow_walk", 6) {
        val shootingStarObjectFalling: GameObject = objects.add("crashed_star_falling_object", currentStarTile)
        World.queue("falling_star_object_removal", 1) {
              for (tile in currentStarTile.toCuboid(2, 2)) {
                 for (player in players[tile]) {
                     player.damage(random.nextInt(10, 50))
                     val direction = if (player.tile == currentStarTile) Direction.SOUTH else currentStarTile.delta(player.tile).toDirection()
                     if (!player.blocked(direction)) {
                         player.forceWalk(direction.delta, 1, direction.inverse())
                    }
                     player.setAnimation("fall_back_on_butt")
                 }
              }
            currentActiveObject = shootingStarObjectFalling.replace("crashed_star_tier_${random.nextInt(1, 9)}")
            npcs.remove(shootingStarShadow)
        }
    }
}

fun getLayerPercentage(totalCollected: Int, totalNeeded: Int): String {
    val remaining = totalNeeded - totalCollected
    val percentageRemaining = (remaining.toDouble() / totalNeeded.toDouble()) * 100
    return String.format("%.2f", percentageRemaining)
}

fun calculateRewards(stardust: Int): Map<String, Int> {
    val coinsPerStardust = 50002.0 / 200
    val astralRunesPerStardust = 52.0 / 200
    val cosmicRunesPerStardust = 152.0 / 200
    val goldOresPerStardust = 20.0 / 200

    val coins = (coinsPerStardust * stardust).toInt()
    val astralRunes = (astralRunesPerStardust * stardust).roundToInt()
    val cosmicRunes = (cosmicRunesPerStardust * stardust).roundToInt()
    val goldOres = (goldOresPerStardust * stardust).roundToInt()

    return mapOf(
            "coins" to coins,
            "astral_rune" to astralRunes,
            "cosmic_rune" to cosmicRunes,
            "gold_ore_noted" to goldOres
    )
}

objectApproach("Prospect", "crashed_star_tier_#") {
    if (player.queue.contains("prospect")) {
        return@objectApproach
    }
    val starPayout = target.def["collect_for_next_layer", -1]
    player.message("You examine the crashed star...")
    player.start("movement_delay", 4)
    player.softQueue("prospect", 4) {
        val star = def.getOrNull<Rock>("mining")?.ores?.firstOrNull()
        if (star == null) {
            player.message("Star has been mined...")
        } else {
            val percentageCollected = getLayerPercentage(totalCollected, starPayout)
            player.message("There is $percentageCollected% left of this layer.")
        }
    }
}

npcOperate("Talk-to", "star_sprite") {
    npc<Cheerful>("Thank you for helping me out of here")
    val starDustCount = player.inventory.count("stardust")
    if (player.inventory.isFull()) {
        player.message("Inventory full. To make more room, sell, drop or bank something.", ChatType.Game)
    } else if (starDustCount == 0) {
        npc<Sad>("You don't seem to have any stardust that I can exchange for a reward")
    } else if (starDustCount > 0) {
        val rewards = calculateRewards(starDustCount)
        player.inventory.remove("stardust", starDustCount)
        val messageBuilder = StringBuilder("Also, ")
        rewards.entries.forEachIndexed { index, (reward, amount) ->
            player.inventory.add(reward, amount)
            if (index == 0) {
                messageBuilder.append("have $amount $reward")
            } else {
                messageBuilder.append(", $amount ${reward.replace("_", " ").replace("noted", "") + "s"}")
            }
        }
        npc<Cheerful>("I have rewarded you by making it so you can mine extra ore for the next 15 minutes, ${messageBuilder}.")
    }
}