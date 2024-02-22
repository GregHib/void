package world.gregs.voidps.world.activity.dnd.shootingstar

import net.pearx.kasechange.toLowerSpaceCase
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.remaining
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.client.variable.stop
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.data.Ore
import world.gregs.voidps.engine.data.definition.data.Rock
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.clearAnimation
import world.gregs.voidps.engine.entity.character.face
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.npc.npcOperate
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.inventoryFull
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.hasRequirementsToUse
import world.gregs.voidps.engine.entity.character.setAnimation
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.obj.*
import world.gregs.voidps.engine.entity.worldSpawn
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.holdsItem
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.queue.softQueue
import world.gregs.voidps.engine.suspend.pause
import world.gregs.voidps.network.visual.update.player.EquipSlot
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random
import world.gregs.voidps.world.activity.bank.bank
import world.gregs.voidps.world.interact.dialogue.Cheerful
import world.gregs.voidps.world.interact.dialogue.Sad
import world.gregs.voidps.world.interact.dialogue.type.npc
import kotlin.math.roundToInt
import kotlin.random.Random

val itemDefinitions: ItemDefinitions by inject()
val objects: GameObjects by inject()
val npcs: NPCs by inject()

var totalCollected: Int = 0
var currentStarTile = Tile(-1, -1) // same as below I don't know if this is a good way to init this var
var currentActiveObject = GameObject(-1,-1,-1,-1,-1,-1) // I don't know if this is a good way to init this var
val startEvent = Random.nextInt(from = 1 * 60 * 60 * 1000, until = 2 * 60 * 60 * 1000) // get a random time between 1h - 2h
val initialStar: IntArray = intArrayOf(38660, 38661, 38662, 38663, 38664, 38665, 38666, 38667, 38668) // array of all tiers of stars to be picked randomly
val ownedStarDustThreshold = 200 // the max amount of star dust a player is allowed to own in both inventory and bank

worldSpawn {
    eventUpdate()
}

