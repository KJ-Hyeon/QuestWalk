package com.hapataka.questwalk.ui.login

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuthException
import com.hapataka.questwalk.R
import com.hapataka.questwalk.data.firebase.repository.AuthRepositoryImpl
import com.hapataka.questwalk.databinding.FragmentLogInBinding
import com.hapataka.questwalk.util.BaseFragment
import com.hapataka.questwalk.util.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class LogInFragment : BaseFragment<FragmentLogInBinding>(FragmentLogInBinding::inflate) {
    private val authRepo by lazy { AuthRepositoryImpl() }
    private val navController by lazy { (parentFragment as NavHostFragment).findNavController() }
    private var backPressedOnce = false
    private val viewModel: LoginViewModel by viewModels { ViewModelFactory(requireContext()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.fade)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserId()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setup()
    }

    private fun initView() {
        initLoginButton()
        initSignUpButton()
        initFindPassWordButton()
    }

    private fun initFindPassWordButton() {
        binding.tvFindPassWord.setOnClickListener {
            navController.navigate(R.id.action_frag_login_to_findPassWordFragment)
        }
    }

    private fun setup() {
        initBackPressedCallback()
        setObserver()
    }

    private fun initLoginButton() {
        with(binding) {
            btnLogin.setOnClickListener {
                val id = etLoginId.text.toString()
                val pw = etLoginPassword.text.toString()

                loginByEmailPassword(id, pw)
                hideKeyBoard()
            }
        }
    }

    private fun initSignUpButton() {
        binding.btnSignUp.setOnClickListener {
            navController.navigate(R.id.action_frag_login_to_frag_sign_up)
        }
    }

    private fun loginByEmailPassword(id: String, pw: String) {
        if (id.isEmpty() || pw.isEmpty()) {
            "이메일 또는 비밀번호가 비어있습니다".showSnackbar(requireView())
            return
        }

        lifecycleScope.launch {
            authRepo.loginByEmailAndPw(id, pw) { task ->
                if (task.isSuccessful) {
                    val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

                    viewModel.setUserId(id)
                    navController.navigate(R.id.action_frag_login_to_frag_home)
                    navGraph.setStartDestination(R.id.frag_home)
                    navController.graph = navGraph
                    return@loginByEmailAndPw
                }
                val exception = task.exception

                if (exception is FirebaseAuthException) {
                    handleFirebaseAuthException(exception)
                } else ("로그인 정보를 다시 확인해 주세요.").showSnackbar(requireView())
            }
        }
    }

    private fun handleFirebaseAuthException(exception: FirebaseAuthException) {
        val errorCode = exception.errorCode
        Log.d("로그디", errorCode)
        val errorMessage = getErrorMessageByErrorCode(errorCode)
        errorMessage.showSnackbar(requireView())
    }

    private fun getErrorMessageByErrorCode(errorCode: String): String {
        return when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "이메일 주소가 유효하지 않습니다."
            "ERROR_USER_NOT_FOUND" -> "계정을 찾을 수 없습니다. 가입되지 않은 이메일입니다."
            "ERROR_WRONG_PASSWORD" -> "비밀번호가 틀렸습니다. 다시 확인해주세요."
            "ERROR_USER_DISABLED" -> "이 계정은 비활성화되었습니다. 관리자에게 문의해주세요."
            "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" -> "이미 다른 인증 방법으로 등록된 이메일입니다."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "이 이메일은 이미 사용 중입니다. 다른 이메일을 사용해주세요."
            "ERROR_CREDENTIAL_ALREADY_IN_USE" -> "이 인증 정보는 이미 다른 계정에서 사용 중입니다."
            "ERROR_OPERATION_NOT_ALLOWED" -> "이메일 및 비밀번호 로그인이 활성화되지 않았습니다."
            "ERROR_TOO_MANY_REQUESTS" -> "요청이 너무 많습니다. 나중에 다시 시도해주세요."
            "ERROR_INVALID_CREDENTIAL" -> "아이디 또는 비밀번호가 유효하지 않습니다.\n 다시 시도해 주세요"
            else -> "로그인 실패: 알 수 없는 오류가 발생했습니다. 다시 시도해주세요."
        }
    }

    private fun initBackPressedCallback() {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressedOnce) {
                    requireActivity().finish()
                    return
                }
                backPressedOnce = true
                Toast.makeText(requireContext(), "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                    .show()
                lifecycleScope.launch {
                    delay(2000)
                    backPressedOnce = false
                }
            }
        }.also {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, it)
        }
    }

    private fun hideKeyBoard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    private fun setObserver() {
        viewModel.userId.observe(viewLifecycleOwner) {id ->
            binding.etLoginId.setText(id)
        }
    }
}

fun String.showSnackbar(view: View) {
    Snackbar.make(view, this, Snackbar.LENGTH_SHORT).show()
}