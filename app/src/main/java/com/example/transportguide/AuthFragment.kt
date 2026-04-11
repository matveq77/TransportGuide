package com.example.transportguide

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (auth.currentUser != null) navigateToMain()

        val emailEt = view.findViewById<EditText>(R.id.etEmail)
        val passEt = view.findViewById<EditText>(R.id.etPassword)

        view.findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = emailEt.text.toString()
            val pass = passEt.text.toString()

            if (email.isNotEmpty() && pass.length > 5) {
                auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) navigateToMain()
                    else {
                        auth.createUserWithEmailAndPassword(email, pass)
                            .addOnSuccessListener { navigateToMain() }
                            .addOnFailureListener { Toast.makeText(context, "Ошибка входа/регистрации", Toast.LENGTH_SHORT).show() }
                    }
                }
            } else {
                Toast.makeText(context, "Заполните данные (пароль мин. 6 симв.)", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(R.id.action_authFragment_to_mainFragment)
    }
}