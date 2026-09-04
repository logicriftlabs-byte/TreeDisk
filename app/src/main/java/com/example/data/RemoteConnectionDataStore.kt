package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "remote_connections")

class RemoteConnectionDataStore(private val context: Context) {

    private val connectionsKey = stringPreferencesKey("connections_json")

    val connectionsFlow: Flow<List<RemoteConnection>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[connectionsKey] ?: "[]"
        parseConnections(jsonString)
    }

    suspend fun saveConnection(connection: RemoteConnection): RemoteConnection {
        var savedConnection = connection
        context.dataStore.edit { preferences ->
            val jsonString = preferences[connectionsKey] ?: "[]"
            val currentList = parseConnections(jsonString).toMutableList()
            // generate ID if 0
            val newConnection = if (connection.id == 0L) {
                connection.copy(id = System.currentTimeMillis())
            } else {
                connection
            }
            savedConnection = newConnection
            // replace if exists
            val existingIndex = currentList.indexOfFirst { it.id == newConnection.id }
            if (existingIndex != -1) {
                currentList[existingIndex] = newConnection
            } else {
                currentList.add(newConnection)
            }
            preferences[connectionsKey] = serializeConnections(currentList)
        }
        return savedConnection
    }

    suspend fun deleteConnection(connection: RemoteConnection) {
        context.dataStore.edit { preferences ->
            val jsonString = preferences[connectionsKey] ?: "[]"
            val currentList = parseConnections(jsonString).toMutableList()
            currentList.removeAll { it.id == connection.id }
            preferences[connectionsKey] = serializeConnections(currentList)
        }
    }

    private fun parseConnections(jsonString: String): List<RemoteConnection> {
        val list = mutableListOf<RemoteConnection>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObj = jsonArray.getJSONObject(i)
                list.add(
                    RemoteConnection(
                        id = jsonObj.optLong("id", 0),
                        name = jsonObj.optString("name", ""),
                        type = ConnectionType.valueOf(jsonObj.optString("type", "SFTP")),
                        host = jsonObj.optString("host", ""),
                        port = jsonObj.optInt("port", 22),
                        username = jsonObj.optString("username", ""),
                        password = SecurityUtils.decrypt(jsonObj.optString("password", "")),
                        remotePath = jsonObj.optString("remotePath", "/"),
                    ),
                )
            }
        } catch (_: Exception) {
            // parse fallback
        }
        return list
    }

    private fun serializeConnections(list: List<RemoteConnection>): String {
        val jsonArray = JSONArray()
        list.forEach { conn ->
            val jsonObj = JSONObject().apply {
                put("id", conn.id)
                put("name", conn.name)
                put("type", conn.type.name)
                put("host", conn.host)
                put("port", conn.port)
                put("username", conn.username)
                put("password", SecurityUtils.encrypt(conn.password))
                put("remotePath", conn.remotePath)
            }
            jsonArray.put(jsonObj)
        }
        return jsonArray.toString()
    }
}
