package world.gregs.voidps.tools.model

import content.bot.chat.BotChatModel
import content.bot.chat.process.Normaliser
import content.bot.chat.tag.ChatEntityTagger
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.AnimationDecoder
import world.gregs.voidps.cache.definition.decoder.EnumDecoder
import world.gregs.voidps.cache.definition.decoder.GraphicDecoder
import world.gregs.voidps.cache.definition.decoder.ItemDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.cache.definition.decoder.ObjectDecoder
import world.gregs.voidps.engine.data.ConfigFiles
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AnimationDefinitions
import world.gregs.voidps.engine.data.definition.EnumDefinitions
import world.gregs.voidps.engine.data.definition.GraphicDefinitions
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.data.definition.QuestDefinitions
import world.gregs.voidps.engine.data.definition.Tables
import world.gregs.voidps.engine.data.definition.VariableDefinitions
import java.io.File

/**
 * Loads just the game data bot chat needs: items, npcs, quests, cache enums and the chat tables.
 */
class BotChatData {
    val files: ConfigFiles
    val normaliser: Normaliser
    val quests: QuestDefinitions

    init {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        files = configFiles()
        ItemDefinitions.init(ItemDecoder().load(cache)).load(files.list(Settings["definitions.items"]))
        ObjectDefinitions.init(ObjectDecoder(true, lowDetail = false).load(cache)).load(files.list(Settings["definitions.objects"]))
        NPCDefinitions.init(NPCDecoder(true).load(cache)).load(files.list(Settings["definitions.npcs"]))
        AnimationDefinitions.init(AnimationDecoder().load(cache)).load(files.list(Settings["definitions.animations"]))
        GraphicDefinitions.init(GraphicDecoder().load(cache)).load(files.list(Settings["definitions.graphics"]))
        VariableDefinitions.load(files)
        // Cache enums only, the custom enum configs need the full server loaded
        EnumDefinitions.init(EnumDecoder().load(cache))
        Tables.load(files.list(Settings["definitions.tables"]).filter { File(it).name.startsWith("chat_") })
        quests = QuestDefinitions().load(files.find(Settings["definitions.quests"]))
        normaliser = Normaliser.load()
    }

    fun examples(): Map<String, List<String>> = BotChatModel.examples(files.list(Settings["bots.chat.intents"]))

    fun entityTagger(examples: Map<String, List<String>>): ChatEntityTagger = ChatEntityTagger.load(normaliser, quests, BotChatModel.vocabulary(normaliser, examples))
}
