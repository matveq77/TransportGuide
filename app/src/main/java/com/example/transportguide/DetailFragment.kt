package com.example.transportguide

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.transportguide.data.*
import com.example.transportguide.utils.ImageUploader
import com.google.firebase.firestore.FirebaseFirestore
import coil.load
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var selectedImageFile: File? = null
    private lateinit var db: AppDatabase
    private val firestore = FirebaseFirestore.getInstance()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val file = File(requireContext().cacheDir, "temp_img.jpg")
            requireContext().contentResolver.openInputStream(it)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            selectedImageFile = file
            view?.findViewById<ImageView>(R.id.ivPreview)?.apply {
                visibility = View.VISIBLE
                load(it)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = AppDatabase.getDatabase(requireContext())

        val etNum = view.findViewById<EditText>(R.id.etNumber)
        val etDesc = view.findViewById<EditText>(R.id.etDesc)
        val etDate = view.findViewById<EditText>(R.id.etDate)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val ivPreview = view.findViewById<ImageView>(R.id.ivPreview)

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%d", d, m + 1, y))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        view.findViewById<Button>(R.id.btnPickImage).setOnClickListener { pickImage.launch("image/*") }

        val routeId = arguments?.getInt("routeId") ?: -1
        val oldUrl = arguments?.getString("imageUrl")

        if (routeId != -1) {
            etNum.setText(arguments?.getString("number"))
            etDesc.setText(arguments?.getString("desc"))
            etDate.setText(arguments?.getString("date"))
            if (!oldUrl.isNullOrEmpty()) {
                ivPreview.visibility = View.VISIBLE
                ivPreview.load(oldUrl)
            }
            btnSave.text = getString(R.string.update)
            view.findViewById<Button>(R.id.btnDelete).visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            val num = etNum.text.toString()
            val desc = etDesc.text.toString()
            val date = etDate.text.toString()

            if (num.isEmpty()) {
                etNum.error = getString(R.string.error_empty_number)
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Uploading..."

            if (selectedImageFile != null) {
                // 1. Сначала грузим фото на сервер
                ImageUploader.uploadImage(selectedImageFile!!) { newUrl ->
                    activity?.runOnUiThread {
                        // 2. Когда получили URL (или null при ошибке), сохраняем в БД
                        if (newUrl != null) {
                            saveFinal(routeId, num, desc, date, newUrl)
                        } else {
                            // Если ошибка загрузки, используем старый URL или ничего
                            Toast.makeText(requireContext(), "Photo upload failed!", Toast.LENGTH_SHORT).show()
                            saveFinal(routeId, num, desc, date, oldUrl)
                        }
                    }
                }
            } else {
                // Если новое фото не выбрано, просто сохраняем со старым URL
                saveFinal(routeId, num, desc, date, oldUrl)
            }
        }

        view.findViewById<Button>(R.id.btnDelete).setOnClickListener {
            lifecycleScope.launch {
                db.routeDao().delete(Route(id = routeId, number = "", description = "", date = ""))
                findNavController().popBackStack()
            }
        }
    }

    private fun saveFinal(id: Int, num: String, desc: String, date: String, url: String?) {
        // Если id == -1, Room создаст новый ID (передаем 0), иначе используем текущий
        val route = Route(if (id == -1) 0 else id, num, desc, date, url)

        lifecycleScope.launch {
            if (id == -1) {
                db.routeDao().insert(route)
            } else {
                db.routeDao().update(route)
            }

            // Отправка в Firebase (url теперь точно не пустой, если загрузка прошла)
            val cloudData = hashMapOf(
                "number" to num,
                "description" to desc,
                "date" to date,
                "imageUrl" to (url ?: "")
            )
            firestore.collection("routes").add(cloudData)

            if (isAdded) findNavController().popBackStack()
        }
    }
}