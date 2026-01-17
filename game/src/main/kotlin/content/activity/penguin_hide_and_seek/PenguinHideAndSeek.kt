package content.activity.penguin_hide_and_seek

import content.entity.effect.transform
import content.entity.player.command.find
import content.quest.questCompleted
import content.quest.questJournal
import net.pearx.kasechange.toTitleCase
import world.gregs.config.Config
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.modCommand
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.ui.chat.Colours
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.client.ui.chat.toTag
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.data.definition.AreaTypes
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.isAdmin
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

class PenguinHideAndSeek(
    val accounts: AccountDefinitions,
) : Script {

    val penguins = arrayOfNulls<NPC>(10)
    var week = -1
    var bear = "hidden"

    private val bearLocations = listOf(
        "rellekka",
        "varrock",
        "rimmington",
        "musa_point",
        "ardougne",
        "falador",
    )

    init {
        npcApproach("Spy-on", "*_penguin,*_turkey") { (target) ->
            approachRange(5)
            updateWeek(this)
            if (!addVarbit("penguins_found", target.id.removePrefix("hidden_"))) {
                // https://youtu.be/E1roiyC8QD4?si=10fPzRMq_UMZ9nkb&t=83
                message("You've already spotted this penguin spy.")
                return@npcApproach
            }
            watch(target)
            anim("spot_penguin")
            // https://youtu.be/E1roiyC8QD4?si=C4nJB4swiJzlTtTQ&t=50
            message("You spy on the penguin.")
            val doublePoints = questCompleted("cold_war") && target.id.removePrefix("hidden_penguin_").toInt() > 4
            inc("penguin_points", if (doublePoints) 2 else 1)
            inc("penguins_found_weekly")
            delay(2)
            clearWatch()
        }

        objectApproach("Inspect", "polar_bear_well*") { (target) ->
            approachRange(5)
            updateWeek(this)
            if (!addVarbit("penguins_found", "polar_bear")) {
                message("You've already spotted this polar bear agent.")
                return@objectApproach
            }
            face(target.tile)
            anim("spot_penguin")
            // https://youtu.be/PrkWAZmuEnw?si=qTL9V6MqLc3EUmSF&t=100
            message("You found the polar bear agent.")
            inc("penguin_points")
            inc("penguins_found_weekly")
            delay(2)
        }

        itemOption("Read", "spy_notebook") {
            updateWeek(this)
            val bear = containsVarbit("penguins_found", "polar_bear")
            var found = get("penguins_found_weekly", 0)
            if (bear) {
                found--
            }
            message("You have recently spotted $found ${"penguin".plural(found)}.")
            if (bear) {
                message("You have recently spotted the polar bear agent.")
            }
            message("You have ${get("penguin_points", 0)} Penguin Points to spend with Larry.")
        }

        adminCommand("respawn_penguins", desc = "Respawn hide and seek penguins") {
            clear()
            load(configFiles())
            sendBear()
        }
        adminCommand("clear_penguins", desc = "Remove all hide and seek penguins") { clear() }
        modCommand("penguins", stringArg("player-name", autofill = accounts.displayNames.keys, optional = true), desc = "Get info about a hide and seek penguin", handler = ::listPenguins)
        worldSpawn(::load)
        playerSpawn(::sendBear)
    }

    private fun updateWeek(player: Player) {
        if (player["penguin_week", -1] == week) {
            return
        }
        player.clear("penguins_found_weekly")
        player.clear("penguins_found")
        player["penguin_week"] = week
    }

    fun load(files: ConfigFiles) {
        if (!Settings["events.penguinHideAndSeek.enabled", false]) {
            return
        }
        val day = DayOfWeek.of(Settings["events.penguinHideAndSeek.resetDay", 3])
        timedLoad("penguin location") {
            week = weeksSince(day)
            val random = Random(week)
            val easy = load(files, "spawns.penguins.easy")
            var spots = easy.shuffled(random).take(5)
            var i = 0
            for ((type, tile) in spots) {
                val penguin = NPCs.add("hidden_penguin_$i", tile)
                penguin.transform(disguise(type), collision = false)
                penguins[i++] = penguin
            }
            val hard = load(files, "spawns.penguins.hard")
            spots = hard.shuffled(random).take(5)
            for ((type, tile) in spots) {
                val penguin = NPCs.add("hidden_penguin_$i", tile)
                penguin.transform(disguise(type), collision = false)
                penguins[i++] = penguin
            }
            bear = bearLocations.random(random)
            easy.size + hard.size
        }
        World.clearQueue("penguins_event_timer")
        World.queue("penguins_event_timer", ticksUntil(day)) {
            clear()
            load(files)
            sendBear()
        }
    }

    fun sendBear(player: Player) {
        player["polar_bear_well"] = if (player.questCompleted("hunt_for_red_rektuber")) bear else "hidden"
    }

    fun sendBear() {
        for (player in Players) {
            sendBear(player)
        }
    }

    /**
     * Clear all penguins
     */
    fun clear() {
        for (i in penguins.indices) {
            val penguin = penguins[i] ?: continue
            NPCs.remove(penguin)
            penguins[i] = null
        }
        bear = "hidden"
        sendBear()
    }

    /**
     * Number of ticks till the next penguin respawn [day]
     */
    fun ticksUntil(day: DayOfWeek, now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): Int {
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
    fun disguise(type: String, now: LocalDate = LocalDate.now()): String {
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
    fun weeksSince(day: DayOfWeek, now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)): Int {
        val epoch = LocalDate.of(2008, 9, 1).with(TemporalAdjusters.nextOrSame(day))
        val daysSinceReset = (now.dayOfWeek.value - day.value + 7) % 7
        val lastReset = now
            .minusDays(daysSinceReset.toLong())
            .truncatedTo(ChronoUnit.DAYS)
        return ChronoUnit.WEEKS.between(epoch, lastReset).toInt()
    }

    private fun load(files: ConfigFiles, path: String): List<Pair<String, Tile>> {
        val locations = mutableListOf<Pair<String, Tile>>()
        Config.fileReader(files.find(Settings[path])) {
            while (nextSection()) {
                section()
                var disguise = ""
                var x = 0
                var y = 0
                while (nextPair()) {
                    val key = key()
                    when (key) {
                        "disguise" -> disguise = string()
                        "x" -> x = int()
                        "y" -> y = int()
                        else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                    }
                }
                locations.add(disguise to Tile(x, y))
            }
        }
        return locations
    }

    private fun listPenguins(player: Player, args: List<String>) {
        val list = mutableListOf(
            "Penguin Points: ${player["penguin_points", 0]}",
            "Penguins found this week: ${player["penguins_found_weekly", 0]}",
            "",
        )
        val target = Players.find(player, args.getOrNull(0)) ?: return
        for ((index, penguin) in penguins.withIndex()) {
            if (penguin == null) {
                continue
            }
            val areas = AreaTypes.get(penguin.tile.zone).firstOrNull { it.tags.contains("penguin_area") }
            var hint = ""
            if (areas != null) {
                list.add("${Colours.BLUE.toTag()}${areas.name.toTitleCase()}")
                hint = areas["hint", ""]
            } else {
                list.add("${Colours.BLUE.toTag()}Penguin ${index + 1}")
            }
            if (target.containsVarbit("penguins_found", "penguin_$index")) {
                list.add("${Colours.DARK_GREEN.toTag()}${penguin.transform.toTitleCase()}")
            } else {
                list.add(penguin.transform.toTitleCase())
            }
            if (player.isAdmin()) {
                list.add("x = ${penguin.tile.x}, y = ${penguin.tile.y}")
            }
            if (hint.isNotBlank()) {
                list.add(hint)
            }
            list.add("")
        }
        list.add("${Colours.BLUE.toTag()}${bear.toTitleCase()}")
        if (target.containsVarbit("penguins_found", "polar_bear")) {
            list.add("${Colours.DARK_GREEN.toTag()}Polar Bear")
        } else {
            list.add("Polar Bear")
        }
        player.questJournal("Penguin Hide and Seek", list)
    }
}
