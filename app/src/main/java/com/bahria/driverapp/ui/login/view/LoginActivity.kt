package com.bahria.driverapp.ui.login.view

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bahria.driverapp.R
import com.bahria.driverapp.databinding.ActivityLoginBinding
import com.bahria.driverapp.ui.main.view.MainActivity
import com.bahria.driverapp.ui.login.view_model.LoginViewModel
import com.bahria.driverapp.ui.login.view_model.LoginViewModelFactory
import com.bahria.driverapp.utils.DriverAppApplication
import com.bahria.driverapp.utils.ResponseHandling
import com.bahria.driverapp.utils.SharedPreferenceHandler
import com.bahria.driverapp.utils.Util

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var progressDialog: ProgressDialog
    private var prefs: SharedPreferenceHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        val repository = (application as DriverAppApplication).loginRepository
        loginViewModel =
            ViewModelProvider(this, LoginViewModelFactory(repository))[LoginViewModel::class.java]
        binding.loginViewModel = loginViewModel
        initialize()

        binding.login.setOnClickListener {
            Util.hideKeyboard(this)
            if (checkValidation()) {
                progressDialog.show()
                loginViewModel.onLoginButtonClick()

                loginViewModel.loginResponse.observe(this) {
                    when (it) {
                        is ResponseHandling.Success -> {
                            if (it.data?.Success == "true") {
                                prefs?.setUser(it.data.Result)
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()

                            } else {
                                loginViewModel.flushVariables()
                                Util.showSnackBar(
                                    binding.loginContainer,
                                    getString(R.string.went_wrong)
                                )
                            }
                            progressDialog.dismiss()
                        }

                        is ResponseHandling.Error -> {
                            progressDialog.dismiss()
                            loginViewModel.flushVariables()
                            Util.showSnackBar(binding.loginContainer, it.errorMessage!!)
                        }
                    }
                }
            }
        }
    }

    private fun initialize() {
        progressDialog = Util.initProgressDialog(this)
        prefs = SharedPreferenceHandler(applicationContext)
    }

    private fun checkValidation(): Boolean {
        if (binding.empID.text.isNullOrEmpty()) {
            Util.showSnackBar(
                binding.loginContainer,
                getString(R.string.emp_id_error)
            )
            return false
        }

        if (binding.cnic.text.isNullOrEmpty()) {
            Util.showSnackBar(
                binding.loginContainer,
                getString(R.string.cnic_error)
            )
            return false
        }

        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        loginViewModel.flushVariables()
    }
}