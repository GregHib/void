package content.skill.summoning

import content.entity.gfx.areaGfx
import content.entity.player.dialogue.type.choice
import content.skill.summoning.pet.callPet
import content.skill.summoning.pet.dismissPet
import content.skill.summoning.pet.pet
import content.skill.summoning.pet.updatePetInterface
import net.pearx.kasechange.toLowerSpaceCase
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.cache.definition.data.NPCDefinition
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.sendScript
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.mode.Follow
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.engine.entity.item.drop.DropTables
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.type.Tile

val Character?.isFamiliar: Boolean
    get() = this != null && this is NPC && id.endsWith("_familiar")

var Player.follower: NPC?
    get() {
        val index = get("follower_index", -1)
        return NPCs.indexed(index)
    }
    set(value) {
        if (value != null) {
            set("follower_index", value.index)
            set("follower_id", value.id)
        }
    }

/**
 * Summons the given familiar if the player doesn't already have a follower
 *
 * @param familiar The [NPCDefinition] of the familiar being summoned
 * @param restart A boolean used to tell if this familiar is being summoned at login. If set to false will start a new
 * familiar timer
 */
fun Player.summonFamiliar(familiar: NPCDefinition, restart: Boolean) {
    if (follower != null || pet != null) {
        message("You already have a follower.")
        return
    }

    val familiarNpc = NPCs.add(familiar.stringId, tile)
    familiarNpc.mode = Follow(familiarNpc, this)
    queue("summon_familiar", 2) {
        follower = familiarNpc
        familiarNpc["owner_index"] = index
        familiarNpc.anim("${familiarNpc.id.removeSuffix("_familiar")}_spawn")
        familiarNpc.gfx("summon_familiar_size_${familiarNpc.size}")
        // Tells the cast button on familiar_details how many points this familiar's special costs.
        set("summoning_special_points_needed", followerScrollId()?.let { ItemDefinitions.get(it)["special_points", 0] } ?: 0)
        updateFamiliarPvpForm()
        updateFamiliarInterface()
        if (!restart) {
            timers.start("familiar_timer")
            timers.start("summoning_drain")
        }
        if (get<DropTables>().get("forage_${familiarNpc.id.removeSuffix("_familiar")}") != null) {
            timers.start("forage")
        }
        if (familiarNpc.id in FAMILIAR_HEAL_LIFEPOINTS) {
            timers.start("familiar_heal")
        }
    }
}

/**
 * Dismisses the familiar following the player and resets the summoning orb and varbits back to their default
 * states. Also stops the familiar timer.
 */
fun Player.dismissFamiliar(removeNpc: Boolean = true) {
    dropBeastOfBurdenItems()
    removeFamiliarFarmingBoost()
    if (removeNpc) {
        NPCs.remove(follower)
    }
    follower = null
    interfaces.close("familiar_details")
    interfaces.close("beast_of_burden")
    sendScript("reset_summoning_orb")

    // Need to wait for the above sendScript to reach the client before resetting
    // Cast option for previous familiar will not be cleared from summoning_orb right-click menu otherwise
    queue("reset_familiar_vars", 1) {
        set("follower_details_name", 0)
        set("follower_details_chathead", 0)
        set("familiar_details_minutes_remaining", 0)
        set("familiar_details_seconds_remaining", 0)
        set("summoning_special_points_needed", 0)
    }
    timers.stop("familiar_timer")
    timers.stop("summoning_drain")
    timers.stop("forage")
    timers.stop("familiar_heal")
}

/**
 * Removes a familiar's Farming boost (dreadfowl/compost mound special) when the familiar leaves,
 * but only if it's still the active boost - a decayed or later (e.g. garden pie) boost is left be.
 */
fun Player.removeFamiliarFarmingBoost() {
    val boostedTo = get("familiar_farming_boost", 0)
    if (boostedTo <= 0) {
        return
    }
    if (levels.get(Skill.Farming) == boostedTo && boostedTo > levels.getMax(Skill.Farming)) {
        levels.set(Skill.Farming, levels.getMax(Skill.Farming))
    }
    clear("familiar_farming_boost")
}

