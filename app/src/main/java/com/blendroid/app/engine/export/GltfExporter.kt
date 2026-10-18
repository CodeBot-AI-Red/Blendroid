package com.blendroid.app.engine.export

import com.blendroid.app.engine.model.SceneGraph
import org.json.JSONArray
import org.json.JSONObject

object GltfExporter {

    fun exportToGltfJson(scene: SceneGraph): String {
        val root = JSONObject()

        val asset = JSONObject()
        asset.put("version", "2.0")
        asset.put("generator", "Blendroid 3D Engine")
        root.put("asset", asset)

        val nodesArray = JSONArray()
        val meshesArray = JSONArray()
        val materialsArray = JSONArray()

        val sceneNodesIndices = JSONArray()

        for ((index, obj) in scene.objects.withIndex()) {
            val nodeObj = JSONObject()
            nodeObj.put("name", obj.name)

            val t = obj.transform
            val posArray = JSONArray().put(t.position.x).put(t.position.y).put(t.position.z)
            val scaleArray = JSONArray().put(t.scale.x).put(t.scale.y).put(t.scale.z)

            nodeObj.put("translation", posArray)
            nodeObj.put("scale", scaleArray)

            if (obj.mesh.vertices.isNotEmpty()) {
                val meshObj = JSONObject()
                meshObj.put("name", "${obj.name}_mesh")

                val primitives = JSONArray()
                val primObj = JSONObject()

                val attributes = JSONObject()
                // Attribute index references
                attributes.put("POSITION", 0)
                attributes.put("NORMAL", 1)
                primObj.put("attributes", attributes)
                primObj.put("material", index)

                primitives.put(primObj)
                meshObj.put("primitives", primitives)

                meshesArray.put(meshObj)
                nodeObj.put("mesh", index)
            }

            // Material definition
            val matObj = JSONObject()
            matObj.put("name", obj.material.name)
            val pbr = JSONObject()
            val baseColorFactor = JSONArray()
                .put(obj.material.baseColor.r)
                .put(obj.material.baseColor.g)
                .put(obj.material.baseColor.b)
                .put(obj.material.baseColor.a)

            pbr.put("baseColorFactor", baseColorFactor)
            pbr.put("metallicFactor", obj.material.metallic)
            pbr.put("roughnessFactor", obj.material.roughness)
            matObj.put("pbrMetallicRoughness", pbr)

            materialsArray.put(matObj)

            nodesArray.put(nodeObj)
            sceneNodesIndices.put(index)
        }

        root.put("nodes", nodesArray)
        root.put("meshes", meshesArray)
        root.put("materials", materialsArray)

        val scenesArray = JSONArray()
        val sceneObj = JSONObject()
        sceneObj.put("nodes", sceneNodesIndices)
        scenesArray.put(sceneObj)

        root.put("scenes", scenesArray)
        root.put("scene", 0)

        return root.toString(2)
    }
}
