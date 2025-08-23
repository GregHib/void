package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import world.gregs.voidps.engine.client.ui.chat.plural
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.event.handle.EventField
import world.gregs.voidps.engine.get
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
        val method: ClassName, val extension: KSType?, val data: Map<String, Any?>, val dispatcher: ClassName, val schemaProvider: SchemaProvider, val params: List<ClassName>)

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
            count += buildTrieFromAnnotations(symbols, schema, kClass)
        }
        if (count == 0) {
            return emptyList()
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
        val dependencies = Dependencies(
            aggregating = false,
            sources = resolver.getAllFiles().toList().toTypedArray()
        )
        fileSpec.writeTo(codeGenerator, dependencies)
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
    }

    private fun buildTrieFromAnnotations(symbols: Sequence<KSFunctionDeclaration>, provider: SchemaProvider, kClass: KClass<out Annotation>): Int {
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
            val types = funDec.parameters.map { it.type.resolve().toClassName() }
            val schema = provider.schema(extension, types, data)
            if (schema.isEmpty()) {
                throw IllegalStateException("Expected method $methodName to have an Event as extension e.g.\"@Use fun Spawn.playerSpawn() {}\"")
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

            insert(rootNode, 0)
        }
        return count
    }
}