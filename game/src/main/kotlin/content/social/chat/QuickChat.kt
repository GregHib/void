package content.social.chat

import com.github.michaelbull.logging.InlineLogger
import content.social.clan.clan
import content.social.ignore.ignores
import world.gregs.voidps.cache.definition.data.QuickChatType
import world.gregs.voidps.engine.Script
import world.gregs.voidps.engine.client.instruction.instruction
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.client.variable.MapValues
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.AuditLog
import world.gregs.voidps.network.client.instruction.QuickChatPrivate
import world.gregs.voidps.network.client.instruction.QuickChatPublic
import world.gregs.voidps.network.login.protocol.encode.clanQuickChat
import world.gregs.voidps.network.login.protocol.encode.privateQuickChatFrom
import world.gregs.voidps.network.login.protocol.encode.privateQuickChatTo
import world.gregs.voidps.network.login.protocol.encode.publicQuickChat

class QuickChat(
    val players: Players,
    val phrases: QuickChatPhraseDefinitions,
    val variables: VariableDefinitions,
    val enums: EnumDefinitions,
    val items: ItemDefinitions,
) : Script {

    val logger = InlineLogger("QuickChat")

    init {
        instruction<QuickChatPrivate> { player ->
            val target = players.get(friend)
            if (target == null || target.ignores(player)) {
                player.message("Unable to send message - player unavailable.")
                return@instruction
            }
            val definition = phrases.get(file)
            val data = generateData(player, file, data)
            player.client?.privateQuickChatTo(target.name, file, data)
            val text = definition.buildString(enums.definitions, items.definitions, data)
            AuditLog.event(player, "told_qc", target, text)
            target.client?.privateQuickChatFrom(player.name, player.rights.ordinal, file, data)
        }

        instruction<QuickChatPublic> { player ->
            when (chatType) {
                0 -> {
                    val definition = phrases.get(file)
                    val data = generateData(player, file, data)
                    val text = definition.buildString(enums.definitions, items.definitions, data)
                    AuditLog.event(player, "said_qc", text)
                    players.filter { it.tile.within(player.tile, VIEW_RADIUS) && !it.ignores(player) }.forEach {
                        it.client?.publicQuickChat(player.index, 0x8000, player.rights.ordinal, file, data)
                    }
                }
                1 -> {
                    val clan = player.clan
                    if (clan == null) {
                        player.message("You must be in a clan chat to talk.", ChatType.ClanChat)
                        return@instruction
                    }
                    if (!clan.hasRank(player, clan.talkRank) || !clan.members.contains(player)) {
                        player.message("You are not allowed to talk in this clan chat channel.", ChatType.ClanChat)
                        return@instruction
                    }
                    val definition = phrases.get(file)
                    val data = generateData(player, file, data)
                    val text = definition.buildString(enums.definitions, items.definitions, data)
                    AuditLog.event(player, "clan_said_qc", clan, text)
                    clan.members.filterNot { it.ignores(player) }.forEach { member ->
                        member.client?.clanQuickChat(player.name, member.clan!!.name, player.rights.ordinal, file, data)
                    }
                }
            }
        }
    }

    fun generateData(player: Player, file: Int, data: ByteArray): ByteArray {
        val definition = phrases.get(file)
        val types = definition.types ?: return data
        if (types.size == 1) {
            when (definition.getType(0)) {
                QuickChatType.SkillLevel -> {
                    val skill = Skill.all[definition.ids!!.first().first()]
                    val level = player.levels.getMax(skill)
                    return byteArrayOf(level.toByte())
                }
                QuickChatType.Varp -> {
                    try {
                        val variable = definition.ids!!.first().first()
                        val key = variables.getVarp(variable)!!
                        val def = variables.get(key)!!
                        val int = def.values.toInt(player[key, def.defaultValue!!])
                        return int(int)
                    } catch (e: Exception) {
                        logger.error(e) { "Quick chat varp: $file ${data.contentToString()}" }
                        return byteArrayOf()
                    }
                }
                QuickChatType.Varbit -> {
                    try {
                        val variable = definition.ids!!.first().first()
                        val key = variables.getVarbit(variable)!!
                        val def = variables.get(key)!!
                        val int = def.values.toInt(player[key, def.defaultValue!!])
                        return int(int)
                    } catch (e: Exception) {
                        logger.error(e) { "Quick chat varbit: $file ${data.contentToString()}" }
                        return byteArrayOf()
                    }
                }
                QuickChatType.CombatLevel -> return byteArrayOf(player.combatLevel.toByte())
                QuickChatType.SlayerAssignment -> {
                    val int = (variables.get("slayer_target")!!.values as MapValues).values[player["slayer_target"]] ?: 0
                    return int(int)
                }
                QuickChatType.ClanRank,
                QuickChatType.AverageCombatLevel,
                QuickChatType.SoulWars,
                -> return byteArrayOf(0)
                else -> return data
            }
        } else {
            val list = mutableListOf<Byte>()
            for (index in types.indices) {
                when (definition.getType(index)) {
                    QuickChatType.SkillLevel, QuickChatType.SkillExperience -> {
                        val skill = Skill.all[definition.ids!![index].last()]
                        list.add(player.levels.getMax(skill).toByte())
                    }
                    else -> return data
                }
            }
            return list.map { it }.toByteArray()
        }
    }

    fun int(value: Int) = byteArrayOf((value shr 24).toByte(), (value shr 16).toByte(), (value shr 8).toByte(), value.toByte())
}
