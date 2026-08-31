package content.skill.hunter

import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.data.definition.Rows
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.equip.equipped
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.exp
import world.gregs.voidps.engine.entity.character.player.skill.level.Level.has
import world.gregs.voidps.engine.entity.character.sound
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.engine.inv.inventory
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.random

class KebbitTracking : Script {

    private data class Segment(
        val varbit: String,
        val tunnel: Boolean,
        val inverted: Boolean,
        val start: Tile,
        val end: Tile,
        val trigger: Tile,
        val burrow: String?,
    )

    private val trackingTrails = mutableMapOf<String, List<Segment>>()
    private val trackingSteps = mutableMapOf<String, Int>()

    init {
        objectOperate("Inspect", "common_kebbit_burrow,common_kebbit_burrow_2,polar_kebbit_hole,polar_kebbit_hole_2,desert_devil_burrow,desert_devil_burrow_2,feldip_weasel_burrow,feldip_weasel_burrow_2") { (target) ->
            inspectBurrow(target)
        }

        objectOperate("Inspect", "kebbit_tracks_plant_*,kebbit_tunnel_*,kebbit_hollow_log_*,desert_cactus_*,desert_rockslide_*,feldip_plant_*") { (target) ->
            inspectTrail(target)
        }

        objectOperate("Search", "kebbit_bush,kebbit_snow_drift,disturbed_sand,weasel_bush") { (target) ->
            inspectTrail(target)
        }

        objectOperate("Attack", "kebbit_bush,kebbit_snow_drift,disturbed_sand,weasel_bush") { (target) ->
            catch(target)
        }

        playerDespawn {
            clearTrail(this)
        }
    }

    private fun kebbit(id: String) = when {
        id.startsWith("polar") -> "polar_kebbit"
        id.startsWith("desert") -> "desert_devil"
        id.startsWith("feldip") -> "feldip_weasel"
        else -> "common_kebbit"
    }

    private fun segments(kebbit: String): List<Segment> = Tables.get("trails").rows().filter { it.string("kebbit") == kebbit }.map { row ->
        Segment(
            varbit = row.string("varbit"),
            tunnel = row.bool("tunnel"),
            inverted = row.bool("inverted"),
            start = row.tile("start"),
            end = row.tile("end"),
            trigger = row.tileOrNull("trigger") ?: row.tile("end"),
            burrow = row.objOrNull("burrow"),
        )
    }

    private fun tunnels(kebbit: String) = segments(kebbit).filter { it.tunnel }.flatMap { listOf(it.start, it.end) }.toSet()

    private fun linkingPool(kebbit: String): List<Segment> {
        val base = segments(kebbit)
        val tunnels = tunnels(kebbit)
        // Only polar kebbit trails can route back through a burrow's starting segments
        val pool = if (kebbit == "polar_kebbit") base else base.filter { it.burrow == null }
        return pool + pool.map { inverse(it, tunnels) }
    }

    private fun inverse(segment: Segment, tunnels: Set<Tile>) = Segment(
        varbit = segment.varbit,
        tunnel = segment.end in tunnels,
        inverted = !segment.inverted,
        start = segment.end,
        end = segment.start,
        trigger = segment.trigger,
        burrow = null,
    )

    private fun generate(kebbit: String, burrow: String, limit: Int, finals: List<Tile>): List<Segment>? {
        val trail = mutableListOf(segments(kebbit).filter { it.burrow == burrow }.random(random))
        val pool = linkingPool(kebbit)
        var spotsLeft = random.nextInt(2, limit + 1)
        var tries = 20
        while (spotsLeft > 0 || trail.last().end !in finals) {
            if (tries-- <= 0) {
                return trim(trail, finals)
            }
            val previous = trail.last()
            val possible = if (previous.tunnel) {
                pool.filter { it.tunnel && it.start != previous.end && it.start.level == previous.end.level && it.start.distanceTo(previous.end) <= 5 }
            } else {
                pool.filter { it.start == previous.end }
            }.filter { next -> trail.none { it.varbit == next.varbit } }
            if (possible.isEmpty()) {
                return trim(trail, finals)
            }
            val next = possible.random(random)
            trail.add(next)
            if (!next.tunnel) {
                spotsLeft--
            }
        }
        return trail
    }

    private fun trim(trail: MutableList<Segment>, finals: List<Tile>): List<Segment>? {
        while (trail.size > 1 && trail.last().end !in finals) {
            trail.removeAt(trail.lastIndex)
        }
        if (trail.last().end !in finals) {
            return null
        }
        return trail
    }

    private fun updateTrail(player: Player) {
        val trail = trackingTrails[player.accountName] ?: return
        val step = trackingSteps[player.accountName] ?: 0
        for (index in 0..step) {
            val segment = trail[index]
            player[segment.varbit] = if (segment.inverted) 5 else 4
        }
    }

    private fun clearTrail(player: Player) {
        val trail = trackingTrails.remove(player.accountName)
        trackingSteps.remove(player.accountName)
        if (trail != null) {
            for (segment in trail) {
                player[segment.varbit] = 0
            }
        }
    }

    private fun Player.inspectBurrow(target: GameObject) {
        if (trackingTrails.containsKey(accountName)) {
            inspectTrail(target)
            return
        }
        val kebbit = kebbit(target.id)
        val row = Rows.get("tracking.$kebbit")
        if (!has(Skill.Hunter, row.int("level"), message = true)) {
            return
        }
        val trail = generate(kebbit, target.id, row.int("limit"), row.tileList("finals"))
        if (trail == null) {
            message("You search but find nothing of interest.")
            return
        }
        trackingTrails[accountName] = trail
        trackingSteps[accountName] = 0
        updateTrail(this)
        message("You discover some tracks nearby.")
    }

    private fun Player.inspectTrail(target: GameObject) {
        val trail = trackingTrails[accountName]
        if (trail == null) {
            message("You search but find nothing.")
            return
        }
        val step = trackingSteps[accountName] ?: 0
        val current = if (step < trail.lastIndex) trail[step + 1] else trail[step]
        if (step == trail.lastIndex && current.end == target.tile) {
            message("It looks like something is moving around in there.")
            return
        }
        if (current.trigger == target.tile || current.end == target.tile) {
            trackingSteps[accountName] = step + 1
            updateTrail(this)
            message("You discover some tracks nearby.")
            return
        }
        message("You search but find nothing of interest.")
    }

    private suspend fun Player.catch(target: GameObject) {
        val trail = trackingTrails[accountName]
        if (trail == null) {
            message("You search but find nothing.")
            return
        }
        if (!inventory.contains("noose_wand") && equipped(EquipSlot.Weapon).id != "noose_wand") {
            message("You need a noose wand to catch the kebbit.")
            return
        }
        val step = trackingSteps[accountName] ?: 0
        val kebbit = kebbit(trail.first().varbit)
        val row = Rows.get("tracking.$kebbit")
        sound("hunting_noose")
        if (step != trail.lastIndex || trail[step].end != target.tile) {
            anim("noose_fail")
            delay(2)
            if (target.id == "disturbed_sand" && inventory.add("old_boot")) {
                message("The trail was false. You find an old boot buried in the sand.")
            } else {
                message("You fail to find anything with your noose wand.")
            }
            return
        }
        anim(row.anim("catch_anim"))
        delay(2)
        for (item in row.itemList("loot")) {
            inventory.add(item)
        }
        exp(Skill.Hunter, row.int("xp") / 10.0)
        clearTrail(this)
        message("You've caught a ${kebbit.replace('_', ' ')}!")
    }
}
