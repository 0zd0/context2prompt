package com.ozd0.context2prompt.core

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile

object FileContentReader {
    fun read(vf: VirtualFile): String = runReadAction {
        FileDocumentManager.getInstance().getDocument(vf)?.text ?: VfsUtilCore.loadText(vf)
    }
}
