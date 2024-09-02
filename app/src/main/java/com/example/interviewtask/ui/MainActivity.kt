package com.example.interviewtask.ui

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.interviewtask.R
import com.example.interviewtask.model.UserResponse
import com.example.interviewtask.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<UserViewModel>()
    private lateinit var userAdapter: UserAdapter
    private var selectedImageUri: Uri? = null
    private lateinit var ivProfileImage: ImageView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Permission launcher to request camera and storage permissions
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraPermissionGranted = permissions[Manifest.permission.CAMERA] ?: false
        val storagePermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

        if (cameraPermissionGranted && storagePermissionGranted) {
            openCamera() // Open camera after permission is granted
        } else {
            Toast.makeText(this, "Camera and storage permissions are required", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher to request location permission
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupRecyclerView()

        // Observe ViewModel for user list updates
        viewModel.userList.observe(this) { users ->
            userAdapter.updateData(users)
        }

        // Load data from SQLite when the app starts
        viewModel.loadAllUsers()

        // Show dialog to add user
        binding.btnAddUser.setOnClickListener {
            showAddUserDialog()
        }

        // Fetch data from API and save it to SQLite
        binding.btnLogin.setOnClickListener {
            viewModel.showUser(2)
        }

        // Request location permission and get current location
        requestLocationPermission()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList())
        binding.rvSetdata.layoutManager = LinearLayoutManager(this)
        binding.rvSetdata.adapter = userAdapter
    }

    private fun showAddUserDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_add_user)

        val etName = dialog.findViewById<EditText>(R.id.etName)
        val etEmail = dialog.findViewById<EditText>(R.id.etEmail)
        ivProfileImage = dialog.findViewById(R.id.ivProfileImage)
        val btnSelectImage = dialog.findViewById<Button>(R.id.btnSelectImage)
        val btnCaptureImage = dialog.findViewById<Button>(R.id.btnCaptureImage)
        val btnSubmit = dialog.findViewById<Button>(R.id.btnSubmit)

        btnSelectImage.setOnClickListener {
            openGallery()
        }

        btnCaptureImage.setOnClickListener {
            requestPermissions()
        }

        btnSubmit.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()

            if (name.isNotEmpty() && email.isNotEmpty() && selectedImageUri != null) {
                // Use the ViewModel to save the user data to the database
                val newUser = UserResponse.UserData(
                    id = 108, // Assign a unique ID
                    firstName = name,
                    lastName = "",
                    email = email,
                    avatar = selectedImageUri.toString()
                )
                viewModel.addUserToDatabase(newUser)
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Please fill all fields and select an image", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(intent)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraResultLauncher.launch(intent)
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            selectedImageUri = result.data!!.data
            ivProfileImage.setImageURI(selectedImageUri)
        }
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val bitmap = result.data!!.extras?.get("data") as Bitmap
            ivProfileImage.setImageBitmap(bitmap)
            // Save bitmap to URI (optional, for storage purposes)
            selectedImageUri = getImageUriFromBitmap(bitmap)
        }
    }

    private fun requestPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                // Permissions already granted
                openCamera()
            }
            else -> {
                // Request necessary permissions
                permissionLauncher.launch(
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                )
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is granted, get the location
                getCurrentLocation()
            }
            else -> {
                // Request permission
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener { location: Location? ->
            if (location != null) {
                val latLongText = "Lat: ${location.latitude}, Long: ${location.longitude}"
                binding.tvLatLong.text = latLongText
            } else {
                binding.tvLatLong.text = "Unable to get location"
            }
        })
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
    }
}
