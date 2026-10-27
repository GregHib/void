package world.gregs.voidps.engine.data.definition

import it.unimi.dsi.fastutil.Hash
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import world.gregs.config.Config
import world.gregs.voidps.cache.definition.data.FontDefinition
import world.gregs.voidps.engine.data.snapshot.Snapshot
import world.gregs.voidps.engine.timedLoad

class FontDefinitions(
    override var definitions: Array<FontDefinition>,
) : DefinitionsDecoder<FontDefinition> {

    override lateinit var ids: Map<String, Int>

    override fun empty() = FontDefinition.EMPTY

    fun load(path: String): FontDefinitions {
        timedLoad("font config") {
            val ids = Object2IntOpenHashMap<String>(20, Hash.VERY_FAST_LOAD_FACTOR)
            Config.fileReader(path) {
                while (nextPair()) {
                    val key = key()
                    val id = int()
                    ids[key] = id
                    definitions[id].stringId = key
                }
            }
            this.ids = ids
            ids.size
        }
        return this
    }

    fun save(snapshot: Snapshot) = snapshot.write {
        int(definitions.size)
        for (definition in definitions) {
            int(definition.id)
            bytes(definition.glyphWidths)
            nullableByteArrays(definition.kerningAdjustments)
            int(definition.verticalSpacing)
            int(definition.topPadding)
            int(definition.bottomPadding)
            string(definition.stringId)
            params(definition.params)
        }
        ids(ids)
    }

    companion object {
        fun restore(snapshot: Snapshot): FontDefinitions? {
            var fonts: FontDefinitions? = null
            snapshot.restore { reader ->
                val definitions = Array(reader.int()) {
                    FontDefinition(
                        id = reader.int(),
                        glyphWidths = reader.bytes(),
                        kerningAdjustments = reader.nullableByteArrays(),
                        verticalSpacing = reader.int(),
                        topPadding = reader.int(),
                        bottomPadding = reader.int(),
                        stringId = reader.string(),
                        params = reader.params(),
                    )
                }
                val ids = reader.ids()
                val created = FontDefinitions(definitions)
                created.ids = ids
                fonts = created
            }
            return fonts
        }
    }
}
