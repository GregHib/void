package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParser
import world.gregs.voidps.engine.data.YamlParserI

class OptimisedLineParser(parser: YamlParserI, reader: CharArrayReader) : DefaultLineParser(parser, reader) {

    override fun createMap(): MutableMap<String, Any> = Object2ObjectOpenHashMap(YamlParser.EXPECTED_MAP_SIZE)

    override fun createList(): MutableList<Any> = ObjectArrayList(YamlParser.EXPECTED_LIST_SIZE)

}