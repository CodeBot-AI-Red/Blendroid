package com.blendroid.app.engine.export

import com.blendroid.app.engine.model.Color4
import com.blendroid.app.engine.model.Face
import com.blendroid.app.engine.model.MaterialData
import com.blendroid.app.engine.model.MeshData
import com.blendroid.app.engine.model.MeshPrimitiveType
import com.blendroid.app.engine.model.SceneGraph
import com.blendroid.app.engine.model.SceneObject
import com.blendroid.app.engine.model.Transform
import com.blendroid.app.engine.model.Vector3
import com.blendroid.app.engine.model.Vertex
import org.json.JSONArray
import org.json.JSONObject

object BlendroidExporter {

    fun exportToJson(scene: SceneGraph): String {
        val root = JSONObject()
        root.put("version", "1.0")
        root.put("activeObjectId", scene.activeObjectId)
        root.put("currentFrame", scene.currentFrame)
        root.put("totalFrames", scene.totalFrames)

        val objectsArray = JSONArray()
        for (obj in scene.objects) {
            val objJson = JSONObject()
            objJson.put("id", obj.id)
            objJson.put("name", obj.name)
            objJson.put("type", obj.type.name)

            // Transform
            val transformJson = JSONObject()
            val p = obj.transform.position
            val r = obj.transform.rotation
            val s = obj.transform.scale

            transformJson.put("px", p.x)
            transformJson.put("py", p.y)
            transformJson.put("pz", p.z)
            transformJson.put("rx", r.x)
            transformJson.put("ry", r.y)
            transformJson.put("rz", r.z)
            transformJson.put("sx", s.x)
            transformJson.put("sy", s.y)
            transformJson.put("sz", s.z)
            objJson.put("transform", transformJson)

            // Material
            val matJson = JSONObject()
            matJson.put("id", obj.material.id)
            matJson.put("name", obj.material.name)
            matJson.put("r", obj.material.baseColor.r)
            matJson.put("g", obj.material.baseColor.g)
            matJson.put("b", obj.material.baseColor.b)
            matJson.put("a", obj.material.baseColor.a)
            matJson.put("metallic", obj.material.metallic)
            matJson.put("roughness", obj.material.roughness)
            objJson.put("material", matJson)

            // Mesh
            val meshJson = JSONObject()
            val vertsArray = JSONArray()
            for (v in obj.mesh.vertices) {
                val vJ = JSONObject()
                vJ.put("id", v.id)
                vJ.put("x", v.position.x)
                vJ.put("y", v.position.y)
                vJ.put("z", v.position.z)
                vertsArray.put(vJ)
            }
            meshJson.put("vertices", vertsArray)

            val facesArray = JSONArray()
            for (f in obj.mesh.faces) {
                val fJ = JSONObject()
                fJ.put("id", f.id)
                val idxArr = JSONArray()
                f.vertexIndices.forEach { idxArr.put(it) }
                fJ.put("indices", idxArr)
                facesArray.put(fJ)
            }
            meshJson.put("faces", facesArray)

            objJson.put("mesh", meshJson)
            objectsArray.put(objJson)
        }

        root.put("objects", objectsArray)
        return root.toString(2)
    }

    fun importFromJson(jsonStr: String): SceneGraph {
        val scene = SceneGraph()
        scene.objects.clear()

        val root = JSONObject(jsonStr)
        scene.activeObjectId = if (root.has("activeObjectId") && !root.isNull("activeObjectId")) root.getString("activeObjectId") else null
        scene.currentFrame = root.optInt("currentFrame", 1)
        scene.totalFrames = root.optInt("totalFrames", 250)

        val objectsArray = root.getJSONArray("objects")
        for (i in 0 until objectsArray.length()) {
            val objJ = objectsArray.getJSONObject(i)
            val id = objJ.getString("id")
            val name = objJ.getString("name")
            val typeStr = objJ.getString("type")
            val type = try { MeshPrimitiveType.valueOf(typeStr) } catch (e: Exception) { MeshPrimitiveType.CUBE }

            val tJ = objJ.getJSONObject("transform")
            val pos = Vector3(tJ.getDouble("px").toFloat(), tJ.getDouble("py").toFloat(), tJ.getDouble("pz").toFloat())
            val rot = Vector3(tJ.getDouble("rx").toFloat(), tJ.getDouble("ry").toFloat(), tJ.getDouble("rz").toFloat())
            val scl = Vector3(tJ.getDouble("sx").toFloat(), tJ.getDouble("sy").toFloat(), tJ.getDouble("sz").toFloat())
            val transform = Transform(pos, rot, scl)

            val mJ = objJ.getJSONObject("material")
            val mat = MaterialData(
                id = mJ.optString("id", System.currentTimeMillis().toString()),
                name = mJ.optString("name", "Material"),
                baseColor = Color4(
                    mJ.getDouble("r").toFloat(),
                    mJ.getDouble("g").toFloat(),
                    mJ.getDouble("b").toFloat(),
                    mJ.getDouble("a").toFloat()
                ),
                metallic = mJ.getDouble("metallic").toFloat(),
                roughness = mJ.getDouble("roughness").toFloat()
            )

            val meshJ = objJ.getJSONObject("mesh")
            val vArr = meshJ.getJSONArray("vertices")
            val vertsList = mutableListOf<Vertex>()
            for (vI in 0 until vArr.length()) {
                val vObj = vArr.getJSONObject(vI)
                vertsList.add(
                    Vertex(
                        id = vObj.getInt("id"),
                        position = Vector3(
                            vObj.getDouble("x").toFloat(),
                            vObj.getDouble("y").toFloat(),
                            vObj.getDouble("z").toFloat()
                        )
                    )
                )
            }

            val fArr = meshJ.getJSONArray("faces")
            val facesList = mutableListOf<Face>()
            for (fI in 0 until fArr.length()) {
                val fObj = fArr.getJSONObject(fI)
                val idxJ = fObj.getJSONArray("indices")
                val indices = mutableListOf<Int>()
                for (k in 0 until idxJ.length()) {
                    indices.add(idxJ.getInt(k))
                }
                facesList.add(
                    Face(
                        id = fObj.getInt("id"),
                        vertexIndices = indices
                    )
                )
            }

            val meshData = MeshData(vertsList, mutableListOf(), facesList)
            meshData.computeNormals()

            val sceneObj = SceneObject(
                id = id,
                name = name,
                type = type,
                transform = transform,
                mesh = meshData,
                material = mat
            )

            scene.objects.add(sceneObj)
        }

        return scene
    }
}
