package de.thm.mow2.fleetbattlegameandroid.model.game

data class User(
    val documentId: String?,
    val username: String
) {
    companion object {
        const val COLLECTION_NAME: String = "users"
        fun toMap(user: User): Map<String, Any> {
            return mapOf(
                "username" to user.username as Any
            )
        }

        fun toObject(documentId: String?, map: Map<String, Any>): User? {
            return if (map["username"] == null) null
            else User(documentId, map["username"]!! as String)
        }
    }
}