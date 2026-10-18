package com.blendroid.app.engine.model

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Vertex(
    val id: Int,
    var position: Vector3,
    var normal: Vector3 = Vector3.Up,
    var uv: Pair<Float, Float> = Pair(0f, 0f),
    var isSelected: Boolean = false
)

data class Edge(
    val id: Int,
    val v1: Int,
    val v2: Int,
    var isSelected: Boolean = false
)

data class Face(
    val id: Int,
    val vertexIndices: List<Int>,
    var normal: Vector3 = Vector3.Up,
    var isSelected: Boolean = false
)

enum class MeshPrimitiveType {
    CUBE, SPHERE, CYLINDER, PLANE, TORUS, MONKEY, CONE, ARMATURE, LIGHT, CAMERA
}

enum class ModifierType {
    SUBDIVISION_SURFACE, MIRROR, ARRAY, SOLIDIFY
}

data class Modifier(
    val id: String,
    val name: String,
    val type: ModifierType,
    val countOrLevel: Int = 1,
    val isEnabled: Boolean = true
)

data class MeshData(
    val vertices: MutableList<Vertex> = mutableListOf(),
    val edges: MutableList<Edge> = mutableListOf(),
    val faces: MutableList<Face> = mutableListOf()
) {
    fun clone(): MeshData {
        val newVerts = vertices.map { it.copy() }.toMutableList()
        val newEdges = edges.map { it.copy() }.toMutableList()
        val newFaces = faces.map { it.copy(vertexIndices = it.vertexIndices.toList()) }.toMutableList()
        return MeshData(newVerts, newEdges, newFaces)
    }

    fun computeNormals() {
        for (f in faces) {
            if (f.vertexIndices.size >= 3) {
                val p0 = vertices[f.vertexIndices[0]].position
                val p1 = vertices[f.vertexIndices[1]].position
                val p2 = vertices[f.vertexIndices[2]].position
                val v1 = p1 - p0
                val v2 = p2 - p0
                f.normal = v1.cross(v2).normalized()
            }
        }
    }

    fun extrudeSelectedFaces(distance: Float = 0.5f) {
        val selectedFacesList = faces.filter { it.isSelected }
        if (selectedFacesList.isEmpty()) return

        for (face in selectedFacesList) {
            val oldVertIndices = face.vertexIndices
            val newVertIndices = mutableListOf<Int>()
            val offset = face.normal * distance

            // Create new vertices moved by face normal offset
            for (vIdx in oldVertIndices) {
                val oldV = vertices[vIdx]
                val newVId = vertices.size
                vertices.add(Vertex(newVId, oldV.position + offset, face.normal, oldV.uv, true))
                newVertIndices.add(newVId)
            }

            // Unselect old face, update its vertices to new ones
            face.isSelected = false

            // Create side faces bridging old and new edge boundaries
            val count = oldVertIndices.size
            for (i in 0 until count) {
                val nextI = (i + 1) % count
                val oldV1 = oldVertIndices[i]
                val oldV2 = oldVertIndices[nextI]
                val newV1 = newVertIndices[i]
                val newV2 = newVertIndices[nextI]

                val sideFace = Face(
                    id = faces.size,
                    vertexIndices = listOf(oldV1, oldV2, newV2, newV1),
                    normal = face.normal
                )
                faces.add(sideFace)
            }

            // Add new top face
            faces.add(Face(id = faces.size, vertexIndices = newVertIndices, normal = face.normal, isSelected = true))
        }
        computeNormals()
    }

    fun subdivide() {
        val newFaces = mutableListOf<Face>()
        val newVerts = vertices.toMutableList()

        for (face in faces) {
            if (face.vertexIndices.size == 4) {
                val v0 = newVerts[face.vertexIndices[0]].position
                val v1 = newVerts[face.vertexIndices[1]].position
                val v2 = newVerts[face.vertexIndices[2]].position
                val v3 = newVerts[face.vertexIndices[3]].position

                val centerPos = (v0 + v1 + v2 + v3) * 0.25f
                val centerIdx = newVerts.size
                newVerts.add(Vertex(centerIdx, centerPos, face.normal))

                val m01Pos = (v0 + v1) * 0.5f
                val m01Idx = newVerts.size
                newVerts.add(Vertex(m01Idx, m01Pos, face.normal))

                val m12Pos = (v1 + v2) * 0.5f
                val m12Idx = newVerts.size
                newVerts.add(Vertex(m12Idx, m12Pos, face.normal))

                val m23Pos = (v2 + v3) * 0.5f
                val m23Idx = newVerts.size
                newVerts.add(Vertex(m23Idx, m23Pos, face.normal))

                val m30Pos = (v3 + v0) * 0.5f
                val m30Idx = newVerts.size
                newVerts.add(Vertex(m30Idx, m30Pos, face.normal))

                // Sub-quad 1
                newFaces.add(Face(newFaces.size, listOf(face.vertexIndices[0], m01Idx, centerIdx, m30Idx), face.normal))
                // Sub-quad 2
                newFaces.add(Face(newFaces.size, listOf(m01Idx, face.vertexIndices[1], m12Idx, centerIdx), face.normal))
                // Sub-quad 3
                newFaces.add(Face(newFaces.size, listOf(centerIdx, m12Idx, face.vertexIndices[2], m23Idx), face.normal))
                // Sub-quad 4
                newFaces.add(Face(newFaces.size, listOf(m30Idx, centerIdx, m23Idx, face.vertexIndices[3]), face.normal))
            } else {
                newFaces.add(face)
            }
        }
        vertices.clear()
        vertices.addAll(newVerts)
        faces.clear()
        faces.addAll(newFaces)
        computeNormals()
    }

    companion object {
        fun createCube(size: Float = 1f): MeshData {
            val hs = size / 2f
            val verts = mutableListOf(
                Vertex(0, Vector3(-hs, -hs, hs)),
                Vertex(1, Vector3(hs, -hs, hs)),
                Vertex(2, Vector3(hs, hs, hs)),
                Vertex(3, Vector3(-hs, hs, hs)),
                Vertex(4, Vector3(-hs, -hs, -hs)),
                Vertex(5, Vector3(hs, -hs, -hs)),
                Vertex(6, Vector3(hs, hs, -hs)),
                Vertex(7, Vector3(-hs, hs, -hs))
            )
            val faces = mutableListOf(
                Face(0, listOf(0, 1, 2, 3), Vector3(0f, 0f, 1f)),  // Front
                Face(1, listOf(5, 4, 7, 6), Vector3(0f, 0f, -1f)), // Back
                Face(2, listOf(4, 0, 3, 7), Vector3(-1f, 0f, 0f)), // Left
                Face(3, listOf(1, 5, 6, 2), Vector3(1f, 0f, 0f)),  // Right
                Face(4, listOf(3, 2, 6, 7), Vector3(0f, 1f, 0f)),  // Top
                Face(5, listOf(4, 5, 1, 0), Vector3(0f, -1f, 0f))  // Bottom
            )
            val mesh = MeshData(verts, mutableListOf(), faces)
            mesh.computeNormals()
            return mesh
        }

        fun createPlane(size: Float = 2f): MeshData {
            val hs = size / 2f
            val verts = mutableListOf(
                Vertex(0, Vector3(-hs, 0f, hs)),
                Vertex(1, Vector3(hs, 0f, hs)),
                Vertex(2, Vector3(hs, 0f, -hs)),
                Vertex(3, Vector3(-hs, 0f, -hs))
            )
            val faces = mutableListOf(
                Face(0, listOf(0, 1, 2, 3), Vector3(0f, 1f, 0f))
            )
            return MeshData(verts, mutableListOf(), faces)
        }

        fun createSphere(radius: Float = 1f, rings: Int = 8, sectors: Int = 12): MeshData {
            val verts = mutableListOf<Vertex>()
            val faces = mutableListOf<Face>()

            val R = 1f / (rings - 1).toFloat()
            val S = 1f / (sectors - 1).toFloat()

            for (r in 0 until rings) {
                for (s in 0 until sectors) {
                    val y = sin(-PI.toFloat() / 2f + PI.toFloat() * r * R)
                    val x = cos(2f * PI.toFloat() * s * S) * sin(PI.toFloat() * r * R)
                    val z = sin(2f * PI.toFloat() * s * S) * sin(PI.toFloat() * r * R)

                    val pos = Vector3(x * radius, y * radius, z * radius)
                    verts.add(Vertex(verts.size, pos, pos.normalized(), Pair(s * S, r * R)))
                }
            }

            for (r in 0 until rings - 1) {
                for (s in 0 until sectors - 1) {
                    val v0 = r * sectors + s
                    val v1 = r * sectors + (s + 1)
                    val v2 = (r + 1) * sectors + (s + 1)
                    val v3 = (r + 1) * sectors + s
                    faces.add(Face(faces.size, listOf(v0, v1, v2, v3)))
                }
            }

            val mesh = MeshData(verts, mutableListOf(), faces)
            mesh.computeNormals()
            return mesh
        }

        fun createCylinder(radius: Float = 1f, height: Float = 2f, segments: Int = 12): MeshData {
            val verts = mutableListOf<Vertex>()
            val faces = mutableListOf<Face>()
            val hh = height / 2f

            val bottomCenter = 0
            val topCenter = 1
            verts.add(Vertex(0, Vector3(0f, -hh, 0f), Vector3(0f, -1f, 0f)))
            verts.add(Vertex(1, Vector3(0f, hh, 0f), Vector3(0f, 1f, 0f)))

            val bottomRingStart = 2

            for (i in 0 until segments) {
                val angle = (2 * PI * i / segments).toFloat()
                val x = cos(angle) * radius
                val z = sin(angle) * radius

                verts.add(Vertex(verts.size, Vector3(x, -hh, z)))
                verts.add(Vertex(verts.size, Vector3(x, hh, z)))
            }

            for (i in 0 until segments) {
                val next = (i + 1) % segments
                val b1 = bottomRingStart + i * 2
                val t1 = bottomRingStart + i * 2 + 1
                val b2 = bottomRingStart + next * 2
                val t2 = bottomRingStart + next * 2 + 1

                // Side Quad Face
                faces.add(Face(faces.size, listOf(b1, b2, t2, t1)))
                // Top Triangle
                faces.add(Face(faces.size, listOf(topCenter, t1, t2)))
                // Bottom Triangle
                faces.add(Face(faces.size, listOf(bottomCenter, b2, b1)))
            }

            val mesh = MeshData(verts, mutableListOf(), faces)
            mesh.computeNormals()
            return mesh
        }

        fun createCone(radius: Float = 1f, height: Float = 2f, segments: Int = 12): MeshData {
            val verts = mutableListOf<Vertex>()
            val faces = mutableListOf<Face>()
            val hh = height / 2f

            val topTip = 0
            val bottomCenter = 1
            verts.add(Vertex(0, Vector3(0f, hh, 0f), Vector3(0f, 1f, 0f)))
            verts.add(Vertex(1, Vector3(0f, -hh, 0f), Vector3(0f, -1f, 0f)))

            val ringStart = 2
            for (i in 0 until segments) {
                val angle = (2 * PI * i / segments).toFloat()
                val x = cos(angle) * radius
                val z = sin(angle) * radius
                verts.add(Vertex(verts.size, Vector3(x, -hh, z)))
            }

            for (i in 0 until segments) {
                val next = (i + 1) % segments
                val v1 = ringStart + i
                val v2 = ringStart + next

                // Side Triangle Face
                faces.add(Face(faces.size, listOf(topTip, v1, v2)))
                // Bottom Triangle Face
                faces.add(Face(faces.size, listOf(bottomCenter, v2, v1)))
            }

            val mesh = MeshData(verts, mutableListOf(), faces)
            mesh.computeNormals()
            return mesh
        }

        fun createTorus(mainRadius: Float = 1f, tubeRadius: Float = 0.3f, mainSegments: Int = 12, tubeSegments: Int = 8): MeshData {
            val verts = mutableListOf<Vertex>()
            val faces = mutableListOf<Face>()

            for (i in 0 until mainSegments) {
                val u = (2 * PI * i / mainSegments).toFloat()
                val cu = cos(u)
                val su = sin(u)

                for (j in 0 until tubeSegments) {
                    val v = (2 * PI * j / tubeSegments).toFloat()
                    val cv = cos(v)
                    val sv = sin(v)

                    val x = (mainRadius + tubeRadius * cv) * cu
                    val y = tubeRadius * sv
                    val z = (mainRadius + tubeRadius * cv) * su

                    verts.add(Vertex(verts.size, Vector3(x, y, z)))
                }
            }

            for (i in 0 until mainSegments) {
                val nextI = (i + 1) % mainSegments
                for (j in 0 until tubeSegments) {
                    val nextJ = (j + 1) % tubeSegments

                    val v0 = i * tubeSegments + j
                    val v1 = nextI * tubeSegments + j
                    val v2 = nextI * tubeSegments + nextJ
                    val v3 = i * tubeSegments + nextJ

                    faces.add(Face(faces.size, listOf(v0, v1, v2, v3)))
                }
            }

            val mesh = MeshData(verts, mutableListOf(), faces)
            mesh.computeNormals()
            return mesh
        }

        fun createMonkey(size: Float = 1.2f): MeshData {
            // Iconic Blender Suzanne stylized low-poly head representation
            val hs = size / 2f
            val verts = mutableListOf(
                Vertex(0, Vector3(0f, hs * 0.8f, hs * 0.5f)),   // Forehead top
                Vertex(1, Vector3(-hs * 0.8f, hs * 0.4f, 0f)),  // Left Brow
                Vertex(2, Vector3(hs * 0.8f, hs * 0.4f, 0f)),   // Right Brow
                Vertex(3, Vector3(-hs * 1.2f, hs * 0.2f, -hs * 0.4f)), // Left Ear
                Vertex(4, Vector3(hs * 1.2f, hs * 0.2f, -hs * 0.4f)),  // Right Ear
                Vertex(5, Vector3(0f, 0f, hs * 1.2f)),          // Snout / Nose
                Vertex(6, Vector3(-hs * 0.5f, -hs * 0.4f, hs * 0.6f)), // Left Cheek
                Vertex(7, Vector3(hs * 0.5f, -hs * 0.4f, hs * 0.6f)),  // Right Cheek
                Vertex(8, Vector3(0f, -hs * 0.8f, hs * 0.2f))   // Chin
            )
            val faces = mutableListOf(
                Face(0, listOf(0, 1, 5)),
                Face(1, listOf(0, 5, 2)),
                Face(2, listOf(1, 3, 6)),
                Face(3, listOf(2, 7, 4)),
                Face(4, listOf(5, 6, 8)),
                Face(5, listOf(5, 8, 7)),
                Face(6, listOf(0, 3, 1)),
                Face(7, listOf(0, 2, 4)),
                Face(8, listOf(6, 3, 8)),
                Face(9, listOf(7, 8, 4))
            )
            val mesh = MeshData(verts, mutableListOf(), faces)
            mesh.computeNormals()
            return mesh
        }
    }
}
