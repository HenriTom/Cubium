package me.henritom.cubium.features.bookmark

class BookmarkManager {

    var bookmarks = mutableListOf<Bookmark>()

    fun deleteBookmark(id: Int) {
        bookmarks.removeIf { it.id == id }
    }

    fun getNextAvailableId(): Int {
        for (id in 1..Int.MAX_VALUE)
            if (bookmarks.none { it.id == id })
                return id

        return -1
    }
}