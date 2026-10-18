package com.blendroid.app.engine.export

import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.SceneObject

object ObjExporter {

    fun exportToObj(scene: SceneGraph): String {
        val sb = StringBuilder()
        sb.append("# Blendroid 3D Wavefront OBJ Exporter\n")
        sb.append("mtllib scene.mtl\n\n")

        var vertexOffset = 1
        var uvOffset = 1
        var normalOffset = 1

        for (obj in scene.objects) {
            if (obj.mesh.vertices.isEmpty()) continue
            sb.append("o ").append(obj.name.replace(" ", "_")).append("\n")
            sb.append("usemtl ").append(obj.material.name.replace(" ", "_")).append("\n")

            // Write transformed vertices
            for (v in obj.mesh.vertices) {
                val p = obj.transform.applyToPoint(v.position)
                sb.append(String.format("v %.4f %.4f %.4f\n", p.x, p.y, p.z))
            }

            // Write UVs
            for (v in obj.mesh.vertices) {
                sb.append(String.format("vt %.4f %.4f\n", v.uv.first, v.uv.second))
            }

            // Write Normals
            for (f in obj.mesh.faces) {
                val n = f.normal
                sb.append(String.format("vn %.4f %.4f %.4f\n", n.x, n.y, n.z))
            }

            // Write Faces (1-based index)
            for ((faceIndex, f) in obj.mesh.faces.withIndex()) {
                sb.append("f")
                val nIdx = normalOffset + faceIndex
                for (vIdx in f.vertexIndices) {
                    val actualV = vertexOffset + vIdx
                    val actualUv = uvOffset + vIdx
                    sb.append(String.format(" %d/%d/%d", actualV, actualUv, nIdx))
                }
                sb.append("\n")
            }

            vertexOffset += obj.mesh.vertices.size
            uvOffset += obj.mesh.vertices.size
            normalOffset += obj.mesh.faces.size
            sb.append("\n")
        }

        return sb.toString()
    }

    fun exportToMtl(scene: SceneGraph): String {
        val sb = StringBuilder()
        sb.append("# Blendroid Material Library File\n\n")

        val materialsUsed = scene.objects.map { it.material }.distinctBy { it.name }

        for (mat in materialsUsed) {
            sb.append("newmtl ").append(mat.name.replace(" ", "_")).append("\n")
            val c = mat.baseColor
            sb.append(String.format("Kd %.4f %.4f %.4f\n", c.r, c.g, c.b))
            sb.append(String.format("Ka %.4f %.4f %.4f\n", c.r * 0.2f, c.g * 0.2f, c.b * 0.2f))
            sb.append(String.format("Ks %.4f %.4f %.4f\n", mat.metallic, mat.metallic, mat.metallic))
            sb.append(String.format("Ns %.1f\n", (1.0f - mat.roughness) * 100f))
            sb.append(String.format("d %.4f\n", mat.alpha))
            sb.append("illum 2\n\n")
        }

        return sb.toString()
    }
}
