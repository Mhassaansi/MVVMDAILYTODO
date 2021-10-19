package com.hsoftz.mvvmtodo.data

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.emptyPreferences
import androidx.datastore.preferences.preferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "PrefrencesManager"

enum class SortOrder { BY_NAME, BY_DATE }

data class FilterPrefrences(val sortOrder: SortOrder, val hideCompleted: Boolean)

@Singleton
class PrefrencesManager @Inject constructor(@ApplicationContext context: Context) {

    private val dataStore = context.createDataStore("user_prefrences")

    val prefrencesFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
                Log.e(TAG, "Error Reading Prefrences", exception)
            } else {
                throw exception
            }
        }
        .map { prefrences ->
            val sortOrder = SortOrder.valueOf(
                prefrences[PrefrenceKeys.SORT_ORDER] ?: SortOrder.BY_DATE.name
            )
            val hideCompeletd = prefrences[PrefrenceKeys.HIDE_COMPELETEd] ?: false

            FilterPrefrences(sortOrder, hideCompeletd)
        }


    suspend fun updatedSortOrder(sortOrder: SortOrder) {
        dataStore.edit { prefrences ->
            prefrences[PrefrenceKeys.SORT_ORDER] = sortOrder.name
        }
    }

    suspend fun updatedHideCompeletd(hideCompleted: Boolean) {
        dataStore.edit { prefrences ->
            prefrences[PrefrenceKeys.HIDE_COMPELETEd] = hideCompleted
        }
    }

    private object PrefrenceKeys {
        val SORT_ORDER = preferencesKey<String>("sort_order")
        val HIDE_COMPELETEd = preferencesKey<Boolean>("hide_compeleted")
    }


}