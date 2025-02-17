import kotlinx.serialization.Serializable
import me.henritom.cubium.features.bookmark.Bookmark

@Serializable
data class BookmarkListContainer(val bookmarks: List<Bookmark>)