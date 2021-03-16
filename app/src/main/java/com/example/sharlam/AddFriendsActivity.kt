package com.example.sharlam

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharlam.navigation.model.PhoneBook
import com.kakao.sdk.talk.TalkApiClient
import java.security.Permission
import java.security.Permissions


class AddFriendsActivity : AppCompatActivity() {
    val PERMISSIONS_REQUEST_READ_CONTACTS : Int = 100
    val PERMISSIONS_REQUEST_WRITE_CONTACTS : Int = 101


    var thumbnails : MutableList<String?> = arrayListOf()
    var nicknames : MutableList<String> = arrayListOf()
    var PhoneBooks : MutableList<PhoneBook> = arrayListOf()
    var friends_size : Int = 0
    var checkedList : MutableList<Boolean>? = null
    var outlist : Array<String> = arrayOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_friends)


        callPermission()
    }

    @SuppressLint("CutPasteId")
    fun Activitys(){
        PhoneBooks = getContacts(this)
        checkedList = MutableList(PhoneBooks.size) { false }

        Toast.makeText(this, PhoneBooks.size.toString(), Toast.LENGTH_SHORT).show()
        TalkApiClient.instance.friends { friends, error ->
            if (error != null) {
                Toast.makeText(this, "친구 목록 불러오기 실패: ${error}", Toast.LENGTH_LONG).show()
                finish()
            } else {
                friends_size = friends!!.totalCount
                for (friend in friends.elements) {
                    thumbnails.add(friend.profileThumbnailImage)
                    nicknames.add(friend.profileNickname)
                }

            }
        }
        findViewById<RecyclerView>(R.id.detailview_add_kfriends).adapter = DetailViewRecyclerViewAdapter()
        findViewById<RecyclerView>(R.id.detailview_add_kfriends).layoutManager = LinearLayoutManager(this)

        findViewById<ImageView>(R.id.add_member_btn).setOnClickListener {
            sendSMS()

            val resultIntent = Intent()
            resultIntent.putExtra("TargetNums", outlist)
            setResult(RESULT_OK, resultIntent)

            finish()
        }
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.kfriends_detail, parent, false)
            return CustomViewHolder(view)
        }


        inner class CustomViewHolder(view: View?) : RecyclerView.ViewHolder(view!!){

            var checkbox : CheckBox = view!!.findViewById(R.id.kfriends_checkbox)
            var textview : TextView = view!!.findViewById(R.id.kfriends_name)

            fun bind(data1 : MutableList<PhoneBook>, data2 : MutableList<Boolean>,num : Int) {
                textview.text = data1[num].name
                checkbox.isChecked = data2[num]
                checkbox.setOnClickListener {
                    checkedList!![num] = checkbox.isChecked
                }
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as CustomViewHolder).bind(PhoneBooks,checkedList!!,position)
        }

        override fun getItemCount(): Int {
            return PhoneBooks.size
        }

    }

    
    fun sendSMS(){
        for(position in 0 until checkedList!!.size){
            if(checkedList!![position]==true){
                outlist = outlist.plus(PhoneBooks[position].number!!)
//                try {
//                    A += 1
//                    //전송
//                    val smsManager: SmsManager = SmsManager.getDefault()
//                    smsManager.sendTextMessage(PhoneBooks[position].number, null, "확인용", null, null)
//                    Toast.makeText(applicationContext, "전송 완료!", Toast.LENGTH_LONG).show()
//                } catch (e: Exception) {
//                    Toast.makeText(applicationContext, "SMS faild, please try again later!", Toast.LENGTH_LONG).show()
//                    e.printStackTrace()
//                }
            }
        }
    }
    fun callPermission(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            Activitys()
            return
        }

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_CONTACTS),PERMISSIONS_REQUEST_READ_CONTACTS)
        else Activitys()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == PERMISSIONS_REQUEST_READ_CONTACTS){
            var check = true
            for(grant in grantResults){
                if(grant != PackageManager.PERMISSION_GRANTED){
                    check = false
                    break
                }
            }
            if(!check){
                Toast.makeText(this,"권한필요",Toast.LENGTH_SHORT).show()
                finish()
                return
            }else{
                Activitys()
            }
        }
    }

    fun getContacts(context: Context) : MutableList<PhoneBook>{
        var dates : MutableList<PhoneBook> = mutableListOf()

        var resolver : ContentResolver = context.contentResolver

        var phoneUri : Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI

        var cursor : Cursor? = resolver.query(phoneUri, arrayOf(ContactsContract.CommonDataKinds.Phone.CONTACT_ID // 인덱스 값, 중복될 수 있음 -- 한 사람 번호가 여러개인 경우
                , ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER), null, null, null)

        if(cursor != null)
        {
            while(cursor.moveToNext()){
                var idIndex : Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                var nameIndex : Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                var numberIndex : Int = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                var id : String = cursor.getString(idIndex)
                var name : String = cursor.getString(nameIndex)
                var number : String = cursor.getString(numberIndex)

                var phonebook : PhoneBook = PhoneBook(id, name, number)

                dates.add(phonebook)
            }
            cursor.close()
        }
        return dates
    }


}