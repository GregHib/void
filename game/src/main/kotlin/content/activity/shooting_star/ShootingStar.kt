package content.activity.shooting_star

import com.github.michaelbull.logging.InlineLogger
import content.activity.shooting_star.ShootingStarHandler.currentActiveObject
import content.activity.shooting_star.ShootingStarHandler.currentStarTile
import content.activity.shooting_star.ShootingStarHandler.totalCollected
import content.entity.combat.hit.damage
import content.entity.player.dialogue.Happy
import content.entity.player.dialogue.Sad
import content.entity.player.dialogue.type.npc
import content.entity.sound.areaSound
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.data.settingsReload
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.mode.interact.Interact
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.entity.objectDespawn
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.blocked
import world.gregs.voidps.engine.timer.timerStart
import world.gregs.voidps.engine.timer.timerStop
import world.gregs.voidps.engine.timer.timerTick
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt
import kotlin.text.toIntOrNull

@Script
class ShootingStar : Api {

    val objects: GameObjects by inject()
    val npcs: NPCs by inject()
    val players: Players by inject()
    val logger = InlineLogger()

    override fun spawn(player: Player) {
        if (player["shooting_star_bonus_ore", 0] > 0) {
            player.timers.restart("shooting_star_bonus_ore_timer")
        }
    }

    override fun worldSpawn() {
        if (Settings["events.shootingStars.enabled", false]) {
            eventUpdate()
        }
    }

