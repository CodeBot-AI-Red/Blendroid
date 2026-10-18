package com.blendroid.app.model

data class ProjectItem(
    val id: String,
    val name: String,
    val lastModified: String,
    val description: String = "Foto salva quando saiu do projeto pela última vez",
    val thumbnailRes: Int? = null,
    val isFavorite: Boolean = false
)
