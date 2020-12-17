package io.sellmair.ionos.dyndns.service

import kotlin.test.*

// TODO: Delete after accessing! 🤷‍♂️
expect val temporaryTestDirectory: File

class FilesTest {

    @Test
    fun writeAndRead() {
        val testFile = temporaryTestDirectory.resolve("test.txt")

        testFile.writeText("Hello")
        assertEquals("Hello", testFile.readText())

        testFile.writeText("A\nB")
        assertEquals("A\nB", testFile.readText())
    }

    @Test
    fun exists() {
        val testFile = temporaryTestDirectory.resolve("doesNotExist")
        assertFalse(testFile.exists)

        testFile.writeText("")
        assertTrue(testFile.exists)
    }

    @Test
    fun isFile() {
        val testFile = temporaryTestDirectory.resolve("test.file")
        assertFalse(testFile.isFile)
        assertFalse(testFile.exists)

        testFile.writeText("")
        assertTrue(testFile.isFile)
        assertTrue(testFile.exists)
    }

    @Test
    fun isDirectory() {
        val testDirectory = temporaryTestDirectory.resolve("testDir")
        assertFalse(testDirectory.isDirectory)
        assertFalse(testDirectory.exists)

        assertTrue(testDirectory.createDirectory())
        assertTrue(testDirectory.isDirectory)
        assertTrue(testDirectory.exists)
        assertFalse(testDirectory.isFile)

        testDirectory.createDirectory()
        //assertFalse(testDirectory.createDirectory()) // TODO?
    }

    @Test
    fun delete() {
        val file = temporaryTestDirectory.resolve("parent/myFile.txt")
        assertFails { file.writeText("") }

        file.parent.createDirectory()
        file.writeText("SOME")
        assertEquals("SOME", file.readText())
        assertTrue(file.isFile)

        file.delete()
        assertFalse(file.isFile)
        assertFails { file.readText() }
    }
}