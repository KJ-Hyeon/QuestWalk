package com.hapataka.questwalk.ui.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hapataka.questwalk.R
import com.hapataka.questwalk.databinding.FragmentCameraBinding
import com.hapataka.questwalk.ui.home.HomeViewModel
import com.hapataka.questwalk.util.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Runnable


@AndroidEntryPoint
class CameraFragment : BaseFragment<FragmentCameraBinding>(FragmentCameraBinding::inflate) {
    private val navController by lazy { (parentFragment as NavHostFragment).findNavController() }
    private val cameraViewModel: CameraViewModel by activityViewModels()
    private val homeViewModel : HomeViewModel by activityViewModels()
    // 카메라 관련 변수
    private var imageCapture: ImageCapture? = null
    private var flashMode = ImageCapture.FLASH_MODE_OFF

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()
        setObserver()
        bindingWidgets()
    }

    /**
     *  Camera Permission 등록
     */

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            ::handlePermissionResult
        )

    private fun checkPermissions() {
        if (
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            startCamera()
        } else {
            Snackbar.make(requireView(), "권한 받아 오기 실패", Snackbar.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }

    /**
     *  Observer 및 ViewBinding
     */

    private fun setObserver() {

        cameraViewModel.bitmap.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            //캡쳐 성공시 CaptureFragment 이동
            navController.navigate(R.id.action_frag_camera_to_frag_capture)
        }
    }

    private fun bindingWidgets() {
        with(binding) {
            ibCapture.setOnClickListener {
                capturePhoto()
            }
            ibFlash.setOnClickListener {
                toggleFlash()
            }
            ibBackBtn.setOnClickListener {
                navController.popBackStack()
            }
            tvCameraQuest.text = homeViewModel.currentKeyword.value
        }
    }


    /**
     *  카메라 기능
     */

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
        cameraProviderFuture.addListener(
            setCamera(cameraProvider),
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    private fun setCamera(cameraProvider: ProcessCameraProvider): Runnable = Runnable {
        val preview = Preview.Builder()
            .build()
            .also { mPreview ->
                mPreview.setSurfaceProvider(
                    binding.pvPreview.surfaceProvider
                )
            }

        imageCapture = ImageCapture.Builder()
            .setTargetResolution(getLargestSize())
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
        } catch (e: Exception) {
            Log.d("CameraX", "startCamera Fail", e)
        }
    }
    //실패 했을떄, imageUri 이 null 으로 반환 해야 한다.

    private fun getLargestSize(): Size {
        val cameraManager =
            requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // 사용할 카메라 ID를 선택
        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
        val configurations =
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        return configurations?.getOutputSizes(ImageFormat.JPEG)
            ?.maxByOrNull { it.height * it.width } ?: Size(4000, 4000)
    }

    private fun capturePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            imageCaptureCallback()
        )
    }

    private fun imageCaptureCallback(): ImageCapture.OnImageCapturedCallback {
        return object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val buffer = image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                cameraViewModel.setCameraCharacteristics(image.imageInfo.rotationDegrees.toFloat())
                cameraViewModel.setBitmap(bitmap)
                image.close()
            }
        }
    }


    private fun toggleFlash() {

        flashMode = if (flashMode == ImageCapture.FLASH_MODE_OFF) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
        imageCapture?.flashMode = flashMode
    }
}