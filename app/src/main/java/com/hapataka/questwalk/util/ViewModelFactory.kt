package com.hapataka.questwalk.util

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hapataka.questwalk.data.firebase.repository.AuthRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.QuestStackRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.UserRepositoryImpl
import com.hapataka.questwalk.ui.login.LoginViewModel
import com.hapataka.questwalk.ui.mainactivity.MainViewModel
import com.hapataka.questwalk.ui.myinfo.MyInfoViewModel
import com.hapataka.questwalk.ui.record.RecordViewModel

class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepo = AuthRepositoryImpl()
        val userRepo = UserRepositoryImpl()
        val questRepo = QuestStackRepositoryImpl()
        val achieveRepo = AuthRepositoryImpl()

        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(authRepo, userRepo, questRepo, achieveRepo) as T
        }

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(authRepo) as T
        }

        if (modelClass.isAssignableFrom(MyInfoViewModel::class.java)) {
            return MyInfoViewModel(authRepo, userRepo) as T
        }

        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            return RecordViewModel(authRepo, userRepo) as T
        }
        throw IllegalArgumentException("unknown view model")
    }
}