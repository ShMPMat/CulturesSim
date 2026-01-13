package io.tashtabash.sim.space.resource.instantiation

import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths


fun getResourcePaths(folderUrls: List<URL>): List<URL> = folderUrls.flatMap { folderUrl ->
    when (folderUrl.protocol) {
        "jar" -> {
            try {
                val jarConnection = folderUrl.openConnection() as JarURLConnection
                val jarFile = jarConnection.jarFile
                val entryName = jarConnection.entryName
                val jarFileUrl = jarConnection.jarFileURL

                // Get all relevant resources by checking all of them
                jarFile.entries().asSequence()
                    .filter { it.name.startsWith(entryName) && !it.isDirectory && it.name != entryName }
                    .mapNotNull { jarEntry ->
                        URI.create("jar:${jarFileUrl}!/${jarEntry.name}")
                            .toURL()
                    }
                    .toList()
            } catch (_: Exception) {
                emptyList()
            }
        }
        "file" -> {
            try {
                val path = Paths.get(folderUrl.toURI())
                if (Files.exists(path) && Files.isDirectory(path)) {
                    Files.walk(path)
                        .toList()
                        .filter { it != null && Files.isRegularFile(it) }
                        .mapNotNull { filePath ->
                            filePath.toUri().toURL()
                        }
                } else {
                    emptyList()
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
        else -> listOf(folderUrl) // For other protocols use the URL as-is
    }
}
