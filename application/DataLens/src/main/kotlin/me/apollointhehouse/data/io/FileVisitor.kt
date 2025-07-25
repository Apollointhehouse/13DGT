package me.apollointhehouse.data.io

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileVisitor

private val logger = KotlinLogging.logger {}

fun visitor(index: MutableMap<String, Path>, noVisit: MutableSet<String>) = fileVisitor {
    onPreVisitDirectory { path, _ ->
        val name = path.fileName?.toString() ?: run {
//            logger.warn { "File with no name found at $path, skipping." }
            return@onPreVisitDirectory FileVisitResult.CONTINUE
        }

//        if (name in index) {
            // If the directory is already indexed, skip it
//            logger.debug { "Directory $name is already indexed, skipping." }
//            return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
//        }

        // Skip directories that cannot be visited
        if (name in noVisit) {
            logger.debug { "Skipping unvisitable directory: $path" }
            return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
        }

        // Index hidden directories
        if (Files.isHidden(path)) {
            logger.debug { "Indexing hidden directory: $path" }
            noVisit.add(name)
            return@onPreVisitDirectory FileVisitResult.SKIP_SUBTREE
        }

        FileVisitResult.CONTINUE
    }

    onVisitFile { path, _ ->
        val name = path.fileName?.toString() ?: run {
            logger.debug { "File with no name found at $path, skipping." }
            return@onVisitFile FileVisitResult.CONTINUE
        }

//        if (name in index) {
            // If the file is already indexed, skip it
//            logger.debug { "File $name is already indexed, skipping." }
//            return@onVisitFile FileVisitResult.CONTINUE
//        }

        // Exclude Recycle Bin system files
        if (name.startsWith("\$I") || name.startsWith("\$R")) return@onVisitFile FileVisitResult.SKIP_SIBLINGS
        if (name.lowercase().startsWith("ntuser")) return@onVisitFile FileVisitResult.CONTINUE

        // Skip hidden files
        if (name in noVisit) {
            logger.debug { "Skipping hidden directory: $path" }
            return@onVisitFile FileVisitResult.CONTINUE
        }

        // Index hidden files
        if (Files.isHidden(path)) {
            logger.debug { "Indexing hidden directory: $path" }
            noVisit.add(name)
            return@onVisitFile FileVisitResult.CONTINUE
        }

        // Add the file to the list of files to search
        logger.debug { "Adding $path to index" }
        index[name] = path

        FileVisitResult.CONTINUE
    }

    onVisitFileFailed { path, _ ->
        // Index files that cannot be visited
        noVisit.add(path.fileName?.toString().orEmpty())

        // Skip files that cannot be visited
        logger.debug { "File $path could not be visited" }

        FileVisitResult.CONTINUE
    }
}