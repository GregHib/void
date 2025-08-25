package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.Character
import world.gregs.voidps.engine.entity.character.npc.NPC
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.item.floor.FloorItem
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.event.handle.EventField
import kotlin.math.log
import kotlin.reflect.KClass

class EventProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val eventSchemas: Map<KClass<out Annotation>, SchemaProvider>
) : SymbolProcessor {

    private data class Node(
        val key: Any?,
        val children: MutableMap<Any?, Node> = mutableMapOf(),
        val handlers: MutableList<NodeHandler> = mutableListOf()
    )

    private data class NodeHandler(
        val method: ClassName, val extension: KSType?, val data: Map<String, Any?>, val dispatcher: ClassName, val schemaProvider: SchemaProvider, val params: List<ClassName>
    )

    private fun emitTrieNode(node: Node): CodeBlock {
        val cb = CodeBlock.builder()
        cb.add("Trie(")

        val children = node.children
        val hasChildren = children.isNotEmpty()
        val hasHandlers = node.handlers.isNotEmpty()

        if (hasChildren || hasHandlers) {
            cb.add("\n⇥") // indent

            if (hasChildren) {
                for ((i, key) in children.keys.withIndex()) {
                    val child = children.getValue(key)
                    if (i > 0) {
                        cb.add(",\n")
                    }
                    cb.add("%S to %L", key, emitTrieNode(child))
                }
                if (hasHandlers) {
                    cb.add(",\n")
                }
            }

            if (hasHandlers) {
                cb.add("handler = setOf(")
                for (i in node.handlers.indices) {
                    val (methodName, extension, data, dispatcher, schema, params) = node.handlers[i]
                    if (i > 0) {
                        cb.add(", ")
                    }
                    cb.add("handler<%T, %T> { ", extension?.toTypeName() ?: schema.extension(), dispatcher)
                    val prefix = schema.prefix(extension.toString(), data)
                    if (prefix != "") {
                        cb.add("$prefix;")
                    }
                    cb.add("%T", methodName)
                    cb.add("(")
                    var index = 0
                    for (param in params) {
                        val field = schema.param(param)
                        if (field != "") {
                            if (index++ > 0) {
                                cb.add(", ")
                            }
                            cb.add(field)
                        }
                    }
                    cb.add(")")
                    cb.add(" }")
                }
                cb.add(")")
            }

            cb.add("\n⇤")
        }

        cb.add(")")
        return cb.build()
    }

    private fun generateHandlerFunction(): FunSpec {
        // Define generic type variables
        val eventClass = Event::class.asClassName()
        val tTypeVar = TypeVariableName("T", eventClass)
        val dispatcherClass = EventDispatcher::class.asClassName()
        val dTypeVar = TypeVariableName("D", dispatcherClass)

        // Define the block parameter type: suspend T.(D) -> Unit
        val blockParamType = LambdaTypeName.get(
            receiver = tTypeVar,
            parameters = arrayOf(dTypeVar),
            returnType = UNIT
        ).copy(suspending = true)

        // Define the return type: suspend Event.(EventDispatcher) -> Unit
        val returnType = LambdaTypeName.get(
            receiver = eventClass,
            parameters = arrayOf(dispatcherClass),
            returnType = UNIT
        ).copy(suspending = true)

        return FunSpec.builder("handler")
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build())
            .addModifiers(KModifier.PRIVATE)
            .addTypeVariable(tTypeVar)
            .addTypeVariable(dTypeVar)
            .addParameter("block", blockParamType)
            .returns(returnType)
            .addStatement("return block as %T", returnType)
            .build()
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        var count = 0
        for ((kClass, schema) in eventSchemas) {
            val symbols = resolver.getSymbolsWithAnnotation(kClass.qualifiedName!!)
                .filterIsInstance<KSFunctionDeclaration>()
            if (symbols.none()) {
                logger.warn("No symbols found; skipping $kClass")
                continue
            }
            count += buildTrieFromAnnotations(symbols, schema, kClass, resolver)
        }

        val rootCode = emitTrieNode(rootNode)
        val funSpec = FunSpec.builder("loadTrie")
            .returns(Trie::class)
            .addModifiers(KModifier.PRIVATE)
            .addStatement("val start = System.currentTimeMillis()")
            .addStatement("val value = %L", rootCode)
            .addStatement("logger.info { \"Loaded $count ${"event handler".plural(count)} in \${System.currentTimeMillis() - start}ms\" }")
            .addStatement("return value")
            .build()
        val fileSpec = FileSpec.builder("world.gregs.voidps.engine.event", "Scripts")
            .addImport("world.gregs.voidps.engine", "get")
            .addType(
                TypeSpec.objectBuilder("Scripts")
                    .addProperty(
                        PropertySpec.builder("logger", InlineLogger::class)
                            .initializer("InlineLogger()")
                            .addModifiers(KModifier.PRIVATE)
                            .build()
                    )
                    .addProperty(
                        PropertySpec.builder("trie", Trie::class)
                            .initializer("loadTrie()")
                            .build()
                    )
                    .addFunction(funSpec)
                    .addFunction(generateHandlerFunction())
                    .build()
            )
            .build()
        try {
            val dependencies = Dependencies(
                aggregating = false,
                sources = resolver.getAllFiles().toList().toTypedArray()
            )
            fileSpec.writeTo(codeGenerator, dependencies)
        } catch (exist: FileAlreadyExistsException) {
            exist.printStackTrace()
        }
        return emptyList()
    }

    private val rootNode = Node("root")

    interface SchemaProvider {
        fun schema(extension: String, params: List<ClassName>, data: Map<String, Any?>): List<EventField>
        fun prefix(extension: String, data: Map<String, Any?>): String = ""
        fun param(param: ClassName): String = "get()" // Works but ideally we can create variables in the method and reuse injected variables
        fun dispatcher(params: List<ClassName>): ClassName {
            return params.firstOrNull() ?: Player::class.asClassName()
        }

        fun extension(): TypeName = throw NotImplementedError("Extension not implemented in ${this::class.simpleName}")

        fun List<ClassName>.key(suffix: String): EventField {
            for (param in this) {
                when (param.simpleName) {
                    Player::class.simpleName -> return EventField.StaticValue("player_$suffix")
                    NPC::class.simpleName -> return EventField.StaticValue("npc_$suffix")
                    Character::class.simpleName -> return EventField.StaticSet(setOf("player_$suffix", "npc_$suffix"))
                    FloorItem::class.simpleName -> return EventField.StaticValue("floor_item_$suffix")
                    GameObject::class.simpleName -> return EventField.StaticValue("object_$suffix")
                    World::class.simpleName -> return EventField.StaticValue("world_$suffix")
                }
            }
            throw IllegalArgumentException("Expected $suffix method to have an entity parameter e.g. \"@On fun method(world: World) {}\"")
        }

        fun List<ClassName>.identifier(): EventField {
            for (param in this) {
                when (param.simpleName) {
                    NPC::class.simpleName -> return EventField.StringList("ids")
                    FloorItem::class.simpleName -> return EventField.StringList("ids")
                    GameObject::class.simpleName -> return EventField.StringList("ids")
                    Player::class.simpleName -> return EventField.StaticValue("player")
                    World::class.simpleName -> return EventField.StaticValue("world")
                }
            }
            return EventField.StaticValue("")
        }
    }

    fun extendsClass(classDeclaration: KSClassDeclaration, targetClassName: String): Boolean {

        return classDeclaration.superTypes.any { superType ->
            val resolved = superType.resolve()
            logger.info("Check: $classDeclaration $superType")
            resolved.declaration.qualifiedName?.asString() == targetClassName
        }
    }

    private fun buildTrieFromAnnotations(symbols: Sequence<KSFunctionDeclaration>, provider: SchemaProvider, kClass: KClass<out Annotation>, resolver: Resolver): Int {
        var count = 0
        for (funDec in symbols) {
            count++
            val useAnnotation = funDec.annotations.find {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == kClass.qualifiedName
            }
            val methodName = funDec.simpleName.asString()
            val receiver = funDec.extensionReceiver?.resolve()
            val extension = receiver?.toString() ?: ""
            val data = mutableMapOf<String, Any>()
            for (argument in useAnnotation?.arguments ?: emptyList()) {
                val key = argument.name!!.asString()
                if (argument.value != null) {
                    data[key] = argument.value!!
                }
            }

            if (funDec.modifiers.contains(Modifier.SUSPEND)) {
                val targetType = resolver.getClassDeclarationByName(SuspendableEvent::class.qualifiedName!!)?.asStarProjectedType()
                if (receiver != null && targetType != null && !targetType.isAssignableFrom(receiver)) {
                    throw IllegalStateException("Suspend method $methodName does not extend a SuspendableEvent.")
                }
            }

            val types = funDec.parameters.map { it.type.resolve().toClassName() }
            val schema = provider.schema(extension, types, data)
            if (schema.isEmpty()) {
                val targetType = resolver.getClassDeclarationByName(Event::class.qualifiedName!!)?.asStarProjectedType()
                if (receiver != null && targetType != null && !targetType.isAssignableFrom(receiver)) {
                    throw IllegalStateException("Expected method $methodName to have an Event as extension e.g.\"@On fun Spawn.playerSpawn() {}\"")
                }
                throw IllegalStateException("No EventProcessor schema found for method $methodName; make sure you're using the correct annotation and event.")
            }
            val className = ClassName(funDec.packageName.asString(), methodName)
            val dispatcher = provider.dispatcher(types)
            fun insert(node: Node, depth: Int) {
                if (depth >= schema.size) {
                    node.handlers.add(NodeHandler(className, receiver, data, dispatcher, provider, types))
                    return
                }
                val keys = schema[depth].get(data)
                for (key in keys) {
                    val child = node.children.getOrPut(key) { Node(key) }
                    insert(child, depth + 1)
                }
            }

            try {
                insert(rootNode, 0)
            } catch (e: Exception) {
                logger.error("Error $className $methodName")
                throw e
            }
        }
        return count
    }
}