    init {
        settingsReload {
            if (Settings["events.shootingStars.enabled", false] && !World.contains("shooting_star_event_timer")) {
                eventUpdate()
            } else if (!Settings["events.shootingStars.enabled", false] && World.contains("shooting_star_event_timer")) {
                World.clearQueue("shooting_star_event_timer")
            }
        }

        timerStart("shooting_star_bonus_ore_timer") {
            interval = TimeUnit.SECONDS.toTicks(1)
        }

        timerTick("shooting_star_bonus_ore_timer") { player ->
            if (player.dec("shooting_star_bonus_ore") <= 0) {
                cancel()
                return@timerTick
            }
        }

        timerStop("shooting_star_bonus_ore_timer") { player ->
            player.message("<dark_red>The ability to mine an extra ore has worn off.")
        }

        objectDespawn("shooting_star_tier_1") {
            areaSound("star_meteor_despawn", it.tile, radius = 15)
            cleanseEvent(false)
        }

        timerStart("mining") { player ->
            val target = (player.mode as? Interact)?.target as GameObject
            val isStar = target.id.startsWith("crashed_star")
            if (isStar) {
                val isEarlyBird = ShootingStarHandler.isEarlyBird()
                if (isEarlyBird) {
                    player.message("Congratulations!, You were the first person to find this star!")
                    val xpToAdd: Double = player.levels.get(Skill.Mining) * 75.0
                    player.experience.add(Skill.Mining, xpToAdd)
                }
            }
        }

        objectApproach("Prospect", "crashed_star_tier_#") {
            if (player.queue.contains("prospect")) {
                return@objectApproach
            }
            approachRange(1)
            arriveDelay()
            val starPayout = target.def["collect_for_next_layer", -1]
            player.message("You examine the crashed star...")
            delay(4)
            val star = def.getOrNull<Rock>("mining")?.ores?.firstOrNull()
            if (star == null) {
                player.message("Star has been mined...")
            } else if (starPayout != -1) {
                val percentageCollected = getLayerPercentage(totalCollected, starPayout)
                player.message("There is $percentageCollected% left of this layer.")
            }
        }

        npcOperate("Talk-to", "star_sprite") {
            npc<Happy>("Thank you for helping me out of here")
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
                        messageBuilder.append(", $amount ${reward.replace("_", " ").replace("noted", "").plural(amount)}")
                    }
                }
                if (!ShootingStarHandler.rewardPlayerBonusOre(player)) {
                    npc<Happy>("I have rewarded you by making it so you can mine extra ore for the next 15 minutes, $messageBuilder.")
                    givePlayerBonusOreReward(player)
                } else {
                    npc<Happy>("You already have the ability to mine an extra ore, ${messageBuilder.replace(0, 4, "However")}.")
                }
            }
        }
        adminCommand("star", stringArg("minutes"), desc = "Start a new shooting star event in [minutes]", handler = ::spawn)
    }

    fun spawn(player: Player, args: List<String>) {
        cleanseEvent(true)
        val minutes = args[0].toIntOrNull()
        if (minutes != null) {
            World.clearQueue("shooting_star_event_timer")
            eventUpdate(minutes)
        } else {
            World.clearQueue("shooting_star_event_timer")
        }
    }

    fun eventUpdate() {
        val minutes = Settings["events.shootingStars.minRespawnTimeMinutes", 60]..Settings["events.shootingStars.maxRespawnTimeMinutes", 60]
        eventUpdate(minutes.random(random))
    }

    fun eventUpdate(minutes: Int) {
        World.queue("shooting_star_event_timer", TimeUnit.MINUTES.toTicks(minutes)) {
            if (currentStarTile != Tile.EMPTY) {
                cleanseEvent(true)
                logger.info { "There was already an active star, deleted it and started a new event" }
            }
            startCrashedStarEvent()
            eventUpdate()
        }
    }

    fun startCrashedStarEvent() {
        val location = StarLocationData.entries.random()
        currentStarTile = location.tile
        val tier = random.nextInt(1, 9)
        if (Settings["world.messages", false]) {
            for (player in players) {
                player.message("${Colours.DARK_RED.toTag()}A star has crashed at ${location.description}.")
            }
        }
        logger.info { "Crashed star event has started at: $location (${currentStarTile.x}, ${currentStarTile.y}) tier $tier." }
        val shootingStarShadow = npcs.add("shooting_star_shadow", Tile(currentStarTile.x, currentStarTile.y + 6), Direction.NONE)
        shootingStarShadow.walkTo(currentStarTile, noCollision = true, forceWalk = true)
        areaSound("star_meteor_falling", currentStarTile, radius = 15, delay = 20)
        World.queue("awaiting_shadow_walk", 6) {
            val shootingStarObjectFalling: GameObject = objects.add("crashed_star_falling_object", currentStarTile)
            val under = mutableListOf<Player>()
            for (tile in currentStarTile.toCuboid(2, 2)) {
                for (player in players[tile]) {
                    under.add(player)
                }
            }
            for (player in under) {
                player.damage(random.nextInt(10, 50))
                val direction = Direction.all.first { !player.blocked(it) }
                player.exactMove(direction.delta, 1, direction = direction.inverse())
                player.anim("step_back_startled")
            }
            World.queue("falling_star_object_removal", 1) {
                currentActiveObject = shootingStarObjectFalling.replace("crashed_star_tier_$tier")
                npcs.remove(shootingStarShadow)
            }
        }
    }

    fun cleanseEvent(forceStopped: Boolean) {
        currentActiveObject?.let { current -> objects[currentStarTile, current.id] }?.remove()
        if (!forceStopped) {
            areaSound("star_sprite_appear", currentStarTile, radius = 10)
            val starSprite = npcs.add("star_sprite", currentStarTile, Direction.NONE)
            World.queue("start_sprite_despawn_timer", TimeUnit.MINUTES.toTicks(10)) {
                npcs.remove(starSprite)
            }
        }
        totalCollected = 0
        currentStarTile = Tile.EMPTY
        currentActiveObject = null
        ShootingStarHandler.earlyBird = false
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
            "gold_ore_noted" to goldOres,
        )
    }

    fun givePlayerBonusOreReward(player: Player) {
        player["shooting_star_bonus_ore"] = 900
        player.timers.start("shooting_star_bonus_ore_timer")
    }
}
