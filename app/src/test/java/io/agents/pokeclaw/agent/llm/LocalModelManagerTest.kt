package io.agents.pokeclaw.agent.llm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class LocalModelManagerTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `model directory uses external app storage when it can be created`() {
        val externalRoot = temporaryFolder.newFolder("external")
        val internalRoot = temporaryFolder.newFolder("internal")

        val dir = LocalModelManager.resolveUsableModelDir(externalRoot, internalRoot)

        assertEquals(externalRoot.resolve("models"), dir)
        assertTrue(dir.isDirectory)
    }

    @Test
    fun `model directory falls back to internal storage when external path is unusable`() {
        val externalRoot = temporaryFolder.newFolder("external")
        externalRoot.resolve("models").writeText("blocking file")
        val internalRoot = temporaryFolder.newFolder("internal")

        val dir = LocalModelManager.resolveUsableModelDir(externalRoot, internalRoot)

        assertEquals(internalRoot.resolve("models"), dir)
        assertTrue(dir.isDirectory)
    }

    @Test
    fun `model directory falls back to internal storage when external path is not writable`() {
        val externalRoot = temporaryFolder.newFolder("external")
        val internalRoot = temporaryFolder.newFolder("internal")
        val externalModelDir = externalRoot.resolve("models")

        val dir = LocalModelManager.resolveUsableModelDir(
            externalRoot = externalRoot,
            internalRoot = internalRoot,
            canWriteDirectory = { candidate -> candidate != externalModelDir },
        )

        assertEquals(internalRoot.resolve("models"), dir)
        assertTrue(dir.isDirectory)
    }

    @Test
    fun `model directory falls back to internal storage when external root is missing`() {
        val internalRoot = temporaryFolder.newFolder("internal")

        val dir = LocalModelManager.resolveUsableModelDir(null, internalRoot)

        assertEquals(internalRoot.resolve("models"), dir)
        assertTrue(dir.isDirectory)
    }
    @Test
    fun `custom model does not match a stale path just because its synthetic id matches`() {
        val customModel = LocalModelManager.ModelInfo(
            id = "custom-local",
            displayName = "Custom: new-model.litertlm",
            url = "https://models.example/new-model.litertlm",
            fileName = "new-model.litertlm",
            sizeBytes = 0L,
            minRamGb = 0,
            isCustom = true,
        )
        val staleConfig = LocalModelConfig(
            modelPath = "/models/old-model.litertlm",
            modelId = "custom-local",
            displayName = "old-model",
            backendPreference = "CPU",
        )

        assertFalse(LocalModelManager.matchesConfiguredModel(customModel, staleConfig))
    }

    @Test
    fun `custom model matches when configured path points to its file`() {
        val customModel = LocalModelManager.ModelInfo(
            id = "custom-local",
            displayName = "Custom: new-model.litertlm",
            url = "https://models.example/new-model.litertlm",
            fileName = "new-model.litertlm",
            sizeBytes = 0L,
            minRamGb = 0,
            isCustom = true,
        )
        val matchingConfig = LocalModelConfig(
            modelPath = "/models/new-model.litertlm",
            modelId = "custom-local",
            displayName = "new-model",
            backendPreference = "CPU",
        )

        assertTrue(LocalModelManager.matchesConfiguredModel(customModel, matchingConfig))
    }

    @Test
    fun `built in model continues to match by persisted model id`() {
        val builtInModel = LocalModelManager.AVAILABLE_MODELS.first { it.id == "gemma4-e2b" }
        val config = LocalModelConfig(
            modelPath = "/models/legacy-model-path.bin",
            modelId = builtInModel.id,
            displayName = "legacy model",
            backendPreference = "CPU",
        )

        assertTrue(LocalModelManager.matchesConfiguredModel(builtInModel, config))
    }

}
