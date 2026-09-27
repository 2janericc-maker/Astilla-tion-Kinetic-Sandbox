package com.example.physics

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class ParticleSimulation(
    initialWidth: Float = 1000f,
    initialHeight: Float = 1000f,
    var maxParticles: Int = 2000
) {
    companion object {
        const val MAX_SPECIES = 12
    }

    var width: Float = initialWidth
        private set
    var height: Float = initialHeight
        private set

    var count: Int = 800
        private set

    var numSpecies: Int = 4
        private set

    var friction: Float = 0.90f
    var forceStrength: Float = 1.0f
    var forceDistance: Float = 110f
    var beta: Float = 0.30f
    var boundaryMode: BoundaryMode = BoundaryMode.WRAP

    // Flat arrays for high performance contiguous memory cache
    var x = FloatArray(maxParticles)
    var y = FloatArray(maxParticles)
    var vx = FloatArray(maxParticles)
    var vy = FloatArray(maxParticles)
    var species = IntArray(maxParticles)

    // Interaction matrix: size [MAX_SPECIES][MAX_SPECIES]
    var matrix: Array<FloatArray> = Array(MAX_SPECIES) { FloatArray(MAX_SPECIES) }

    // Touch interaction
    var isTouched: Boolean = false
    var touchX: Float = 0f
    var touchY: Float = 0f
    var touchMode: TouchMode = TouchMode.ATTRACT

    // Spatial partitioning grid structures
    private var gridCols = 1
    private var gridRows = 1
    private var head = IntArray(1) { -1 }
    private var next = IntArray(maxParticles) { -1 }

    init {
        // Initialize default random matrix
        randomizeMatrix(RandomizeMode.SYMBIOTIC)
        spawnRandomParticles(count, numSpecies)
        updateGridDimensions()
    }

    fun updateDimensions(w: Float, h: Float) {
        if (w <= 0f || h <= 0f) return
        val oldW = width
        val oldH = height
        width = w
        height = h
        updateGridDimensions()

        // Rescale positions gracefully if bounds changed significantly
        if (oldW > 10f && oldH > 10f && (abs(oldW - w) > 50f || abs(oldH - h) > 50f)) {
            val scaleX = w / oldW
            val scaleY = h / oldH
            for (i in 0 until count) {
                x[i] = (x[i] * scaleX).coerceIn(0f, w)
                y[i] = (y[i] * scaleY).coerceIn(0f, h)
            }
        }
    }

    private fun updateGridDimensions() {
        val cellSize = max(forceDistance, 40f)
        gridCols = max(1, (width / cellSize).toInt())
        gridRows = max(1, (height / cellSize).toInt())
        val totalCells = gridCols * gridRows
        if (head.size < totalCells) {
            head = IntArray(totalCells) { -1 }
        }
    }

    fun setSpeciesCount(newSpeciesCount: Int) {
        val clamped = newSpeciesCount.coerceIn(2, MAX_SPECIES)
        if (numSpecies != clamped) {
            numSpecies = clamped
            // Re-assign species for active particles
            for (i in 0 until count) {
                if (species[i] >= clamped) {
                    species[i] = Random.nextInt(clamped)
                }
            }
        }
    }

    fun setParticleCount(newCount: Int) {
        val target = newCount.coerceIn(50, maxParticles)
        if (target > count) {
            // Spawn additional particles
            for (i in count until target) {
                x[i] = Random.nextFloat() * width
                y[i] = Random.nextFloat() * height
                vx[i] = (Random.nextFloat() - 0.5f) * 2f
                vy[i] = (Random.nextFloat() - 0.5f) * 2f
                species[i] = Random.nextInt(numSpecies)
            }
        }
        count = target
    }

    fun spawnRandomParticles(particleCount: Int, speciesCount: Int) {
        numSpecies = speciesCount.coerceIn(2, MAX_SPECIES)
        count = particleCount.coerceIn(50, maxParticles)
        for (i in 0 until count) {
            x[i] = Random.nextFloat() * max(width, 100f)
            y[i] = Random.nextFloat() * max(height, 100f)
            vx[i] = (Random.nextFloat() - 0.5f) * 4f
            vy[i] = (Random.nextFloat() - 0.5f) * 4f
            species[i] = Random.nextInt(numSpecies)
        }
    }

    fun spawnClusteredParticles() {
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        val radius = min(width, height) * 0.35f
        for (i in 0 until count) {
            val angle = Random.nextFloat() * (Math.PI * 2).toFloat()
            val r = sqrt(Random.nextFloat()) * radius
            x[i] = centerX + kotlin.math.cos(angle) * r
            y[i] = centerY + kotlin.math.sin(angle) * r
            vx[i] = -kotlin.math.sin(angle) * 3f
            vy[i] = kotlin.math.cos(angle) * 3f
            species[i] = Random.nextInt(numSpecies)
        }
    }

    fun setMatrixValue(row: Int, col: Int, value: Float) {
        if (row in 0 until MAX_SPECIES && col in 0 until MAX_SPECIES) {
            matrix[row][col] = value.coerceIn(-1.0f, 1.0f)
        }
    }

    fun randomizeMatrix(mode: RandomizeMode = RandomizeMode.CHAOTIC) {
        when (mode) {
            RandomizeMode.CHAOTIC -> {
                for (i in 0 until MAX_SPECIES) {
                    for (j in 0 until MAX_SPECIES) {
                        matrix[i][j] = (Random.nextFloat() * 2f - 1f)
                    }
                }
            }
            RandomizeMode.SYMMETRIC -> {
                for (i in 0 until MAX_SPECIES) {
                    for (j in i until MAX_SPECIES) {
                        val force = (Random.nextFloat() * 2f - 1f)
                        matrix[i][j] = force
                        matrix[j][i] = force
                    }
                }
            }
            RandomizeMode.ASYMMETRIC_CHASE -> {
                for (i in 0 until MAX_SPECIES) {
                    for (j in 0 until MAX_SPECIES) {
                        val next = (i + 1) % numSpecies
                        val prev = (i - 1 + numSpecies) % numSpecies
                        matrix[i][j] = when (j) {
                            next -> 0.8f + Random.nextFloat() * 0.2f
                            prev -> -0.8f - Random.nextFloat() * 0.2f
                            i -> 0.3f
                            else -> (Random.nextFloat() * 0.6f - 0.3f)
                        }
                    }
                }
            }
            RandomizeMode.SYMBIOTIC -> {
                for (i in 0 until MAX_SPECIES) {
                    for (j in 0 until MAX_SPECIES) {
                        matrix[i][j] = if (i == j) {
                            0.4f + Random.nextFloat() * 0.3f
                        } else {
                            (Random.nextFloat() * 1.6f - 0.8f)
                        }
                    }
                }
            }
            RandomizeMode.CRYSTALLINE -> {
                for (i in 0 until MAX_SPECIES) {
                    for (j in 0 until MAX_SPECIES) {
                        matrix[i][j] = if (i == j) {
                            0.75f
                        } else {
                            -0.45f - Random.nextFloat() * 0.3f
                        }
                    }
                }
            }
        }
    }

    enum class RandomizeMode(val label: String) {
        CHAOTIC("Chaos Matrix"),
        SYMBIOTIC("Emergent Symbiosis"),
        ASYMMETRIC_CHASE("Predator Chase"),
        SYMMETRIC("Symmetric Equilibrium"),
        CRYSTALLINE("Crystalline Lattices")
    }

    /**
     * Compute force scalar based on normalized distance rNorm in [0, 1].
     * Force is repulsive for rNorm < beta to avoid infinite collapse.
     * For beta <= rNorm < 1.0, force scales according to attraction/repulsion coefficient A.
     */
    private inline fun computeForce(rNorm: Float, b: Float, a: Float): Float {
        return if (rNorm < b) {
            rNorm / b - 1.0f
        } else if (rNorm < 1.0f) {
            val midpoint = (1.0f + b) * 0.5f
            val halfSpan = (1.0f - b) * 0.5f
            a * (1.0f - abs(rNorm - midpoint) / halfSpan)
        } else {
            0.0f
        }
    }

    /**
     * Physics tick execution.
     * Takes delta time multiplier dt.
     */
    fun tick(dtMultiplier: Float = 1.0f) {
        if (width <= 0f || height <= 0f || count <= 0) return

        val dt = (0.016f * dtMultiplier).coerceIn(0.001f, 0.08f)
        val rMax = max(forceDistance, 10f)
        val rMaxSq = rMax * rMax
        val b = beta.coerceIn(0.1f, 0.6f)
        val strength = forceStrength * 40.0f
        val w = width
        val h = height
        val isWrap = (boundaryMode == BoundaryMode.WRAP)
        val halfW = w * 0.5f
        val halfH = h * 0.5f

        updateGridDimensions()
        val cols = gridCols
        val rows = gridRows
        val totalCells = cols * rows

        // Clear spatial hash
        for (c in 0 until totalCells) {
            head[c] = -1
        }

        val cellW = w / cols
        val cellH = h / rows

        // Bin particles into grid: O(N)
        for (i in 0 until count) {
            val cx = (x[i] / cellW).toInt().coerceIn(0, cols - 1)
            val cy = (y[i] / cellH).toInt().coerceIn(0, rows - 1)
            val cellIdx = cy * cols + cx
            next[i] = head[cellIdx]
            head[cellIdx] = i
        }

        // Physics force calculation using spatial grid
        for (i in 0 until count) {
            val px = x[i]
            val py = y[i]
            val s1 = species[i]
            val matrixRow = matrix[s1]

            val cx = (px / cellW).toInt().coerceIn(0, cols - 1)
            val cy = (py / cellH).toInt().coerceIn(0, rows - 1)

            var fx = 0.0f
            var fy = 0.0f

            // Check the 3x3 neighboring cells
            for (dy in -1..1) {
                var ny = cy + dy
                if (isWrap) {
                    ny = (ny % rows + rows) % rows
                } else if (ny < 0 || ny >= rows) {
                    continue
                }

                val rowOffset = ny * cols

                for (dx in -1..1) {
                    var nx = cx + dx
                    if (isWrap) {
                        nx = (nx % cols + cols) % cols
                    } else if (nx < 0 || nx >= cols) {
                        continue
                    }

                    var j = head[rowOffset + nx]
                    while (j != -1) {
                        if (i != j) {
                            var deltaX = x[j] - px
                            var deltaY = y[j] - py

                            if (isWrap) {
                                if (deltaX > halfW) deltaX -= w
                                else if (deltaX < -halfW) deltaX += w

                                if (deltaY > halfH) deltaY -= h
                                else if (deltaY < -halfH) deltaY += h
                            }

                            val distSq = deltaX * deltaX + deltaY * deltaY
                            if (distSq < rMaxSq && distSq > 0.0001f) {
                                val dist = sqrt(distSq)
                                val rNorm = dist / rMax
                                val s2 = species[j]
                                val a = matrixRow[s2]
                                val force = computeForce(rNorm, b, a) * strength
                                val invDist = 1.0f / dist
                                fx += deltaX * invDist * force
                                fy += deltaY * invDist * force
                            }
                        }
                        j = next[j]
                    }
                }
            }

            // Touch interaction force
            if (isTouched && touchMode != TouchMode.NONE) {
                var tdx = touchX - px
                var tdy = touchY - py
                if (isWrap) {
                    if (tdx > halfW) tdx -= w
                    else if (tdx < -halfW) tdx += w

                    if (tdy > halfH) tdy -= h
                    else if (tdy < -halfH) tdy += h
                }

                val tDistSq = tdx * tdx + tdy * tdy
                val touchRadius = 260f
                if (tDistSq < touchRadius * touchRadius && tDistSq > 1.0f) {
                    val tDist = sqrt(tDistSq)
                    val tFactor = (1.0f - tDist / touchRadius) * 250f
                    val invTDist = 1.0f / tDist
                    when (touchMode) {
                        TouchMode.ATTRACT -> {
                            fx += tdx * invTDist * tFactor
                            fy += tdy * invTDist * tFactor
                        }
                        TouchMode.REPEL -> {
                            fx -= tdx * invTDist * tFactor * 1.5f
                            fy -= tdy * invTDist * tFactor * 1.5f
                        }
                        TouchMode.SPAWN -> {
                            // Impart swirling motion around touch point
                            fx += -tdy * invTDist * tFactor * 0.8f
                            fy += tdx * invTDist * tFactor * 0.8f
                        }
                        TouchMode.NONE -> {}
                    }
                }
            }

            // Velocity integration & damping
            var newVx = (vx[i] + fx * dt) * friction
            var newVy = (vy[i] + fy * dt) * friction

            // Speed limit to prevent numerical explosion
            val maxSpeed = 350f
            val speedSq = newVx * newVx + newVy * newVy
            if (speedSq > maxSpeed * maxSpeed) {
                val speed = sqrt(speedSq)
                val scale = maxSpeed / speed
                newVx *= scale
                newVy *= scale
            }

            vx[i] = newVx
            vy[i] = newVy

            // Position integration
            var newX = px + newVx * dt
            var newY = py + newVy * dt

            // Boundary handling
            if (isWrap) {
                newX = (newX % w + w) % w
                newY = (newY % h + h) % h
            } else {
                // Bounce
                if (newX < 0f) {
                    newX = 0f
                    vx[i] = -vx[i] * 0.8f
                } else if (newX > w) {
                    newX = w
                    vx[i] = -vx[i] * 0.8f
                }

                if (newY < 0f) {
                    newY = 0f
                    vy[i] = -vy[i] * 0.8f
                } else if (newY > h) {
                    newY = h
                    vy[i] = -vy[i] * 0.8f
                }
            }

            x[i] = newX
            y[i] = newY
        }
    }

    fun spawnAtTouch(touchX: Float, touchY: Float, targetSpecies: Int = 0) {
        if (count >= maxParticles) return
        val i = count
        count++
        x[i] = touchX + (Random.nextFloat() - 0.5f) * 20f
        y[i] = touchY + (Random.nextFloat() - 0.5f) * 20f
        vx[i] = (Random.nextFloat() - 0.5f) * 15f
        vy[i] = (Random.nextFloat() - 0.5f) * 15f
        species[i] = targetSpecies.coerceIn(0, numSpecies - 1)
    }
}
