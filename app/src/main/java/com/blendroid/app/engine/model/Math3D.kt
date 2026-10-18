package com.blendroid.app.engine.model

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    operator fun plus(v: Vector3) = Vector3(x + v.x, y + v.y, z + v.z)
    operator fun minus(v: Vector3) = Vector3(x - v.x, y - v.y, z - v.z)
    operator fun times(s: Float) = Vector3(x * s, y * s, z * s)
    operator fun div(s: Float) = if (s != 0f) Vector3(x / s, y / s, z / s) else this

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.00001f) Vector3(x / len, y / len, z / len) else Vector3(0f, 0f, 0f)
    }

    fun dot(v: Vector3): Float = x * v.x + y * v.y + z * v.z

    fun cross(v: Vector3): Vector3 = Vector3(
        y * v.z - z * v.y,
        z * v.x - x * v.z,
        x * v.y - y * v.x
    )

    companion object {
        val Zero = Vector3(0f, 0f, 0f)
        val One = Vector3(1f, 1f, 1f)
        val Up = Vector3(0f, 1f, 0f)
        val Forward = Vector3(0f, 0f, 1f)
        val Right = Vector3(1f, 0f, 0f)

        fun lerp(a: Vector3, b: Vector3, t: Float): Vector3 {
            val clampedT = t.coerceIn(0f, 1f)
            return Vector3(
                a.x + (b.x - a.x) * clampedT,
                a.y + (b.y - a.y) * clampedT,
                a.z + (b.z - a.z) * clampedT
            )
        }
    }
}

data class Color4(
    val r: Float = 0.8f,
    val g: Float = 0.8f,
    val b: Float = 0.8f,
    val a: Float = 1.0f
) {
    fun toArgbInt(): Int {
        val ai = (a.coerceIn(0f, 1f) * 255).toInt()
        val ri = (r.coerceIn(0f, 1f) * 255).toInt()
        val gi = (g.coerceIn(0f, 1f) * 255).toInt()
        val bi = (b.coerceIn(0f, 1f) * 255).toInt()
        return (ai shl 24) or (ri shl 16) or (gi shl 8) or bi
    }

    companion object {
        val DefaultGrey = Color4(0.7f, 0.7f, 0.7f, 1.0f)
        val DefaultOrange = Color4(0.95f, 0.5f, 0.1f, 1.0f)
        val White = Color4(1f, 1f, 1f, 1f)
        val Black = Color4(0f, 0f, 0f, 1f)
    }
}

data class Transform(
    val position: Vector3 = Vector3.Zero,
    val rotation: Vector3 = Vector3.Zero, // Pitch (X), Yaw (Y), Roll (Z) in degrees
    val scale: Vector3 = Vector3.One
) {
    fun applyToPoint(p: Vector3): Vector3 {
        // Apply scale
        var px = p.x * scale.x
        var py = p.y * scale.y
        var pz = p.z * scale.z

        // Apply Rotation (Euler X, Y, Z in radians)
        val radX = Math.toRadians(rotation.x.toDouble()).toFloat()
        val radY = Math.toRadians(rotation.y.toDouble()).toFloat()
        val radZ = Math.toRadians(rotation.z.toDouble()).toFloat()

        // X rotation
        var cy = py * cos(radX) - pz * sin(radX)
        var cz = py * sin(radX) + pz * cos(radX)
        py = cy
        pz = cz

        // Y rotation
        var cx = px * cos(radY) + pz * sin(radY)
        cz = -px * sin(radY) + pz * cos(radY)
        px = cx
        pz = cz

        // Z rotation
        cx = px * cos(radZ) - py * sin(radZ)
        cy = px * sin(radZ) + py * cos(radZ)
        px = cx
        py = cy

        // Apply translation
        return Vector3(px + position.x, py + position.y, pz + position.z)
    }

    companion object {
        val Identity = Transform()
    }
}
