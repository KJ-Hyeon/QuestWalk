package com.hapataka.questwalk.ui.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.media.MediaActionSound
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.hapataka.questwalk.R
import com.hapataka.questwalk.databinding.FragmentCameraBinding
import com.hapataka.questwalk.ui.mainactivity.MainViewModel
import com.hapataka.questwalk.ui.record.TAG
import com.hapataka.questwalk.util.BaseFragment
import com.hapataka.questwalk.util.ViewModelFactory
import com.hapataka.questwalk.util.extentions.gone
import com.hapataka.questwalk.util.extentions.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate) {
    companion object {
        private var TO_HOME_FRAG = "homefragment"
        private var TO_CAPT_FRAG = "capturefragment"
    }


    private val navController by lazy { (parentFragment as NavHostFragment).findNavController() }
    private val mainViewModel: MainViewModel by activityViewModels { ViewModelFactory() }
    private val cameraViewModel: CameraViewModel by activityViewModels()

    private lateinit var cameraHandler: CameraHandler
    private var isComingFromSettings = false

    private var toFrag = TO_HOME_FRAG

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::handlePermissionResult
        )

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraHandler = CameraHandler(requireContext(), this, binding.pvPreview)
        checkPermission()
        setObserver()
        initViews()
    }

    override fun onResume() {
        super.onResume()
        if (isComingFromSettings) {
            checkPermission()
            isComingFromSettings = false
        }
    }

    private fun checkPermission() {
        val cameraPermission = Manifest.permission.CAMERA
        val checkCameraPermission =
            checkSelfPermission(requireContext(), cameraPermission) == PERMISSION_GRANTED

        if (checkCameraPermission) {
            cameraHandler.initCamera()
            return
        }
        requestPermissionLauncher.launch(cameraPermission)
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            cameraHandler.initCamera()
            return
        }

        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Snackbar.make(requireView(), "카메라를 사용하기 위해서는 권한이 필요합니다.", Snackbar.LENGTH_SHORT)
                .setAction("확인") {
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
                .show()
        } else {
            Snackbar.make(requireView(), "권한 받아 오기 실패", Snackbar.LENGTH_SHORT)
                .setAction("권한 설정") {
                    isComingFromSettings = true
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", requireActivity().packageName, null)
                    }
                    startActivity(intent)
                }
                .show()
        }
    }

    private fun setObserver() {
        with(mainViewModel) {
            imageBitmap.observe(viewLifecycleOwner) {
                Log.d(TAG, "bitmap: $it")
                with(binding.ivCapturedImage) {
                    load(it)
                    visible()
                }
            }

        }
        cameraViewModel.bitmap.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            if (toFrag == TO_CAPT_FRAG) {
                toFrag = TO_HOME_FRAG
                navController.navigate(R.id.action_frag_camera_to_frag_capture)
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initViews() {
        flashImageSet()
        initCaptureButton()
        binding.ivCapturedImage.gone()
        with(binding) {

            btnFlash.setOnClickListener {
                cameraHandler.toggleFlash()
            }

            btnBack.setOnClickListener {
                navController.popBackStack()
            }
            tvCameraQuest.text = mainViewModel.currentKeyword.value
            tvCameraQuest.setOnClickListener {
                cameraHandler.capturePhoto(imageCaptureCallback())
                toFrag = TO_CAPT_FRAG
            }
        }
    }

    private fun flashImageSet() {
        cameraHandler.flashModeChanged = {flashMode ->
            val flashIcon = if (flashMode == ImageCapture.FLASH_MODE_ON) {
                R.drawable.btn_flash_on
            } else{
                R.drawable.btn_flash
            }
            binding.ivFlash.setImageResource(flashIcon)
        }
    }

    private fun initCaptureButton() {
        val mediaActionSound = MediaActionSound()

        binding.btnCapture.apply {
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        this.load(R.drawable.btn_capture_click)
                        mediaActionSound.play(MediaActionSound.SHUTTER_CLICK)
                        cameraHandler.capturePhoto(imageCaptureCallback())
                    }

                    MotionEvent.ACTION_UP -> {
                        this.load(R.drawable.btn_capture)
                        v.performClick()
                    }
                }
                true
            }
        }

    }


    private fun imageCaptureCallback(): ImageCapture.OnImageCapturedCallback {
        return object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {

                if (toFrag == TO_CAPT_FRAG) {
                    cameraViewModel.calculateAcc(binding.pvPreview.width,binding.pvPreview.height,image)
                    cameraViewModel.imageProxyToBitmap(image)

                } else {
                    mainViewModel.setCaptureImage(
                        image,
                        { navController.popBackStack() },
                    ) {
                        with(binding.ivCapturedImage) {
                            load(it)
                            visible()
                        }
                    }
                }
                image.close()

            }
        }
    }

}