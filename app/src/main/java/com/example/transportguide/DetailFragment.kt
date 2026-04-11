package com.example.transportguide

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Bitmap
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
    private var currentImageUrl: String? = null // Переменная для хранения URL

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

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val file = File(requireContext().cacheDir, "camera_img.jpg")
            file.outputStream().use { it1 -> it.compress(Bitmap.CompressFormat.JPEG, 100, it1) }
            selectedImageFile = file
            view?.findViewById<ImageView>(R.id.ivPreview)?.apply {
                visibility = View.VISIBLE
                setImageBitmap(it)
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
        val btnDelete = view.findViewById<Button>(R.id.btnDelete)

        view.findViewById<Button>(R.id.btnTakePhoto).setOnClickListener { takePhoto.launch(null) }
        view.findViewById<Button>(R.id.btnPickImage).setOnClickListener { pickImage.launch("image/*") }

        // ОБНОВЛЕННЫЙ ШАРИНГ (отправляет все данные)
        view.findViewById<Button>(R.id.btnShare).setOnClickListener {
            val number = etNum.text.toString()
            val desc = etDesc.text.toString()
            val date = etDate.text.toString()
            val imageInfo = if (!currentImageUrl.isNullOrEmpty()) "\nФото: $currentImageUrl" else ""

            val fullMessage = "Маршрут №$number\nОписание: $desc\nДата: $date$imageInfo"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, fullMessage)
            }
            startActivity(Intent.createChooser(shareIntent, "Поделиться маршрутом через"))
        }

        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, d ->
                etDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%d", d, m + 1, y))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        val routeId = arguments?.getInt("routeId") ?: -1
        currentImageUrl = arguments?.getString("imageUrl") // Получаем URL из аргументов

        if (routeId != -1) {
            etNum.setText(arguments?.getString("number"))
            etDesc.setText(arguments?.getString("desc"))
            etDate.setText(arguments?.getString("date"))
            if (!currentImageUrl.isNullOrEmpty()) {
                ivPreview.visibility = View.VISIBLE
                ivPreview.load(currentImageUrl)
            }
            btnSave.text = "Обновить"
            btnDelete.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            val num = etNum.text.toString()
            val desc = etDesc.text.toString()
            val date = etDate.text.toString()

            if (num.isEmpty()) {
                etNum.error = "Введите номер!"
                return@setOnClickListener
            }

            btnSave.isEnabled = false
            btnSave.text = "Загрузка..."

            if (selectedImageFile != null) {
                ImageUploader.uploadImage(selectedImageFile!!) { newUrl ->
                    activity?.runOnUiThread {
                        currentImageUrl = newUrl ?: currentImageUrl
                        saveFinal(routeId, num, desc, date, currentImageUrl)
                    }
                }
            } else {
                saveFinal(routeId, num, desc, date, currentImageUrl)
            }
        }

        btnDelete.setOnClickListener {
            lifecycleScope.launch {
                db.routeDao().delete(Route(id = routeId, number = "", description = "", date = ""))
                findNavController().popBackStack()
            }
        }
    }

    private fun saveFinal(id: Int, num: String, desc: String, date: String, url: String?) {
        val route = Route(if (id == -1) 0 else id, num, desc, date, url)
        lifecycleScope.launch {
            if (id == -1) db.routeDao().insert(route) else db.routeDao().update(route)

            // Отправка в облако
            val cloudData = hashMapOf(
                "number" to num,
                "description" to desc,
                "date" to date,
                "imageUrl" to (url ?: "")
            )
            firestore.collection("routes").document(num).set(cloudData) // Используем номер как ID документа, чтобы не было дублей в Firebase

            findNavController().popBackStack()
        }
    }
}