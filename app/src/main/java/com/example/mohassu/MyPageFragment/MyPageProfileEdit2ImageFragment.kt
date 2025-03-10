package com.example.mohassu.MyPageFragment

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.mohassu.R
import com.example.mohassu.databinding.FragmentMypageProfileEdit2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class MyPageProfileEdit2ImageFragment : Fragment() {

    companion object {
        private const val TAG = "mohassu:mypage_edit_profile"
    }

    private var _binding: FragmentMypageProfileEdit2Binding? = null
    private val binding get() = _binding!!

    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore
    private var selectedImageUri: Uri? = null
    private var circularBitmapToUpload: Bitmap? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMypageProfileEdit2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()
        binding.btnBack.setOnClickListener { navController.navigateUp() }

        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        val sharedPreferences = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val photoUrl = sharedPreferences.getString("photoUrl", "") ?: ""

        if (photoUrl.isNotEmpty()) {
            Glide.with(this)
                .load(Uri.parse(photoUrl))
                .circleCrop()
                .placeholder(R.drawable.img_basic_profile)
                .error(R.drawable.img_basic_profile)
                .into(binding.profileImage)
        } else {
            binding.profileImage.setImageResource(R.drawable.img_basic_profile)
        }

        binding.editProfileButton.setOnClickListener { openImagePicker() }
        binding.btnSave.setOnClickListener {
            if (circularBitmapToUpload != null) {
                uploadCircularImage(navController)
            } else {
                Toast.makeText(requireContext(), "프로필을 변경하지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, it)
                    circularBitmapToUpload = getCircularBitmap(bitmap)
                    binding.profileImage.setImageBitmap(circularBitmapToUpload)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "이미지를 로드할 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun uploadCircularImage(navController: NavController) {
        val circularBitmap = circularBitmapToUpload ?: return
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef: StorageReference = storage.reference.child("profilePictures/${UUID.randomUUID()}")

        val baos = ByteArrayOutputStream()
        circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val photoUrl = uri.toString()
                    savePhotoUrlToFirestore(userId, photoUrl, navController)
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "다운로드 URL 가져오기 실패", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun savePhotoUrlToFirestore(userId: String, photoUrl: String, navController: NavController) {
        val updates = mapOf("photoUrl" to photoUrl)

        db.collection("users").document(userId)
            .set(updates, SetOptions.merge())
            .addOnSuccessListener {
                requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit()
                    .putString("photoUrl", photoUrl)
                    .apply()
                Toast.makeText(requireContext(), "프로필 사진이 업데이트되었습니다.", Toast.LENGTH_SHORT).show()
                navController.navigate(R.id.actionSaveProfile)
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Firestore 저장 실패", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val min = minOf(bitmap.width, bitmap.height)
        val output = Bitmap.createBitmap(min, min, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, min, min)

        canvas.drawCircle(min / 2f, min / 2f, min / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}