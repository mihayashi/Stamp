package jp.gogo_south.stamp

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

object UUIDManager {
    private const val PREFS_NAME = "jp.gogo_south.stamp.prefs"
    private const val KEY_UUID = "key_uuid"

    fun getUUID(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var uuid = prefs.getString(KEY_UUID, null)
        if (uuid == null) {
            uuid = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_UUID, uuid).apply()
        }
        return uuid
    }
}
