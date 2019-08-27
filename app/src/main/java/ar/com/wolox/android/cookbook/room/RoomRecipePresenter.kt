package ar.com.wolox.android.cookbook.room

import android.app.Application
import ar.com.wolox.android.cookbook.room.database.RoomDataEntity
import ar.com.wolox.android.cookbook.room.database.RoomDatabaseManager
import ar.com.wolox.wolmo.core.presenter.BasePresenter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoomRecipePresenter @Inject constructor(
    val application: Application
) : BasePresenter<RoomRecipeView>() {

    private lateinit var db: RoomDatabaseManager
    private var userName: String? = null

    override fun onViewAttached() {
        super.onViewAttached()
        db = RoomDatabaseManager.invoke(application.applicationContext)
    }

    fun onSessionButtonClicked(user: String) {

        userName?.let {
            userName = null
            view.logout()
        } ?: run {
            if (user.isNotEmpty()) {
                userName = user
                view.loginSuccess()
            } else {
                view.loginError()
            }
        }
    }

    fun onAddButtonClicked(data: String) {
        userName?.let {
            GlobalScope.launch {
                val entity = RoomDataEntity()
                var index = db.RoomDataDao().getLastIndex()
                if (index <= 0) {
                    index = 1
                } else {
                    index += 1
                }
                entity.id = index
                entity.user = it
                entity.data = data
                db.RoomDataDao().insertAll(entity)
            }
        }
    }

    fun onClearButtonClicked() {
        GlobalScope.launch {
            db.clearAllTables()
        }
    }
}