fun eventUpdate() {
    World.run("shooting_star_event_timer", startEvent) {
       if(isPlayersPresent()) {
            eventUpdate()
            return@run
       }
       if(currentStarTile != Tile(-1, -1)) { //there's already an active event
            cleanseEvent(true) // force stop even meaning - don't spawn the npc because the server stopped the event.
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
    val starLocation = getRandomLocation()
    val starToSpawn = getInitialStar()
    currentStarTile = starLocation.location
    currentActiveObject = GameObject(starToSpawn, currentStarTile, 10, 0)
    objects.add(currentActiveObject)
    println("${starLocation.locationDescription}")
}

fun handleMinedStarDust(currentMinedStar: GameObject) {
    val starPayout = currentMinedStar.def["collect_for_next_layer", -1]
    if(totalCollected >= starPayout){
        val stage = currentMinedStar.id.takeLast(1)
        if(stage.equals("1")){ //if is the lowest tier star just delete the star and clean the event
            currentMinedStar.remove(currentMinedStar.intId, true)
            cleanseEvent(false)
            return
        }
        val nextStage = currentMinedStar.id.replace(stage, (stage.toInt() - 1).toString())
        val nextstar = currentMinedStar.replace(nextStage)
        totalCollected = 0
        changeStar(currentMinedStar.id, nextstar.id)
    }
}

fun changeStar(oldStar: String, newStar: String): Boolean {
    val existing = objects.get(currentStarTile, oldStar)
    if (existing != null) {
        currentActiveObject = existing
        existing.replace(newStar)
        return true
    }
    return false
}

fun getInitialStar(): Int {
    return initialStar[Random.nextInt(initialStar.size)]
}

fun cleanseEvent(forceStopped: Boolean) {
    val existing = objects.get(currentStarTile, currentActiveObject.id) //get the current start
    if (existing != null) {
        existing.remove(existing.intId, true) // delete the start object
    }
    if(!forceStopped){
        val starSprite = npcs.add("star_sprite", currentStarTile, Direction.NONE, 0)
        World.run("start_sprite_despawn_timer", 600) { // 10 minutes
            npcs.remove(starSprite)
        }
    }
    totalCollected = 0
    this.currentStarTile = Tile(-1, -1)
    currentActiveObject = GameObject(-1,-1,-1,-1,-1,-1)
}

fun getRandomLocation(): StarLocationData {
    val location = StarLocationData.entries.toTypedArray()
    return location[Random.nextInt(location.size)]
}

val gems = setOf(
        "uncut_sapphire",
        "uncut_emerald",
        "uncut_ruby",
        "uncut_diamond"
)

val pickaxes = listOf(
        Item("dragon_pickaxe"),
        Item("volatile_clay_pickaxe"),
        Item("sacred_clay_pickaxe"),
        Item("inferno_adze"),
        Item("rune_pickaxe"),
        Item("adamant_pickaxe"),
        Item("mithril_pickaxe"),
        Item("steel_pickaxe"),
        Item("iron_pickaxe"),
        Item("bronze_pickaxe")
)

fun getBestPickaxe(player: Player): Item? {
    return pickaxes.firstOrNull { pickaxe -> hasRequirements(player, pickaxe, false) && player.holdsItem(pickaxe.id) }
}

fun hasRequirements(player: Player, pickaxe: Item?, message: Boolean = false): Boolean {
    if (pickaxe == null) {
        if (message) {
            player.message("You need a pickaxe to mine this rock.")
            player.message("You do not have a pickaxe which you have the mining level to use.")
        }
        return false
    }
    return player.hasRequirementsToUse(pickaxe, message, setOf(Skill.Mining, Skill.Firemaking))
}

fun addStarDust(player: Player, ore: String): Boolean {
    val added = player.inventory.add(ore)
    if (added) {
        player.message("You manage to mine some ${ore.toLowerSpaceCase()}.")
    } else {
        player.inventoryFull()
    }
    return added
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

objectOperate("Mine") {
    val isStar: Boolean = target.def["star", false]
    if(!isStar){
        return@objectOperate
    }
    if (target.id.startsWith("depleted")) {
        player.message("There is currently no ore available in this rock.")
        return@objectOperate
    }
    player.softTimers.start("mining")
    var first = true
    while (true) {
        if (!objects.contains(target)) {
            break
        }

        if (player.inventory.isFull()) {
            player.message("Your inventory is too full to hold any more ore.")
            break
        }

        val rock: Rock? = target.def.getOrNull("mining")
        if (rock == null || !player.has(Skill.Mining, rock.level, true)) {
            break
        }
        val pickaxe = getBestPickaxe(player)
        if (!hasRequirements(player, pickaxe, true) || pickaxe == null) {
            break
        }
        val delay = if (pickaxe.id == "dragon_pickaxe" && random.nextInt(6) == 0) 2 else pickaxe.def["mining_delay", 8]
        if (first) {
            player.message("You swing your pickaxe at the rock.", ChatType.Filter)
            first = false
        }
        val remaining = player.remaining("skill_delay")
        if (remaining < 0) {
            player.face(target)
            player.setAnimation("${pickaxe.id}_swing_low")
            player.start("skill_delay", delay)
            pause(delay)
        } else if (remaining > 0) {
            pause(delay)
        }
        if (rock.gems) {
            val glory = player.equipped(EquipSlot.Amulet).id.startsWith("amulet_of_glory_")
            if (Level.success(player.levels.get(Skill.Mining), if (glory) 3..3 else 1..1)) {
                addStarDust(player, gems.random())
                continue
            }
        }
        var ores = rock.ores
        if (target.id == "rune_essence_rocks") {
            val name = if (World.members && player.has(Skill.Mining, 30)) "pure_essence" else "rune_essence"
            ores = rock.ores.filter { it == name }
        }
        for (item in ores) {
            val ore = itemDefinitions.get(item)["mining", Ore.EMPTY]
            if (Level.success(player.levels.get(Skill.Mining), ore.chance)) {
                player.experience.add(Skill.Mining, ore.xp)
                totalCollected++
                handleMinedStarDust(target)
                val totalStarDust = player.inventory.count(item) + player.bank.count(item)
                if(totalStarDust >= ownedStarDustThreshold){
                    player.message("You have the maximum amount of star dust but was still rewarded experience.")
                } else {
                    if (!addStarDust(player, item)) {
                        player.clearAnimation()
                        break
                    }
                }
            }
        }
        player.stop("skill_delay")
    }
    player.softTimers.stop("mining")
}

objectApproach("Prospect") {
    val isStar: Boolean = target.def["star", false]
    if(!isStar){
        return@objectApproach
    }
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
    val isInventoryFull = player.inventory.isFull()
    val starDustCount = player.inventory.count("stardust")
    val messageBuilder = StringBuilder("Also, ")
    if(isInventoryFull){
        player.message("Inventory full. To make more room, sell, drop or bank something.", ChatType.Game)
    } else if(starDustCount == 0) {
        npc<Sad>("You don't seem to have any star dust that I can exchange for a reward")
    } else if(starDustCount > 0) {
        val rewards = calculateRewards(player.inventory.count("stardust"))
        player.inventory.remove("stardust", starDustCount)
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