package com.example.sharlam

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class AddGroupActivity : AppCompatActivity() {

    var storage : FirebaseStorage? = null
    var firestore : FirebaseFirestore? = null

    val ADD_GROUP_ADD_FRIEND_REQUEST_CODE = 104
    val ADD_PHOTO_IMAGE_REQUEST_CODE = 105
    var uuid : Array<String> = arrayOf()
    var photoUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()

        findViewById<ImageView>(R.id.Addgroup_activity_add_group_friend_btn).setOnClickListener {
            val intent = Intent(this,AddGroupAddFriends::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

            startActivityForResult(intent, ADD_GROUP_ADD_FRIEND_REQUEST_CODE)
        }
        findViewById<ImageView>(R.id.Addgroup_activity_add_group_btn).setOnClickListener{
            if(uuid.size<2) Toast.makeText(this,"친구가 1명 이상 선택되어야 그룹이 활성화 됩니다.",Toast.LENGTH_LONG).show()
            else {
                if(findViewById<EditText>(R.id.Addgroup_activity_add_group_name).text.toString() == "") Toast.makeText(this,"그룹 명을 입력해 주셔야 합니다.",Toast.LENGTH_LONG).show()
                else {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                    val imageFileName = "IMAGE_" + timestamp + "_.png"

                    val storageRef = storage?.reference?.child("images")?.child(imageFileName)
                    if(photoUri == null){
                        val resultIntent = Intent()
                        resultIntent.putExtra("KTargetNums", uuid)
                        resultIntent.putExtra("GroupName", findViewById<EditText>(R.id.Addgroup_activity_add_group_name).text.toString())
                        resultIntent.putExtra("uri","")
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }else {
                        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
                            storageRef.downloadUrl.addOnSuccessListener { uri ->
                                val resultIntent = Intent()
                                resultIntent.putExtra("KTargetNums", uuid)
                                resultIntent.putExtra("GroupName", findViewById<EditText>(R.id.Addgroup_activity_add_group_name).text.toString())
                                resultIntent.putExtra("uri", uri.toString())
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            }
                        }
                    }
                }
            }
        }

        findViewById<ImageView>(R.id.addphoto_image).setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent,ADD_PHOTO_IMAGE_REQUEST_CODE)
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0,0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==ADD_GROUP_ADD_FRIEND_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                uuid = data!!.getSerializableExtra("KTargetNums") as Array<String>
            }
        }
        if(requestCode == ADD_PHOTO_IMAGE_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                photoUri = data?.data
                findViewById<ImageView>(R.id.addphoto_image).setImageURI(photoUri)
            }
        }
    }
}