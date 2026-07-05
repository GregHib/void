package content.skill.summoning

import content.entity.combat.attackers
import content.entity.combat.hit.hit
import content.entity.gfx.areaGfx
import content.entity.player.bank.bank
import content.entity.proj.shoot
import org.rsmod.game.pathfinder.StepValidator
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.hasClock
import world.gregs.voidps.engine.client.variable.start
import world.gregs.voidps.engine.entity.character.move.tele
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItems
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.beastOfBurden
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.engine.inv.remove
import world.gregs.voidps.engine.map.collision.canFit
import world.gregs.voidps.engine.map.spiral
import world.gregs.voidps.engine.queue.queue
import world.gregs.voidps.engine.timer.CLIENT_TICKS
import world.gregs.voidps.type.random

// A queued action fires initialDelay + 1 ticks after it is added (and a dropped floor item becomes
// visible the tick after that), so these delays sit one under the intended tick counts. The macaw
// searches for ~3 seconds (a 5-tick wait) before its drop graphic plays; the herb lands soon after.
/** Delay before the Herbcall drop graphic plays - fires 5 ticks (~3 seconds) after the cast. */
private const val HERBCALL_SEARCH_DELAY = 4

/** Delay before the herb lands - fires 6 ticks after the cast, part-way through the drop graphic. */
private const val HERBCALL_DROP_DELAY = 5

/** Delay before the fruit bat's drop animation - it flies up into the trees for ~4 ticks first. */
private const val FRUITFALL_DROP_DELAY = 3

/** Delay before the fruits land - a tick into the drop animation and its tile splashes. */
private const val FRUITFALL_LAND_DELAY = 4

/**
 * Utility familiar specials: send-the-familiar-to-fight, call/ambush, ground-drop foragers, and
 * banking. Combat-engage moves register as [FamiliarSpecialMoves.npc]; the rest are instant casts.
 * Pack Yak's Winter Storage picks an inventory item, so it registers as [FamiliarSpecialMoves.item]
 * (the cast button used on the item to bank).
 */
class FamiliarUtilitySpecials : Script {

    // Herbcall's herb pool, mirroring 2009scape's MacawNPC.HERBS - a uniform pick from every herb
    // type, low- and high-level alike (grimy, as the macaw digs them up).
    private val herbcallHerbs = listOf(
        "grimy_guam",
        "grimy_marrentill",
        "grimy_tarromin",
        "grimy_harralander",
        "grimy_ranarr",
        "grimy_toadflax",
        "grimy_spirit_weed",
        "grimy_irit",
        "grimy_avantoe",
        "grimy_kwuarm",
        "grimy_snapdragon",
        "grimy_cadantine",
        "grimy_lantadyme",
        "grimy_dwarf_weed",
        "grimy_torstol",
    )
    private val fruit = listOf("orange", "banana", "lemon", "lime", "pineapple")
    private val rawFish = listOf("raw_shrimps", "raw_cod", "raw_bass", "raw_mackerel")

    init {
        // Pester - the special simply sends the mosquito at the target.
        FamiliarSpecialMoves.npc("spirit_mosquito_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@npc false
            }
            commandFamiliarAttack(target, silent = true)
            true
        }

