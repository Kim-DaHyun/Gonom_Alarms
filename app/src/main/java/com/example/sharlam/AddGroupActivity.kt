package com.example.sharlam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView

class AddGroupActivity : AppCompatActivity() {

    val ADD_GROUP_ADD_FRIEND_REQUEST_CODE = 104
    var uuid : Array<String> = arrayOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)


        findViewById<TextView>(R.id.Addgroup_activity_add_group_friend_btn).setOnClickListener {
            startActivityForResult(Intent(this, AddGroupAddFriends::class.java), ADD_GROUP_ADD_FRIEND_REQUEST_CODE)
        }
        findViewById<ImageView>(R.id.Addgroup_activity_add_group_btn).setOnClickListener{
            val resultIntent = Intent()
            resultIntent.putExtra("KTargetNums",uuid)
            resultIntent.putExtra("GroupName",findViewById<EditText>(R.id.Addgroup_activity_add_group_name).text.toString())
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==ADD_GROUP_ADD_FRIEND_REQUEST_CODE){
            if(resultCode==RESULT_OK){
                uuid = data!!.getSerializableExtra("KTargetNums") as Array<String>
            }
        }
    }
}