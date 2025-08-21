package world.gregs.voidps.engine.event

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueArgument
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.math.log

class EventProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Use::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()
        if (symbols.none()) {
            logger.warn("No symbols found; skipping")
            return emptyList()
        }
        val builder = FunSpec.builder("load")
        builder.addStatement("println(\"Hello world!\")")
        for (symbol in symbols) {
            logger.info("Symbol: $symbol")
            generateCodec(builder, symbol)
        }
        val fileSpec = FileSpec.builder("world.gregs.voidps.engine.event", "Scripts")
            .addType(
                TypeSpec.objectBuilder("Scripts")
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

    private fun generateCodec(builder: FunSpec.Builder, funDec: KSFunctionDeclaration) {
        val useAnnotation = funDec.annotations.find {
            it.annotationType.resolve().declaration.qualifiedName?.asString() == Use::class.qualifiedName
        }

        logger.info(useAnnotation.toString())

        var id = ""
        var ids = emptyList<String>()
        var on = emptyList<String>()
        var option: String = ""
        var component: String = ""
        for (argument in useAnnotation?.arguments ?: emptyList()) {
            when (argument.name?.asString()) {
                "id" -> id = argument.value as String
                "ids" -> ids = argument.value as List<String>
                "on" -> on = argument.value as List<String>
                "option" -> option = argument.value as String
                "component" -> component = argument.value as String
                else -> throw IllegalArgumentException("Unexpected argument ${argument.name?.asString()}")
            }
        }
        val className = funDec.simpleName.asString()
        val pkg = funDec.packageName.asString()
        logger.info("$className $pkg")
        when (funDec.extensionReceiver?.resolve().toString()) {
            "Test" -> builder.addStatement("//test(\"${id}\", ${ids})")
        }

    }
}