        // Goad - the graahk gores its target with two heavy melee strikes, then keeps up the fight.
        FamiliarSpecialMoves.npc("spirit_graahk_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@npc false
            }
            val graahk = follower ?: return@npc false
            graahk.watch(target)
            graahk.anim("goad")
            repeat(2) {
                graahk.hit(target, offensiveType = "melee", damage = random.nextInt(121), delay = 0)
            }
            if (graahk !in target.attackers) {
                target.attackers.add(graahk)
            }
            commandFamiliarAttack(target, silent = true)
            true
        }

        // Ambush - the kyatt pounces from nowhere onto its target, landing one heavy strike.
        FamiliarSpecialMoves.npc("spirit_kyatt_familiar") { target ->
            if (!familiarCanSpecial(target)) {
                return@npc false
            }
            val kyatt = follower ?: return@npc false
            val validator: StepValidator = get()
            val landing = target.tile.spiral(kyatt.size).asSequence().firstOrNull {
                it != target.tile && validator.canFit(it, kyatt.collision, kyatt.size, kyatt.blockMove)
            }
            if (landing == null) {
                message("Your kyatt can't find a place to land on that target right now.")
                return@npc false
            }
            kyatt.tele(landing, clearMode = false)
            kyatt.watch(target)
            kyatt.anim("ambush")
            kyatt.gfx("ambush")
            kyatt.hit(target, offensiveType = "melee", damage = random.nextInt(225), delay = 0)
            if (kyatt !in target.attackers) {
                target.attackers.add(kyatt)
            }
            commandFamiliarAttack(target, silent = true)
            true
        }

        // Call to Arms - the Void familiars teleport their owner to the Void Knights' Outpost.
        FamiliarSpecialMoves.instant(
            "void_ravager_familiar",
            "void_shifter_familiar",
            "void_spinner_familiar",
            "void_torcher_familiar",
        ) {
            queue("call_to_arms", 1) {
                anim("call_to_arms")
                gfx("call_to_arms")
            }
            queue("call_to_arms_land", 3) {
                tele(2659, 2658, 0)
                anim("call_to_arms_land")
                gfx("call_to_arms_land")
            }
            true
        }

        // Cheese Feast - the albino rat produces 4 cheese into its own inventory
        FamiliarSpecialMoves.instant("albino_rat_familiar") {
            ensureBeastOfBurdenInventory()
            val free = beastOfBurdenCapacity - beastOfBurden.items.count { it.isNotEmpty() }
            if (free < 4) {
                message("Your familiar is too full to collect items.")
                return@instant false
            }
            familiarSelfSpecial(anim = "cheese_feast", sourceGfx = "cheese_feast") {
                beastOfBurden.add("cheese", 4)
                if (interfaces.contains("beast_of_burden")) {
                    syncBeastOfBurdenInterface()
                }
            }
        }

        // Egg Spawn - the spirit spider scatters red spider eggs onto free tiles around the player
        FamiliarSpecialMoves.instant("spirit_spider_familiar") {
            val steps: StepValidator = get()
            val eggTiles = tile.spiral(1).asSequence()
                .filter { it != tile && steps.canFit(it, collision, 1, blockMove) }
                .toList()
                .shuffled()
                .take(random.nextInt(5) + 1)
            familiarSelfSpecial(anim = "egg_spawn") {
                for (eggTile in eggTiles) {
                    areaGfx("egg_spawn", eggTile)
                }
                queue("egg_spawn", 1) {
                    for (eggTile in eggTiles) {
                        FloorItems.add(eggTile, "red_spiders_eggs", revealTicks = 120, disappearTicks = 30, owner = this)
                    }
                }
            }
        }

        // Generate Compost - cast on an empty compost bin to fill it with compost (10% supercompost).
        FamiliarSpecialMoves.obj("compost_mound_familiar") { obj ->
            if (!obj.id.startsWith("farming_compost_bin_")) {
                message("This scroll can only be used on an empty compost bin.")
                return@obj false
            }
            val variable = obj.id.removePrefix("farming_")
            if (get(variable, "empty") != "empty") {
                message("This scroll can only be used on an empty compost bin.")
                return@obj false
            }
            val familiar = follower ?: return@obj false
            familiar.face(obj.tile)
            familiar.anim("generate_compost")
            familiar.gfx("generate_compost")
            val flight = familiar.shoot("generate_compost_proj", obj.tile)
            queue("generate_compost", CLIENT_TICKS.toTicks(flight)) {
                areaGfx("generate_compost_bin", obj.tile)
                val superCompost = random.nextInt(10) == 0
                this[variable] = if (superCompost) "supercompost_15" else "compost_15"
            }
            true
        }

        // Herbcall - the macaw flaps up and searches for a herb; when the fly-up animation finishes it
        // drops a random grimy herb where it hovered and is called back down to its owner. As in
        // 2009scape it has a one-minute cooldown of its own on top of the usual scroll/point cost.
        FamiliarSpecialMoves.instant("macaw_familiar") {
            val familiar = follower ?: return@instant false
            if (hasClock("herbcall_delay")) {
                message("You must wait one minute until using the macaw's special again.")
                return@instant false
            }
            familiar.anim("herbcall")
            val herb = herbcallHerbs[random.nextInt(herbcallHerbs.size)]
            // Three seconds into the search the drop graphic bursts where the macaw hovers (an area
            // gfx so the follow-up recall teleport doesn't drag it away)...
            queue("herbcall_search", HERBCALL_SEARCH_DELAY) {
                areaGfx("herbcall", familiar.tile)
            }
            // ...and a second later the herb lands where it hovered and the macaw is called back down.
            queue("herbcall_drop", HERBCALL_DROP_DELAY) {
                FloorItems.add(familiar.tile, herb, disappearTicks = 300, owner = this@instant)
                callFollower()
            }
            start("herbcall_delay", 100)
            true
        }

        // Fruitfall - the bat flies up into the trees, then swoops down shaking loose up to six
        // fruits, each landing with a splash on its own free tile around the owner, the first
        // always a papaya. An unlucky cast (about 1 in 7) shakes down nothing at all but counts.
        FamiliarSpecialMoves.instant("fruit_bat_familiar") {
            val bat = follower ?: return@instant false
            bat.anim("fruitfall_ascend")
            this.gfx("fruitfall_ascend")
            val validator: StepValidator = get()
            val fruitTiles = tile.spiral(1).asSequence()
                .filter { it != tile && validator.canFit(it, collision, 1, blockMove) }
                .toList()
                .shuffled()
                .take(random.nextInt(7))
            queue("fruitfall_drop", FRUITFALL_DROP_DELAY) {
                bat.anim("fruitfall")
                for (fruitTile in fruitTiles) {
                    areaGfx("fruitfall_land", fruitTile)
                }
            }
            queue("fruitfall_land", FRUITFALL_LAND_DELAY) {
                for ((index, fruitTile) in fruitTiles.withIndex()) {
                    FloorItems.add(fruitTile, if (index == 0) "papaya_fruit" else fruit[random.nextInt(fruit.size)], disappearTicks = 120, owner = this)
                }
            }
            true
        }

        // Fish Rain - the ibis calls down a single low-level fish beside itself.
        FamiliarSpecialMoves.instant("ibis_familiar") {
            val ibis = follower ?: return@instant false
            ibis.anim("fish_rain")
            ibis.gfx("fish_rain")
            queue("fish_rain", 2) {
                FloorItems.add(ibis.tile, rawFish[random.nextInt(rawFish.size)], disappearTicks = 300, owner = this)
            }
            true
        }

        // Essence Shipment - bank all rune and pure essence carried, the familiar's pack included.
        FamiliarSpecialMoves.instant("abyssal_titan_familiar") {
            val titan = follower ?: return@instant false
            ensureBeastOfBurdenInventory()
            val moved = bankAll("rune_essence") + bankAll("pure_essence") +
                bankAllCarried("rune_essence") + bankAllCarried("pure_essence")
            if (moved == 0) {
                message("You have no essence for your familiar to bank.")
                return@instant false
            }
            titan.anim("essence_shipment")
            titan.gfx("essence_shipment")
            message("Your familiar banks your essence.")
            true
        }

        // Winter Storage - bank the inventory item the player uses the Cast option on. Using an
        // item directly on the yak stores it in its pack (the beast-of-burden handler always wins
        // that click), so the special lives on the cast button alone.
        FamiliarSpecialMoves.instant("pack_yak_familiar") {
            message("To cast Winter Storage, use the Cast option on the item you wish to bank.")
            false
        }
        FamiliarSpecialMoves.item("pack_yak_familiar") { item ->
            if (item.id == "winter_storage_scroll") {
                message("The yak refuses to bank the scroll powering its special.")
                return@item false
            }
            if (!inventory.contains(item.id)) {
                return@item false
            }
            inventory.remove(item.id, 1)
            bank.add(item.id, 1)
            follower?.gfx("winter_storage")
            follower?.say("Baroo!")
            message("Your pack yak sends the ${item.def.name.lowercase()} to your bank.")
            true
        }
    }

    /** Drops [id] on the player's tile (it despawns after ~3 minutes), counting as a successful cast. */
    private fun Player.dropForage(id: String): Boolean {
        FloorItems.add(tile, id, disappearTicks = 300, owner = this)
        return true
    }

    /** Moves every [id] from the inventory to the bank, returning how many were moved. */
    private fun Player.bankAll(id: String): Int {
        val count = inventory.count(id)
        if (count > 0) {
            inventory.remove(id, count)
            bank.add(id, count)
        }
        return count
    }

    /** Moves every [id] from the familiar's pack to the bank, returning how many were moved. */
    private fun Player.bankAllCarried(id: String): Int {
        val count = beastOfBurden.count(id)
        if (count > 0) {
            beastOfBurden.remove(id, count)
            bank.add(id, count)
        }
        return count
    }
}
