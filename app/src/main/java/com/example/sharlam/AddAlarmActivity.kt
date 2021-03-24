package com.example.sharlam

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.sharlam.navigation.model.AlarmDTO
import com.example.sharlam.navigation.model.SAlarmDatabase
import com.example.sharlam.navigation.model.SAlarmEntitiy
import com.example.sharlam.navigation.model.UserDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.experimental.xor

class AddAlarmActivity : AppCompatActivity() {

    var storage : FirebaseStorage? = null
    var firestore : FirebaseFirestore? = null

    lateinit var tp : TimePicker
    lateinit var et : EditText

    val ADD_FRIENDS_REQUEST_CODE : Int = 101

    var contactNums : Array<String> = arrayOf()
    var kcontactNums : Array<String> = arrayOf()

    var TargetDays : Byte = 0

    var days_btns : MutableList<ImageView> = mutableListOf() // For the Mon, Tue, Wed.. Days Btn functions

    lateinit var db : SAlarmDatabase

    var logged : Boolean = false // Btn and Background is changed depending on log in state

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)

        //Initiate storage
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        db = SAlarmDatabase.getInstance(this)!!

        tp = findViewById(R.id.addalarm_time_picker)
        et = findViewById(R.id.addalarm_activity_alarm_title)

        this.findViewById<ImageView>(R.id.addalarm_activity_complete_addalarm_btn).setOnClickListener {
            alaramUpload()
        }

        //Set on ClickListener to Days
        days_btns.add(findViewById(R.id.days_picker_monday))
        days_btns.add(findViewById(R.id.days_picker_tuesday))
        days_btns.add(findViewById(R.id.days_picker_wednesday))
        days_btns.add(findViewById(R.id.days_picker_thursday))
        days_btns.add(findViewById(R.id.days_picker_friday))
        days_btns.add(findViewById(R.id.days_picker_saturday))
        days_btns.add(findViewById(R.id.days_picker_sunday))
        days_btns[0].setOnClickListener { changedays(0, ) }
        days_btns[1].setOnClickListener { changedays(1) }
        days_btns[2].setOnClickListener { changedays(2) }
        days_btns[3].setOnClickListener { changedays(3) }
        days_btns[4].setOnClickListener { changedays(4) }
        days_btns[5].setOnClickListener { changedays(5) }
        days_btns[6].setOnClickListener { changedays(6) }


        //Get Kakao Login state
        //Only Use With Kakao Login
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if(error == null && tokenInfo != null){
                logged = true
                this.findViewById<TextView>(R.id.addalarm_activity_kakao_friends_button).text = "친구 추가하기"
                this.findViewById<TextView>(R.id.addalarm_activity_kakao_friends_button).setOnClickListener {
//                    Toast.makeText(this,"로딩중",Toast.LENGTH_SHORT).show()
                    val intent = Intent(this,AddFriendsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
                    startActivityForResult(intent,ADD_FRIENDS_REQUEST_CODE)
                }
            }else{
                logged = false
                this.findViewById<TextView>(R.id.addalarm_activity_kakao_friends_button).text = "카카오 로그인하기"
                this.findViewById<TextView>(R.id.addalarm_activity_kakao_friends_button).setOnClickListener {
                    kakaoLogin(this)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0,0)
    }

    fun changedays(index : Int){
        TargetDays = TargetDays.xor(0x80.ushr(index).toByte())
        //Toast.makeText(this,TargetDays.toString(),Toast.LENGTH_SHORT).show()
        if(TargetDays.and(0x80.ushr(index).toByte())!=0.toByte() ) {
            days_btns[index].setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN);
        }
        else{
            days_btns[index].colorFilter = null
        }

    }

    //KAKAO Login 1
    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {

        }
        else if (token != null) {
            logged = true
            this.findViewById<TextView>(R.id.addalarm_activity_kakao_friends_button).text = "친구 추가하기"
            this.findViewById<TextView>(R.id.addalarm_activity_kakao_friends_button).setOnClickListener {
                //Toast.makeText(this,"로딩중",Toast.LENGTH_SHORT).show()
                val intent = Intent(this,AddFriendsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

                startActivity(intent)
            }
            UserApiClient.instance.me{ user, error ->
                firestore!!.collection("UserIDs").document(user!!.id.toString()).get().addOnSuccessListener { document ->
                    if(!document.exists()){
                        var userDTO = UserDTO()
                        userDTO.accountTimeStamp = System.currentTimeMillis()
                        userDTO.UserID = user!!.id.toString()
                        firestore!!.collection("UserIDs").document(user!!.id.toString()).set(userDTO)
                    }
                }
            }


        }

    }
    //KAKAO Login 2
    fun kakaoLogin(context: Context){
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_FRIENDS_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                contactNums = data!!.getSerializableExtra("TargetNums") as Array<String>
                kcontactNums = data!!.getSerializableExtra("KTargetNums") as Array<String>
            }
        }
    }

    private fun alaramUpload(){
        if(TargetDays==0.toByte()){
            Toast.makeText(this,"무슨 요일에 일어나는지 알려주세요!",Toast.LENGTH_SHORT).show()
            return
        }

        var alarmDTO = AlarmDTO()
        if(et.text.toString()==""){
            alarmDTO.Title = "일어나세요!"
        }else{
            alarmDTO.Title = et.text.toString()
        }
        alarmDTO.SoundUrl = "test"
        alarmDTO.TargetDays = TargetDays.toInt()
        alarmDTO.Targethours = tp.hour
        alarmDTO.Targetminutes = tp.minute
        alarmDTO.Timestamp = System.currentTimeMillis()
        firestore?.collection("SingleAlarms")?.document()?.set(alarmDTO)


        if(et.text.toString()=="") {
            val Salarm = SAlarmEntitiy(System.currentTimeMillis(),"일어나세요","", tp.hour, tp.minute, TargetDays,contactNums,kcontactNums)
            insertAlarm(Salarm)
        }else{
            val Salarm = SAlarmEntitiy(System.currentTimeMillis(), et.text.toString(),"",tp.hour,tp.minute,TargetDays,contactNums,kcontactNums)
            insertAlarm(Salarm)
        }

        setResult(Activity.RESULT_OK)
        finish()
    }



    @SuppressLint("StaticFieldLeak")
    fun insertAlarm(alarm : SAlarmEntitiy){
        val insertTask = object : AsyncTask<Unit, Unit, Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                db.salarmDAO().insert(alarm)
            }
        }
        insertTask.execute()
    }

}