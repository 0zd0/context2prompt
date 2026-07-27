package com.ozd0.context2prompt.core

data class SelectedItem<T>(
    val payload: T,
    val relativePath: String,
    val isDirectory: Boolean,
)

data class SplitSelection<T>(
    val normal: List<SelectedItem<T>>,
    val ignored: List<SelectedItem<T>>,
) {
    val hasIgnored: Boolean get() = ignored.isNotEmpty()
}