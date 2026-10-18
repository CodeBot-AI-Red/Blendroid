package com.blendroid.app.engine.export

import com.blendroid.app.engine.model.SceneGraph

object StlExporter {

    fun exportToStlAscii(scene: SceneGraph): String {
        val sb = StringBuilder()
        sb.append("solid blendroid_model\n")

        for (obj in scene.objects) {
            if (obj.mesh.vertices.isEmpty()) continue

            for (face in obj.mesh.faces) {
                if (face.vertexIndices.size < 3) continue

                val n = face.normal
                // Triangulate face if fan (v0, vi, vi+1)
                val v0 = obj.transform.applyToPoint(obj.mesh.vertices[face.vertexIndices[0]].position)

                for (i in 1 until face.vertexIndices.size - 1) {
                    val v1 = obj.transform.applyToPoint(obj.mesh.vertices[face.vertexIndices[i]].position)
                    val v2 = obj.transform.applyToPoint(obj.mesh.vertices[face.vertexIndices[i + 1]].position)

                    sb.append(String.format("  facet normal %.6e %.6e %.6e\n", n.x, n.y, n.z))
                    sb.append("    outer loop\n")
                    sb.append(String.format("      vertex %.6e %.6e %.6e\n", v0.x, v0.y, v0.z))
                    sb.append(String.format("      vertex %.6e %.6e %.6e\n", v1.x, v1.y, v1.z))
                    sb.append(String.format("      vertex %.6e %.6e %.6e\n", v2.x, v2.y, v2.z))
                    sb.append("    endloop\n")
                    sb.append("  endfacet\n")
                }
            }
        }

        sb.append("endsolid blendroid_model\n")
        return sb.toString()
    }
}
