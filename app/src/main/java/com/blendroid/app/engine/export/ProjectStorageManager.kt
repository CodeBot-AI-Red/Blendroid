package com.blendroid.app.engine.export

import android.content.Context
import com.blendroid.app.model.ProjectItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ProjectStorageManager {

    private const val PROJECTS_INDEX_FILE = "user_projects.json"

    fun loadProjects(context: Context): List<ProjectItem> {
        val file = File(context.filesDir, PROJECTS_INDEX_FILE)
        if (!file.exists()) {
            return emptyList()
        }

        return try {
            val jsonStr = FileInputStream(file).use { it.bufferedReader().readText() }
            val array = JSONArray(jsonStr)
            val result = mutableListOf<ProjectItem>()

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val lastModified = obj.optString("lastModified", "Agora")
                val description = obj.optString("description", "Projeto salvo na memória local")
                val isFavorite = obj.optBoolean("isFavorite", false)

                result.add(
                    ProjectItem(
                        id = id,
                        name = name,
                        lastModified = lastModified,
                        description = description,
                        isFavorite = isFavorite
                    )
                )
            }
            result
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun saveProjects(context: Context, projects: List<ProjectItem>) {
        try {
            val array = JSONArray()
            for (p in projects) {
                val obj = JSONObject()
                obj.put("id", p.id)
                obj.put("name", p.name)
                obj.put("lastModified", p.lastModified)
                obj.put("description", p.description)
                obj.put("isFavorite", p.isFavorite)
                array.put(obj)
            }

            val file = File(context.filesDir, PROJECTS_INDEX_FILE)
            FileOutputStream(file).use { fos ->
                fos.write(array.toString(2).toByteArray(Charsets.UTF_8))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createProject(context: Context, name: String, initialSceneJson: String? = null): ProjectItem {
        val projects = loadProjects(context).toMutableList()
        val id = System.currentTimeMillis().toString()
        val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val newProject = ProjectItem(
            id = id,
            name = name,
            lastModified = formattedDate,
            description = "Criado em $formattedDate"
        )

        projects.add(0, newProject)
        saveProjects(context, projects)

        if (!initialSceneJson.isNullOrEmpty()) {
            saveProjectScene(context, id, initialSceneJson)
        }

        return newProject
    }

    fun updateProject(context: Context, project: ProjectItem) {
        val projects = loadProjects(context).toMutableList()
        val index = projects.indexOfFirst { it.id == project.id }
        if (index != -1) {
            projects[index] = project
            saveProjects(context, projects)
        }
    }

    fun deleteProject(context: Context, projectId: String) {
        val projects = loadProjects(context).toMutableList()
        projects.removeAll { it.id == projectId }
        saveProjects(context, projects)

        val sceneFile = File(context.filesDir, "project_$projectId.blendroid")
        if (sceneFile.exists()) {
            sceneFile.delete()
        }
    }

    fun saveProjectScene(context: Context, projectId: String, sceneJson: String) {
        try {
            val sceneFile = File(context.filesDir, "project_$projectId.blendroid")
            FileOutputStream(sceneFile).use { fos ->
                fos.write(sceneJson.toByteArray(Charsets.UTF_8))
            }

            // Update project timestamp
            val projects = loadProjects(context).toMutableList()
            val index = projects.indexOfFirst { it.id == projectId }
            if (index != -1) {
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                projects[index] = projects[index].copy(
                    lastModified = formattedDate,
                    description = "Editado em $formattedDate"
                )
                saveProjects(context, projects)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadProjectScene(context: Context, projectId: String): String? {
        val sceneFile = File(context.filesDir, "project_$projectId.blendroid")
        if (!sceneFile.exists()) return null

        return try {
            FileInputStream(sceneFile).use { it.bufferedReader().readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
