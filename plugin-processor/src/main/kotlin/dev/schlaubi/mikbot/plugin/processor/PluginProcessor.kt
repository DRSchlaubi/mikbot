package dev.schlaubi.mikbot.plugin.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.schlaubi.mikbot.plugin.api.PluginMain
import org.pf4j.Extension

class PluginProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        // this prevents the processor from running twice
        if (resolver.getNewFiles().none()) return emptyList()

        processPluginMain(resolver)
        processExtension(resolver)

        // this processor can't fail processing a symbol,
        // it just can fail on wrong symbols
        return emptyList()
    }

    private fun processExtension(resolver: Resolver) {
        val symbols = resolver.getSymbolsWithAnnotation(
            Extension::class.qualifiedName ?: error("Could not determine name of @Extension")
        )

        val names = symbols.filterIsInstance<KSClassDeclaration>()
            .map { it.qualifiedName }
            .filterNotNull()
            .map { it.asString() }

        val file = environment.codeGenerator.createNewFile(Dependencies.ALL_FILES, "META-INF", "extensions", "idx")
        file.bufferedWriter().use { it.write(names.joinToString("\n")) }
    }

    private fun processPluginMain(resolver: Resolver): Pair<List<KSAnnotated>, KSAnnotated> {
        val symbolsSequence = resolver.getSymbolsWithAnnotation(
            PluginMain::class.qualifiedName ?: error("Could not determine name of @PluginMain")
        )
        val symbols = symbolsSequence.take(2).toList()
        require(symbols.isNotEmpty()) { "No @PluginMain found in this module: $symbols" }
        require(symbols.size == 1) {
            "Multiple plugin main files found in the same module: ${
                symbolsSequence.joinToString(",") { (it.location as FileLocation).filePath }
            }"
        }

        val symbol = symbols.first()
        require(symbol is KSClassDeclaration) { "Found @PluginMain on non class: $symbol" }

        val name = symbol.qualifiedName?.asString() ?: error("Symbol does not have a name: $symbol")
        val path = environment.codeGenerator.createNewFile(Dependencies(false), "META-INF", "MANIFEST", "MF")
        path.bufferedWriter().use { it.write("Plugin-Class: $name"); it.write(System.lineSeparator()) }
        return Pair(symbols, symbol)
    }
}
