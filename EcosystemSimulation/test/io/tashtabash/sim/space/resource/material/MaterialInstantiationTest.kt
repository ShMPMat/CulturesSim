package io.tashtabash.sim.space.resource.material

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.net.URLClassLoader
import java.nio.file.Path


internal class MaterialInstantiationTest {
    @TempDir
    lateinit var tempDir: Path

    private val outputStream = ByteArrayOutputStream()

    @BeforeEach
    fun setUp() {
        System.setOut(PrintStream(outputStream))
    }

    @AfterEach
    fun tearDown() {
        System.setOut(System.out)
    }

    @Test
    fun `createPool prints error when action tag does not exist`() {
        val materialsDir = File(tempDir.toFile(), "TestMaterials")
        materialsDir.mkdir()
        val materialFile = File(materialsDir, "TestMaterials.txt")
        materialFile.writeText("-Wood 2.5 +Burn:Ash")

        val instantiation = MaterialInstantiation(
            emptyList(),
            emptyList(),
            listOf(materialFile.toURI().toURL())
        )
        instantiation.createPool()

        val output = outputStream.toString().trim()

        assertTrue(output.contains("No action Burn:Ash found for material Wood"),
            "No error message found in the console output. Actual: $output")
    }
}
