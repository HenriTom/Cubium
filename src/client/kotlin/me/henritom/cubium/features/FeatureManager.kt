package me.henritom.cubium.features

class FeatureManager {

    var features = mutableMapOf<String, Boolean>()

    init {
        features["history"] = true
        features["warden"] = true
        features["darkreader"] = true
    }

    fun toggleFeature(key: String) {
        features[key.lowercase()]?.let {
            features[key.lowercase()] = !it
        }
    }
}