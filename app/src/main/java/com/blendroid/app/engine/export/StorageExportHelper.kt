package com.blendroid.app.engine.export

import android.content.Context
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

object StorageExportHelper {

    fun saveToFile(context: Context, fileName: String, content: String): File? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(content.toByteArray(Charsets.UTF_8))
            }
            Toast.makeText(context, "Exportado com sucesso para: ${file.absolutePath}", Toast.LENGTH_LONG).show()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to internal app storage if external media not available
            try {
                val internalFile = File(context.filesDir, fileName)
                FileOutputStream(internalFile).use { fos ->
                    fos.write(content.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "Exportado internamente: ${internalFile.name}", Toast.LENGTH_LONG).show()
                internalFile
            } catch (ex: Exception) {
                Toast.makeText(context, "Erro ao exportar arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
                null
            }
        }
    }
}
