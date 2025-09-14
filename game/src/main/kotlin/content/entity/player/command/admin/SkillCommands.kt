package content.entity.player.command.admin

import content.skill.prayer.PrayerConfigs
import content.skill.prayer.isCurses
import net.pearx.kasechange.toSentenceCase
import world.gregs.voidps.engine.client.clearCamera
import world.gregs.voidps.engine.client.command.adminCommand
import world.gregs.voidps.engine.client.command.adminCommands
import world.gregs.voidps.engine.client.command.command
import world.gregs.voidps.engine.client.command.intArg
import world.gregs.voidps.engine.client.command.stringArg
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.event.Script
import world.gregs.voidps.engine.inject
import world.gregs.voidps.engine.queue.softQueue
import kotlin.getValue

@Script
class SkillCommands {

    val players: Players by inject()
    val accounts: AccountDefinitions by inject()

    init {
        val skills = Skill.entries.map { it.name }.toSet()
        adminCommand("master", stringArg("player-name", "target player (default self)", optional = true), desc = "set all skills to level 99", handler = ::master)
        val self = command(
            stringArg("skill-name", "the name of the skill", autofill = skills),
            intArg("level", "level to set it to"),
            desc = "set any skill to a specific level",
            handler = ::set,
        )
        val other = command(
            stringArg("player-name", "the name of target player", autofill = accounts.displayNames.keys),
            stringArg("skill-name", "the name of the skill", autofill = skills),
            intArg("level", "level to set it to"),
            desc = "set any players skill to a specific level",
            handler = ::set,
        )
        adminCommands("set_level", self, other)
        adminCommand("reset", stringArg("player-name", "target player (default self)", optional = true), desc = "set all skills to level 1", handler = ::reset)
    }

    fun set(player: Player, args: List<String>) {
        val target = players.find(player, if (args.size == 3) args[0] else null) ?: return
        val skill = Skill.valueOf(args[if (args.size == 3) 1 else 0].toSentenceCase())
        val level = args.last().toInt()
        target.experience.set(skill, Level.experience(skill, level))
        player.levels.set(skill, level)
        player.softQueue("flash_reset", 1) {
            target.removeVarbit("skill_stat_flash", skill.name.lowercase())
        }
    }

    fun master(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        for (skill in Skill.all) {
            target.experience.set(skill, 14000000.0)
            target.levels.restore(skill, 1000)
        }
        target.softQueue("", 1) {
            player.clear("skill_stat_flash")
        }
    }

    fun reset(player: Player, args: List<String>) {
        val target = players.find(player, args.getOrNull(0)) ?: return
        for ((index, skill) in Skill.all.withIndex()) {
            target.experience.set(skill, Experience.defaultExperience[index])
            target.levels.set(skill, Levels.defaultLevels[index])
        }
        target[if (target.isCurses()) PrayerConfigs.QUICK_CURSES else PrayerConfigs.QUICK_PRAYERS] = emptyList<Any>()
        target["xp_counter"] = 0.0
        target.clearCamera()
    }
}
