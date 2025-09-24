package content.activity.penguin_hide_and_seek

import content.entity.effect.transform
import world.gregs.config.Config
import world.gregs.voidps.engine.Api
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.find
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.timer.toTicks
import world.gregs.voidps.type.Tile
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@Script
class PenguinHideAndSeek : Api {

    val npcs: NPCs by inject()

    val penguins = arrayOfNulls<NPC>(10)

    private data class PenguinLocation(val type: String, val tile: Tile, val hint: String)

    init {
        // TODO convert hints into areas
        //      Hint by select random penguin and checking it's area
        //      Track spotted penguins in custom varp
        //      Track week they were spotted in
        //      If last tracked week is old reset weekly counter
        //      Add points on spotting with quest multiplier
        //      Add larry and rewards

        adminCommand("respawn_penguins", desc = "Respawn hide and seek penguins") { player, _ ->
            clear()
            worldSpawn(configFiles())
        }

        adminCommand("clear_penguins", desc = "Remove all hide and seek penguins") { player, _ ->
            clear()
        }

        adminCommand("penguins", intArg("index", optional = true), desc = "Get info about a hide and seek penguin") { player, _ ->
            // TODO exact location, type and hint
        }
    }

    override fun worldSpawn(files: ConfigFiles) {
        if (!Settings["events.penguinHideAndSeek.enabled", false]) {
            return
        }
        val day = DayOfWeek.of(Settings["events.penguinHideAndSeek.resetDay", 3])
        timedLoad("penguin location") {
            val locations = load(files)
            val weeks = weeksSince(day)
            val random = Random(weeks)
            val spots = locations.shuffled(random).take(10)
            var i = 0
            for (spot in spots) {
                val penguin = npcs.add("hidden_penguin_${i}", spot.tile)
                penguin.transform(disguise(spot.type), collision = false)
                penguins[i++] = penguin
            }
            locations.size
        }

        World.clearQueue("penguins_event_timer")
        World.queue("penguins_event_timer", ticksUntil(day)) {
            clear()
            worldSpawn(files)
        }
    }

    /**
     * Clear all penguins
     */
    private fun clear() {
        for (penguin in penguins) {
            npcs.remove(penguin)
        }
    }

    /**
     * Number of ticks till the next penguin respawn [day]
     */
    fun ticksUntil(day: DayOfWeek): Int {
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        var nextWed = now.withHour(0).withMinute(0).withSecond(0).withNano(0)
            .with(TemporalAdjusters.next(day))
        if (!nextWed.isAfter(now)) {
            nextWed = nextWed.plusWeeks(1)
        }
        return TimeUnit.SECONDS.toTicks(ChronoUnit.SECONDS.between(now, nextWed).toInt())
    }

    /**
     * Pick a disguise
     */
    private fun disguise(type: String): String {
        val now = LocalDate.now()
        if (now.month == Month.DECEMBER) {
            return "snowman_penguin"
        }
        val thanksGiving = LocalDate.of(now.year, Month.NOVEMBER, 1)
            .with(TemporalAdjusters.dayOfWeekInMonth(4, DayOfWeek.THURSDAY))
        if (!now.isBefore(thanksGiving.with(DayOfWeek.MONDAY)) && !now.isAfter(thanksGiving.with(DayOfWeek.SUNDAY))) {
            return "${type}_turkey"
        }
        val halloween = LocalDate.of(now.year, Month.OCTOBER, 31)
        val halloweenWeekStart = halloween.minusDays(6)
        if (!now.isBefore(halloweenWeekStart) && !now.isAfter(halloween)) {
            return "pumpkin_penguin"
        }
        return "${type}_penguin"
    }

    /**
     * Count the number of weeks since a fixed start date
     */
    private fun weeksSince(day: DayOfWeek): Long {
        val epoch = LocalDate.of(2008, 9, 1).with(TemporalAdjusters.nextOrSame(day))
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        val daysSinceReset = (now.dayOfWeek.value - day.value + 7) % 7
        val lastReset = now
            .minusDays(daysSinceReset.toLong())
            .truncatedTo(ChronoUnit.DAYS)
        return ChronoUnit.WEEKS.between(epoch, lastReset)
    }

    private fun load(files: ConfigFiles): List<PenguinLocation> {
        val locations = mutableListOf<PenguinLocation>()
        Config.fileReader(files.find(Settings["spawns.penguins"])) {
            while (nextSection()) {
                section()
                var disguise = ""
                var x = 0
                var y = 0
                var hint = ""
                while (nextPair()) {
                    val key = key()
                    when (key) {
                        "name" -> string()
                        "disguise" -> disguise = string()
                        "x" -> x = int()
                        "y" -> y = int()
                        "hint" -> hint = string()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                locations.add(PenguinLocation(disguise, Tile(x, y), hint))
            }
        }
        return locations
    }
}
