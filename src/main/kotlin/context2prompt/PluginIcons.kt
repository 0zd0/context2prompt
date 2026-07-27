package com.ozd0.context2prompt

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object PluginIcons {
    @JvmField val LOGO: Icon = load("/icons/context2prompt.svg")
    @JvmField val PROBLEMS_LINE: Icon = load("/icons/problemsLine.svg")
    @JvmField val PROBLEMS_LINE_FILE: Icon = load("/icons/problemsLineFile.svg")
    @JvmField val PROBLEMS_ALL: Icon = load("/icons/problemsAll.svg")
    @JvmField val PROBLEMS_ALL_FILE: Icon = load("/icons/problemsAllFile.svg")

    private fun load(path: String): Icon = IconLoader.getIcon(path, PluginIcons::class.java)
}