/**
 * Updates the familiar interface (663) with the details of the player's current follower
 */
fun Player.updateFamiliarInterface() {
    val follower = follower ?: return
    interfaces.open("familiar_details")
    set("follower_details_name", EnumDefinitions.get("summoning_familiar_ids").getKey(follower.def.id))
    set("follower_details_chathead", follower.def.id)
    set("follower_details_chathead_animation", follower.id)
}

/**
 * Opens the interface used to set the left-click option of the summoning orb on the minimap
 */
fun Player.openFollowerLeftClickOptions() {
    interfaces.open("follower_left_click_options")
}

/**
 * Confirms the selected option in the follower_left_click_options interface and sets the var.
 */
fun Player.confirmFollowerLeftClickOptions() {
    // Default falls back to 0 (follower_details) because `PlayerVariables.set`
    // clears any persistent variable assigned its default value (`int` -> 0),
    // so picking the first radio leaves `summoning_menu_left_click_option`
    // empty rather than literally 0.
    set("summoning_orb_left_click_option", get("summoning_menu_left_click_option", 0))
    interfaces.close("follower_left_click_options")
}

/**
 * Teleports the player's follower to their position
 */
fun Player.callFollower() {
    val follower = follower ?: return
    val steps: StepValidator = get()
    var target: Tile? = null
    for (tile in tile.spiral(follower.size)) {
        if (tile == this.tile) {
            continue
        }
        if (!steps.canFit(tile, follower.collision, follower.size, follower.blockMove)) {
            continue
        }
        target = tile
        break
    }
    if (target == null) {
        message("Your familiar is too large to fit in the area you are standing in. Move into a larger space and try again.")
        return
    }
    follower.tele(target, clearMode = false)
    follower.watch(this)
    follower.anim("${follower.id.removeSuffix("_familiar")}_spawn")
    follower.gfx("summon_familiar_size_${follower.size}")
    if (follower.mode !is Follow) {
        follower.mode = Follow(follower, this)
    }
    updateFamiliarPvpForm()
}

/** Ticks the obelisk charge-up graphic plays for before the player performs the infuse animation. */
private const val OBELISK_RENEW_GRAPHIC_TICKS = 2

/**
 * Restores summoning points at an obelisk, mirroring prayer altar behaviour. The obelisk charges
 * up first ([summoning_renew_obelisk] graphic), then once it finishes the player performs the
 * infuse animation and graphic together.
 */
suspend fun Player.renewSummoningPoints(obelisk: GameObject) {
    if (levels.getOffset(Skill.Summoning) >= 0) {
        message("You already have full summoning points.")
    } else {
        areaGfx("summoning_renew_obelisk", obelisk.tile)
        delay(OBELISK_RENEW_GRAPHIC_TICKS)
        levels.set(Skill.Summoning, levels.getMax(Skill.Summoning))
        anim("summoning_renew")
        areaGfx("summoning_renew_player", tile)
        sound("summoning_renew")
        message("You renew your summoning points at the obelisk.")
    }
}

/**
 * Runs a familiar special move costing [cost] special-move points. Special moves draw from the
 * 0-60 special-move-points pool (regenerated each 30s by [SummoningTimers]) rather than from the
 * player's summoning points. Warns and does nothing if the pool is too low; only spends if [action]
 * runs, so callers should perform any other preconditions (ownership, situational checks) first.
 */
fun Player.useFamiliarSpecial(cost: Int, action: () -> Unit) {
    val points = get("summoning_special_points_remaining", 0)
    if (points < cost) {
        message("Your familiar does not have enough special move points left.")
        return
    }
    action()
    set("summoning_special_points_remaining", (points - cost).coerceAtLeast(0))
}

/**
 * The item id of the scroll matching the player's current follower, or null if the player has no
 * familiar or no scroll is mapped for it. Resolved follower npc -> pouch (`summoning_familiar_ids`)
 * -> scroll (`summoning_scroll_ids_2`).
 */
fun Player.followerScrollId(): Int? {
    val familiar = follower ?: return null
    val pouchId = EnumDefinitions.get("summoning_familiar_ids").getKey(familiar.def.id)
    if (pouchId == -1) {
        return null
    }
    return EnumDefinitions.get("summoning_scroll_ids_2").intOrNull(pouchId)
}

