package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.DefaultPresets
import com.example.physics.BoundaryMode
import com.example.physics.ParticleSimulation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Astilla-tion: Kinetic Sandbox", appName)
    }

    @Test
    fun `built in presets have valid matrices and colors`() {
        val presets = DefaultPresets.BUILT_IN_PRESETS
        assertTrue(presets.isNotEmpty())
        presets.forEach { preset ->
            assertEquals(preset.speciesCount, preset.matrix.size)
            assertEquals(preset.speciesCount, preset.speciesColors.size)
            assertNotNull(preset.name)
        }
    }

    @Test
    fun `particle simulation ticks and preserves particle bounds`() {
        val sim = ParticleSimulation(800f, 600f, maxParticles = 200)
        sim.setParticleCount(100)
        sim.boundaryMode = BoundaryMode.WRAP

        // Tick simulation
        sim.tick(1.0f)
        sim.tick(1.0f)

        assertEquals(100, sim.count)
        for (i in 0 until sim.count) {
            assertTrue("x[i] should be in width bounds", sim.x[i] >= 0f && sim.x[i] <= 800f)
            assertTrue("y[i] should be in height bounds", sim.y[i] >= 0f && sim.y[i] <= 600f)
        }
    }

    @Test
    fun `supports up to 12 species in simulation`() {
        val sim = ParticleSimulation(800f, 600f, maxParticles = 300)
        sim.setSpeciesCount(12)
        assertEquals(12, sim.numSpecies)
        sim.randomizeMatrix(ParticleSimulation.RandomizeMode.SYMBIOTIC)

        sim.setMatrixValue(11, 11, 0.9f)
        assertEquals(0.9f, sim.matrix[11][11], 0.001f)

        sim.tick(1.0f)
        assertEquals(12, sim.numSpecies)
    }
}
