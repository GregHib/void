package world.gregs.voidps.engine.event

import com.github.michaelbull.logging.InlineLogger
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ksp.writeTo
import world.gregs.voidps.engine.client.ui.chat.plural

class EventProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val handlers: Map<String, UseData.(Int) -> Unit>
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Use::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
        if (symbols.none()) {
            logger.warn("No symbols found; skipping")
            return emptyList()
        }
        val builder = FunSpec.builder("load")
            .addAnnotation(AnnotationSpec.builder(Suppress::class).addMember("\"UNCHECKED_CAST\"").build())
        builder.addStatement("val start = System.currentTimeMillis()")
        var count = 0
        for (symbol in symbols) {
            generateCodec(builder, symbol, count++)
        }
        builder.addStatement("logger.info { \"Loaded $count ${"event handler".plural(count)} in \${System.currentTimeMillis() - start}ms\" }")
        val fileSpec = FileSpec.builder("world.gregs.voidps.engine.event", "Scripts")
            .addType(
                TypeSpec.objectBuilder("Scripts")
                    .addProperty(
                        PropertySpec.builder("logger", InlineLogger::class)
                            .initializer("InlineLogger()")
                            .addModifiers(KModifier.PRIVATE)
                            .build())
                    .addFunction(
                        builder
                            .build()
                    )
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

    private fun generateCodec(builder: FunSpec.Builder, funDec: KSFunctionDeclaration, index: Int) {
        val useAnnotation = funDec.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == Use::class.qualifiedName
        }
        val methodName = funDec.simpleName.asString()
        val pkg = funDec.packageName.asString()
        val name = ClassName(pkg, methodName)
        val data = UseData(builder, name)
        for (argument in useAnnotation?.arguments ?: emptyList()) {
            when (argument.name?.asString()) {
                "id" -> {
                    val id = argument.value as String
                    if (id != "") {
                        data.ids.add(id)
                    }
                }
                "ids" -> data.ids.addAll(argument.value as List<String>)
                "on" -> data.on = argument.value as List<String>
                "option" -> data.option = argument.value as String
                "component" -> data.component = argument.value as String
                "approach" -> data.approach = argument.value as Boolean
                "arrive" -> data.arrive = argument.value as Boolean
                else -> throw IllegalArgumentException("Unexpected argument ${argument.name?.asString()}")
            }
        }
        val extension = funDec.extensionReceiver?.resolve()?.toString() ?: throw IllegalStateException("Expected event extension on method $methodName e.g.\n@Use\nfun Spawn.playerSpawn() {}")
        val method = handlers[extension] ?: throw UnsupportedOperationException("Unknown extension type $extension on method $methodName. Please ensure the event type is added to EventProcessorProvider.handlers.")
        method.invoke(data, index)
    }
}