/**
 * Runs a scroll-driven familiar special move - the moves triggered from the cast button on the
 * `familiar_details` interface.
 * validates a 3-tick cooldown, sufficient special-move points, the familiar's scroll in the
 * inventory, and that the familiar is within 15 tiles. [effect] performs the move and returns true
 * on success; only then is one scroll removed, the points drained, the cooldown set, and the
 * scroll's `use_experience` Summoning xp awarded. Returns false (consuming nothing) when [effect]
 * soft-fails, e.g. an invalid target.
 */
fun Player.castFamiliarSpecial(effect: () -> Boolean) {
    val familiar = follower ?: return
    if (hasClock("familiar_special_delay")) {
        return
    }
    val scrollId = followerScrollId()
    if (scrollId == null) {
        message("Your familiar doesn't have a special move.")
        return
    }
    val scrollDef = ItemDefinitions.get(scrollId)
    val cost = scrollDef["special_points", 0]
    if (get("summoning_special_points_remaining", 0) < cost) {
        message("Your familiar does not have enough special move points left.")
        return
    }
    if (!inventory.contains(scrollDef.stringId)) {
        message("You do not have enough scrolls left to do this special move.")
        return
    }
    if (tile.distanceTo(familiar.tile) > 15) {
        message("Your familiar is too far away to use that scroll. Call it closer or move nearer to it.")
        return
    }
    // Set the cooldown before running the effect so a re-entrant dispatch in the same tick (see the
    // approach handlers in FamiliarSpecialMovesDispatch) can't fire the special twice. Scroll/points
    // are still only spent on a successful effect() below.
    start("familiar_special_delay", 3)
    if (!effect()) {
        return
    }
    anim("summoning_special_cast")
    gfx("summoning_special_cast")
    sound("summoning_special_cast")
    inventory.remove(scrollDef.stringId, 1)
    val points = get("summoning_special_points_remaining", 0)
    set("summoning_special_points_remaining", (points - cost).coerceAtLeast(0))
    exp(Skill.Summoning, scrollDef["use_experience", 0.0])
}

/**
 * Resets the familiar back to its maximum remaining time based on the summoned familiar. Removes the pouch from the player's
 * inventory and rewards xp.
 */
fun Player.renewFamiliar() {
    val follower = follower ?: return
    val pouchId = EnumDefinitions.get("summoning_familiar_ids").getKey(follower.def.id)
    val pouchItem = Item(ItemDefinitions.get(pouchId).stringId)
    val remaining = get("familiar_details_minutes_remaining", 0) * 60 + get("familiar_details_seconds_remaining", 0)
    if (remaining >= 170) {
        message("You need to have less than 2:50 remaining before you can renew your familiar.")
        return
    }
    if (!inventory.contains(pouchItem.id)) {
        message("You need a ${pouchItem.def.name.toLowerSpaceCase()} to renew your familiar's timer.")
        return
    }
    if (!inventory.remove(pouchItem.id)) {
        return
    }
    set("familiar_details_minutes_remaining", follower.def["summoning_time_minutes", 0])
    set("familiar_details_seconds_remaining", 0)
    follower.gfx("summon_familiar_size_${follower.size}")
    message("You use your remaining pouch to renew your familiar.")
}

class Summoning : Script {

