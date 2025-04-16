package content.social.chat

import world.gregs.voidps.cache.definition.data.QuickChatType
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.update.view.Viewport.Companion.VIEW_RADIUS
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.QuickChatPhraseDefinitions
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import world.gregs.voidps.engine.entity.character.player.*
import world.gregs.voidps.engine.entity.character.player.chat.ChatType
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanQuickChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.friend.PrivateQuickChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.global.PublicQuickChatMessage
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.event.onEvent
import world.gregs.voidps.engine.inject
import world.gregs.voidps.network.login.protocol.encode.clanQuickChat
import world.gregs.voidps.network.login.protocol.encode.privateQuickChatFrom
import world.gregs.voidps.network.login.protocol.encode.privateQuickChatTo
import world.gregs.voidps.network.login.protocol.encode.publicQuickChat
import content.social.clan.clan
import content.social.ignore.ignores
import world.gregs.voidps.engine.client.instruction.onInstruction
import world.gregs.voidps.network.client.instruction.QuickChatPrivate
import world.gregs.voidps.network.client.instruction.QuickChatPublic

val players: Players by inject()
val phrases: QuickChatPhraseDefinitions by inject()
val variables: VariableDefinitions by inject()
val enums: EnumDefinitions by inject()
val items: ItemDefinitions by inject()

onInstruction<QuickChatPrivate> { player ->
    val target = players.get(friend)
    if (target == null || target.ignores(player)) {
        player.message("Unable to send message - player unavailable.")
        return@onInstruction
    }
    val definition = phrases.get(file)
    val data = generateData(player, file, data)
    player.client?.privateQuickChatTo(target.name, file, data)

    val text = definition.buildString(enums.definitions, items.definitions, data)
    val message = PrivateQuickChatMessage(player, file, text, data)
    target.emit(message)
}

onEvent<Player, PrivateQuickChatMessage> { player ->
    player.client?.privateQuickChatFrom(source.name, source.rights.ordinal, file, data)
}

onInstruction<QuickChatPublic> { player ->
    when (chatType) {
        0 -> {
            val definition = phrases.get(file)
            val data = generateData(player, file, data)
            val text = definition.buildString(enums.definitions, items.definitions, data)
            val message = PublicQuickChatMessage(player, chatType, file, text, data)
            players.filter { it.tile.within(player.tile, VIEW_RADIUS) && !it.ignores(player) }.forEach {
                it.emit(message)
            }
        }
        1 -> {
            val clan = player.clan
            if (clan == null) {
                player.message("You must be in a clan chat to talk.", ChatType.ClanChat)
                return@onInstruction
            }
            if (!clan.hasRank(player, clan.talkRank) || !clan.members.contains(player)) {
                player.message("You are not allowed to talk in this clan chat channel.", ChatType.ClanChat)
                return@onInstruction
            }
            val definition = phrases.get(file)
            val data = generateData(player, file, data)
            val text = definition.buildString(enums.definitions, items.definitions, data)
            val message = ClanQuickChatMessage(player, chatType, file, text, data)
            clan.members.filterNot { it.ignores(player) }.forEach {
                it.emit(message)
            }
        }
    }
}

onEvent<Player, PublicQuickChatMessage> { player ->
    player.client?.publicQuickChat(source.index, 0x8000, source.rights.ordinal, file, data)
}

onEvent<Player, ClanQuickChatMessage> { player ->
    player.client?.clanQuickChat(source.name, player.clan!!.name, source.rights.ordinal, file, data)
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
                val variable = definition.ids!!.first().first()
                val key = variables.getVarp(variable)!!
                return int(player[key]!!)
            }
            QuickChatType.Varbit -> {
                val variable = definition.ids!!.first().first()
                val key = variables.getVarbit(variable)!!
                return int(player[key]!!)
            }
            QuickChatType.CombatLevel -> return byteArrayOf(player.combatLevel.toByte())
            QuickChatType.SlayerAssignment,
            QuickChatType.ClanRank,
            QuickChatType.AverageCombatLevel,
            QuickChatType.SoulWars -> return byteArrayOf(0)
            else -> return data
        }
    } else {
        val list = mutableListOf<Int>()
        for (index in types.indices) {
            when (definition.getType(index)) {
                QuickChatType.SkillLevel, QuickChatType.SkillExperience -> {
                    val skill = Skill.all[definition.ids!![index].last()]
                    list.add(player.levels.getMax(skill))
                }
                else -> return data
            }
        }
        return list.map { it.toByte() }.toByteArray()
    }
}

fun int(value: Int) = byteArrayOf((value shr 24).toByte(), (value shr 16).toByte(), (value shr 8).toByte(), value.toByte())