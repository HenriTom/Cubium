import kotlinx.serialization.Serializable
import me.henritom.cubium.features.history.History

@Serializable
data class HistoryListContainer(val history: List<History>)