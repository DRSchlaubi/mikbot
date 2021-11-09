package dev.schlaubi.mikbot.plugin.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.schlaubi.mikbot.plugin.api.PluginMain
import org.pf4j.Extension
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.*

class PluginMainProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val outputDir = Path(environment.options["output-dir"] ?: "")

        val (symbols, symbol) = processPluginMain(outputDir, resolver)
        processExtension(outputDir, resolver)

        return symbols.toList() - symbol
    }

    private fun processExtension(outputDir: Path, resolver: Resolver) {
        val symbols = resolver.getSymbolsWithAnnotation(
            Extension::class.qualifiedName ?: error("Could not determine name of @Extension")
        )

        val names = symbols.filterIsInstance<KSClassDeclaration>()
            .map { it.qualifiedName }
            .filterNotNull()
            .map { it.asString() }

        val file = outputDir / "extensions.txt"
        file.writeText(names.joinToString("\n"))
    }

    private fun processPluginMain(outputDir: Path, resolver: Resolver): Pair<List<KSAnnotated>, KSAnnotated> {
        val symbolsSequence = resolver.getSymbolsWithAnnotation(
            PluginMain::class.qualifiedName ?: error("Could not determine name of @PluginMain")
        )
        val symbols = symbolsSequence.take(2).toList()
        require(symbols.isNotEmpty()) { "No @PluginMain found in this module" }
        require(symbols.size == 1) {
            "Multiple plugin main files found in the same module: ${
                symbolsSequence.joinToString(",") { (it.location as FileLocation).filePath }
            }"

        }

        val symbol = symbols.first()
        require(symbol is KSClassDeclaration) { "Found @PluginMain on non class: $symbol" }

        val name = symbol.qualifiedName?.asString() ?: error("Symbol does not have a name: $symbol")
        val path = outputDir / "plugin-main-class.txt"
        if (!path.parent.exists()) {
            path.parent.createDirectories()
        }
        path.writeText(name, options = arrayOf(StandardOpenOption.CREATE))
        return Pair(symbols, symbol)
    }
}
