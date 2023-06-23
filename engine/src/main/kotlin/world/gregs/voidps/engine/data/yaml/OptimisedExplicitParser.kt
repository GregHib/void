package world.gregs.voidps.engine.data.yaml

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import world.gregs.voidps.engine.data.CharArrayReader
import world.gregs.voidps.engine.data.YamlParser
import world.gregs.voidps.engine.data.YamlParserI

class OptimisedExplicitParser(parser: YamlParserI, reader: CharArrayReader) : DefaultExplicitParser(parser, reader) {

    override fun createList(): MutableList<Any> = ObjectArrayList(YamlParser.EXPECTED_EXPLICIT_LIST_SIZE)

    override fun createMap(): MutableMap<String, Any> = Object2ObjectOpenHashMap(YamlParser.EXPECTED_MAP_SIZE)

}