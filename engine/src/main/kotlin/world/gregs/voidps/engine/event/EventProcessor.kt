package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import world.gregs.voidps.engine.client.ui.chat.plural

class EventProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val eventSchemas: Map<String, List<EventField>>
) : SymbolProcessor {

    private data class Node(
        val key: Any?,
        val children: MutableMap<Any, Node> = mutableMapOf(),
        val handlers: MutableList<Pair<KSFunctionDeclaration, Boolean>> = mutableListOf() // or store references
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
//                cb.add("children = mapOf(\n⇥")
                for ((i, key) in children.keys.withIndex()) {
                    val child = children.getValue(key)
                    if (i > 0) {
                        cb.add(",\n")
                    }
                    cb.add("%S to %L", key, emitTrieNode(child))
                }
//                cb.add("\n⇤)")
                if (hasHandlers) {
                    cb.add(",\n")
                }
            }

            if (hasHandlers) {
                cb.add("handler = setOf(")
                for (i in node.handlers.indices) {
                    val (dec, arrive) = node.handlers[i]
                    if (i > 0) {
                        cb.add(", ")
                    }
                    val extension = dec.extensionReceiver?.resolve()?.toTypeName()
                    val type = ClassName("world.gregs.voidps.engine.entity.character.player", "Player")
                    val methodName = ClassName(dec.packageName.asString(), dec.simpleName.asString())
                    if (arrive) {
                        cb.add("handler<%T, %T> { arriveDelay(); %T() }", extension, type, methodName)
                    } else {
                        cb.add("handler<%T, %T> { %T() }", extension, type, methodName)
                    }
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
        val tTypeVar = TypeVariableName("T", ClassName("world.gregs.voidps.engine.event", "Event"))
        val dTypeVar = TypeVariableName("D", ClassName("world.gregs.voidps.engine.event", "EventDispatcher"))

        // Define the block parameter type: suspend T.(D) -> Unit
        val blockParamType = LambdaTypeName.get(
            receiver = tTypeVar,
            parameters = arrayOf(dTypeVar),
            returnType = UNIT
        ).copy(suspending = true)

        // Define the return type: suspend Event.(EventDispatcher) -> Unit
        val returnType = LambdaTypeName.get(
            receiver = ClassName("", "Event"),
            parameters = arrayOf(ClassName("", "EventDispatcher")),
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
        val symbols = resolver.getSymbolsWithAnnotation(Use::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
        if (symbols.none()) {
            logger.warn("No symbols found; skipping")
            return emptyList()
        }

        val (rootNode, count) = buildTrieFromAnnotations(symbols)
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

    private fun buildTrieFromAnnotations(symbols: Sequence<KSFunctionDeclaration>): Pair<Node, Int> {
        val root = Node("root")
        var count = 0
        for (funDec in symbols) {
            count++
            val useAnnotation = funDec.annotations.find {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == Use::class.qualifiedName
            }
            val methodName = funDec.simpleName.asString()
            val extension = funDec.extensionReceiver?.resolve()?.toString() ?: throw IllegalStateException("Expected event extension on method $methodName e.g.\n@Use\nfun Spawn.playerSpawn() {}")
            val data = UseData()
            for (argument in useAnnotation?.arguments ?: emptyList()) {
                when (argument.name?.asString()) {
                    "id" -> {
                        val id = argument.value as String
                        if (id != "") {
                            data.sources.add(id)
                        }
                    }
                    "ids" -> data.sources.addAll(argument.value as List<String>)
                    "on" -> data.targets.addAll(argument.value as List<String>)
                    "npcs" -> data.targets.addAll(argument.value as List<String>)
                    "option" -> data.option = argument.value as String
                    "component" -> data.component = argument.value as String
                    "approach" -> data.approach = argument.value as Boolean
                    "arrive" -> data.arrive = argument.value as Boolean
                    else -> throw IllegalArgumentException("Unexpected argument ${argument.name?.asString()}")
                }

            }
            val schema = eventSchemas[extension]
                ?: throw IllegalStateException("No schema defined for extension type: $extension")

            fun insert(node: Node, depth: Int) {
                if (depth >= schema.size) {
                    node.handlers.add(funDec to data.arrive)
                    return
                }
                val keys = schema[depth].get(data)
                for (key in keys) {
                    val child = node.children.getOrPut(key) { Node(key) }
                    insert(child, depth + 1)
                }
            }

            insert(root, 0)
        }
        return root to count
    }
}