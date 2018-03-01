package net.dankito.jpa.apt.util

import java.io.File


class FileUtil {

    fun deleteFolderAndItsContent(file: File) {
        if(file.isDirectory) {
            file.listFiles().toList().forEach {
                deleteFolderAndItsContent(it)
            }
        }

        file.delete()
    }

}