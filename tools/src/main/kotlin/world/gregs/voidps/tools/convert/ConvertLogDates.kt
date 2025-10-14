package world.gregs.voidps.tools.convert

import world.gregs.voidps.engine.data.Settings
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object ConvertLogDates {
    var remove = false

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val logs = File(Settings["storage.players.logs"])
        for (file in logs.listFiles()!!) {
            val lines = file.readLines()
            val replaced = mutableSetOf<String>()
            try {
                // Skip already converted logs
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(lines.first().split("\t").last())
                if (!remove) {
                    println("Skipped $file")
                    continue
                }
                for (line in lines) {
                    replaced.add(line.substringBeforeLast("\t"))
                }
            } catch (e: Exception) {
                if (!remove) {
                    for (line in lines) {
                        val milliseconds = line.substringBefore("\t").toLong()
                        val formatted = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofEpochSecond(TimeUnit.MILLISECONDS.toSeconds(milliseconds), 0, ZoneOffset.UTC))
                        replaced.add("$line\t$formatted")
                    }
                }
            }
            file.writeText(replaced.joinToString("\n"))
            println("Completed $file")
        }
    }
}