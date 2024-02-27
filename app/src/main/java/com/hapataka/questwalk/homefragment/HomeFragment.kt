package com.hapataka.questwalk.homefragment

import android.content.res.ColorStateList
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.SystemClock
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.hapataka.questwalk.R
import com.hapataka.questwalk.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {


    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding

    //모험 대기(wait),모험 시작(play)
    private var isPlay = false

    //타이머
    private lateinit var timer : Chronometer
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        timer = binding.cmQuestTime
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        // TODO: Use the ViewModel
        initView()
    }

    private fun initView() {
        setQuestButtonEvent()

    }


    private fun setQuestButtonEvent(){
        with(binding) {
            btnQuestStatus.setOnClickListener {
                if (!isPlay) { // 모험 시작!
                    clPlayingBottomWidgets.visibility = View.VISIBLE
                    clPlayingUpperWidgets.visibility = View.VISIBLE
                    clWaitingBottomWidgets.visibility = View.GONE
                    isPlay = true

                    //버튼색 이름 및 색깔 변경
                    btnQuestStatus.text = "포기하기"
                    setBackgroundWidget(btnQuestStatus,R.color.red)

                    //타이머
                    timer.base = SystemClock.elapsedRealtime()
                    timer.start()
                } else { // 모험이 끝날때!
                    clPlayingBottomWidgets.visibility = View.GONE
                    clPlayingUpperWidgets.visibility = View.GONE
                    clWaitingBottomWidgets.visibility = View.VISIBLE
                    isPlay = false


                    //버튼색 이름 및 색깔 변경
                    btnQuestStatus.text = "모험 시작하기"
                    setBackgroundWidget(btnQuestStatus,R.color.green)

                    //타이머 중지
                    timer.stop()
                }
            }
        }
    }


    // backgroundTint 값 변경
    private fun setBackgroundWidget(view : View,colorResource :Int){
        val colorStateList = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                colorResource
            )
        )
        ViewCompat.setBackgroundTintList(view, colorStateList)
    }
}