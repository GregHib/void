import world.gregs.voidps.cache.definition.data.QuickChatType
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.QuickChatPhraseDecoder
import world.gregs.voidps.engine.client.message
import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.Players
import world.gregs.voidps.engine.entity.character.player.chat.PrivateQuickChat
import world.gregs.voidps.engine.entity.character.player.chat.PrivateQuickChatMessage
import world.gregs.voidps.engine.entity.character.player.chat.PublicQuickChat
import world.gregs.voidps.engine.entity.character.player.chat.PublicQuickChatMessage
import world.gregs.voidps.engine.entity.character.player.rights
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.update.visual.player.combatLevel
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.definition.VariableDefinitions
import world.gregs.voidps.engine.event.on
import world.gregs.voidps.engine.utility.inject
import world.gregs.voidps.network.encode.privateQuickChatFrom
import world.gregs.voidps.network.encode.privateQuickChatTo
import world.gregs.voidps.network.encode.publicQuickChat

val players: Players by inject()
val phrases: QuickChatPhraseDecoder by inject()
val variables: VariableDefinitions by inject()
val enums: EnumDecoder by inject()
val items: ItemDecoder by inject()

on<PrivateQuickChat> { player: Player ->
    val target = players.get(friend)
    if (target == null) {
        player.message("Unable to send message - player unavailable.")
        return@on
    }
    val definition = phrases.get(file)
    val data = generateData(player, file, data)
    player.client?.privateQuickChatTo(target.name, file, data)

    val text = definition.buildString(enums, items, data)
    val message = PrivateQuickChatMessage(player, file, text, data)
    target.events.emit(message)
}

on<PrivateQuickChatMessage>({ it.client != null }) { player: Player ->
    player.client?.privateQuickChatFrom(source.name, source.rights.ordinal, file, data)
}

on<PublicQuickChat> { player: Player ->
    val definition = phrases.get(file)
    val data = generateData(player, file, data)
    val text = definition.buildString(enums, items, data)
    val message = PublicQuickChatMessage(player, script, file, text, data)
    player.viewport.players.current.forEach {
        it.events.emit(message)
    }
}

on<PublicQuickChatMessage>({ it.client != null }) { player: Player ->
    player.client?.publicQuickChat(source.index, 0x8000, source.rights.ordinal, file, data)
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
                val key = variables.getKey(variable)!!
                return int(player.getVar(key))
            }
            QuickChatType.Varbit -> {
                val variable = definition.ids!!.first().first()
                val key = variables.getKey(variable)!!
                return int(player.getVar(key))
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