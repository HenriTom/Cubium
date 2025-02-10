package me.henritom.cubium.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchEngine(val title: String, val description: String, val url: String, val searchUrl: String) {

    override fun toString(): String {
        return "SearchEngine{title='$title', description='$description', url='$url', searchUrl='$searchUrl'}"
    }
}