    init {
        objectOperate("Renew-points") { (target) ->
            renewSummoningPoints(target)
        }

        itemOption("Summon", "*_pouch") { option ->
            val familiarLevel = EnumDefinitions.get("summoning_pouch_levels").int(option.item.def.id)
            val familiarId = EnumDefinitions.get("summoning_familiar_ids").int(option.item.def.id)
            val summoningXp = option.item.def["summon_experience", 0.0]
            val familiar = NPCDefinitions.get(familiarId)
            val summonCost = option.item.def["summon_points", 0]
            if (!has(Skill.Summoning, familiarLevel)) {
                message("You are not high enough level to use this pouch.")
                return@itemOption
            }
            if (follower != null || pet != null) {
                message("You already have a follower.")
                return@itemOption
            }
            if (levels.get(Skill.Summoning) < summonCost) {
                message("You do not have enough summoning points to summon this familiar.")
                return@itemOption
            }
            summonFamiliar(familiar, false)
            inventory.remove(option.item.id)
            levels.drain(Skill.Summoning, summonCost)
            set("summoning_special_points_remaining", 60)
            exp(Skill.Summoning, summoningXp)
        }

        interfaceOption("Select left-click option", id = "summoning_orb:leftclick_options") {
            openFollowerLeftClickOptions()
        }

        interfaceOption("Select", id = "follower_left_click_options:*") { option ->
            val component = option.component
            val varbitValue = when {
                component.startsWith("follower_details") -> 0
                component.startsWith("special_move") -> 1
                component.startsWith("attack") -> 2
                component.startsWith("call_follower") -> 3
                component.startsWith("dismiss_follower") -> 4
                component.startsWith("take_bob") -> 5
                component.startsWith("renew_familiar") -> 6
                else -> -1
            }

            set("summoning_menu_left_click_option", varbitValue)
        }

        interfaceOption("Confirm Selection", "follower_left_click_options:confirm") {
            confirmFollowerLeftClickOptions()
        }

        interfaceOption("Dismiss", id = "summoning_orb:*dismiss_follower") {
            when {
                follower != null -> dismissFamiliar()
                pet != null -> dismissPet()
                else -> message("You don't have a follower.")
            }
        }

        interfaceOption("Renew Familiar", id = "summoning_orb:*renew_familiar") {
            renewFamiliar()
        }

        interfaceOption("*", "familiar_details:dismiss") { option ->
            if (pet != null) {
                choice("Are you sure you want to release your pet?") {
                    option("Yes.") {
                        dismissPet()
                    }
                    option("No.")
                }
                return@interfaceOption
            }
            when (option.option) {
                "Dismiss Familiar" -> choice("Are you sure you want to dismiss your familiar?") {
                    option("Yes.") {
                        dismissFamiliar()
                    }
                    option("No.")
                }
                "Dismiss Now" -> dismissFamiliar()
            }
        }

        interfaceOption("*", "pet_details:dismiss") {
            choice("Are you sure you want to release your pet?") {
                option("Yes.") {
                    dismissPet()
                }
                option("No.")
            }
        }

        interfaceOption("Renew Familiar", "familiar_details:renew") {
            renewFamiliar()
        }

        interfaceOption("*", "familiar_details:call") {
            if (pet != null) callPet() else callFollower()
        }

        interfaceOption("*", "pet_details:call") {
            callPet()
        }

        interfaceOption("Call Follower", "summoning_orb:*call_follower") {
            when {
                follower != null -> callFollower()
                pet != null -> callPet()
                else -> message("You don't have a follower.")
            }
        }

        interfaceOption("Follower Details", "summoning_orb:leftclick_follower_details") {
            when {
                follower != null -> updateFamiliarInterface()
                pet != null -> updatePetInterface()
                else -> message("You don't have a follower.")
            }
        }

        playerSpawn {
            if (get("familiar_details_seconds_remaining", 0) == 0 && get("familiar_details_minutes_remaining", 0) == 0) {
                return@playerSpawn
            }

            val familiarDef = NPCDefinitions.get(get("follower_details_chathead", -1))
            variables.send("follower_details_name")
            variables.send("follower_details_chathead")
            variables.send("familiar_details_minutes_remaining")
            variables.send("familiar_details_seconds_remaining")
            variables.send("follower_details_chathead_animation")
            timers.restart("familiar_timer")
            timers.restart("summoning_drain")
            summonFamiliar(familiarDef, true)
        }

        npcDeath("*_familiar") { death ->
            death.respawn = false
            death.dropItems = false
            val owner = Players.indexed(this["owner_index", -1]) ?: return@npcDeath
            if (owner.follower?.index == index) {
                // Familiar slain in combat: drop its stored items and dismiss it.
                // The death flow despawns the NPC, so don't remove it again here.
                owner.dismissFamiliar(removeNpc = false)
            }
        }
    }
}
