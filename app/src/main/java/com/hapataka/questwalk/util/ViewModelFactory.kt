package com.hapataka.questwalk.util

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hapataka.questwalk.data.cloudvision.repository.OcrRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.AchieveItemRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.AuthRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.ImageRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.QuestStackRepositoryImpl
import com.hapataka.questwalk.data.firebase.repository.UserRepositoryImpl
import com.hapataka.questwalk.data.fusedlocation.repository.LocationRepositoryImpl
import com.hapataka.questwalk.data.pref.repository.LocalRepositoryImpl
import com.hapataka.questwalk.domain.repository.LocalRepository
import com.hapataka.questwalk.domain.repository.LocationRepository
import com.hapataka.questwalk.ui.home.HomeViewModel
import com.hapataka.questwalk.ui.login.LoginViewModel
import com.hapataka.questwalk.ui.mainactivity.ImageUtil
import com.hapataka.questwalk.ui.mainactivity.MainViewModel
import com.hapataka.questwalk.ui.myinfo.MyInfoViewModel
import com.hapataka.questwalk.ui.record.RecordViewModel
import com.hapataka.questwalk.ui.result.ResultViewModel
import com.hapataka.questwalk.ui.signup.SignUpViewModel

class ViewModelFactory() : ViewModelProvider.Factory {
    private lateinit var locationRepo: LocationRepository
    private lateinit var imageUtil: ImageUtil
    private lateinit var localRepo: LocalRepository
    constructor(context: Context) : this() {
        this.locationRepo = LocationRepositoryImpl(context)
        this.imageUtil = ImageUtil(context)
        this.localRepo = LocalRepositoryImpl(context)
    }
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val authRepo = AuthRepositoryImpl()
        val userRepo = UserRepositoryImpl()
        val questRepo = QuestStackRepositoryImpl()
        val imageRepo = ImageRepositoryImpl()
        val ocrRepo = OcrRepositoryImpl()
        val achieveItemRepo = AchieveItemRepositoryImpl()


        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(userRepo, questRepo, imageRepo, ocrRepo, locationRepo, imageUtil) as T
        }

        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(localRepo) as T
        }

        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel(authRepo, localRepo) as T
        }

        if (modelClass.isAssignableFrom(MyInfoViewModel::class.java)) {
            return MyInfoViewModel(authRepo, userRepo) as T
        }

        if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
            return RecordViewModel(authRepo, userRepo, achieveItemRepo) as T
        }

        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(authRepo) as T
        }

        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(userRepo, questRepo) as T
        }

        throw IllegalArgumentException("unknown view model")
    }
}