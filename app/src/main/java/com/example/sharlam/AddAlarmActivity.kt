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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient

class AddAlarmActivity : AppCompatActivity() {

    var storage : FirebaseStorage? = null
    var firestore : FirebaseFirestore? = null
    var TargetDays : MutableList<Boolean> = MutableList<Boolean>(7,{false})

    var TargetD : Array<Boolean> = Array(7,{false})
    var days : MutableList<ImageView> = mutableListOf()
    var timepicker : TimePicker? = null
    var Btn_Add_Alarm : ImageView? = null
    var edittext : EditText?= null

    var TargetNums : Array<String>? = arrayOf()

    lateinit var db : SAlarmDatabase
    var alarmList : List<SAlarmEntitiy> = listOf<SAlarmEntitiy>()


    var logged : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_alarm)
        db = SAlarmDatabase.getInstance(this)!!
        //Initiate storage
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()
        timepicker = findViewById(R.id.time_picker)
        Btn_Add_Alarm = findViewById(R.id.btn_complete_add_alarm)
        edittext = findViewById(R.id.alarm_title)
        Btn_Add_Alarm?.setOnClickListener {
            alaramUpload()
        }
        days.add(findViewById(R.id.days_picker_monday))
        days[0].setOnClickListener { changedays(0) }
        days.add(findViewById(R.id.days_picker_tuesday))
        days[1].setOnClickListener { changedays(1) }
        days.add(findViewById(R.id.days_picker_wednesday))
        days[2].setOnClickListener { changedays(2) }
        days.add(findViewById(R.id.days_picker_thursday))
        days[3].setOnClickListener { changedays(3) }
        days.add(findViewById(R.id.days_picker_friday))
        days[4].setOnClickListener { changedays(4) }
        days.add(findViewById(R.id.days_picker_saturday))
        days[5].setOnClickListener { changedays(5) }
        days.add(findViewById(R.id.days_picker_sunday))
        days[6].setOnClickListener { changedays(6) }


        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if(error == null && tokenInfo != null){
                logged = true
                this.findViewById<TextView>(R.id.kakao_friends_button).text = "친구 추가하기"
                this.findViewById<TextView>(R.id.kakao_friends_button).setOnClickListener {
//                    Toast.makeText(this,"로딩중",Toast.LENGTH_SHORT).show()
                    startActivityForResult(Intent(this, AddFriendsActivity::class.java),1)
                }
            }else{
                logged = false
                this.findViewById<TextView>(R.id.kakao_friends_button).text = "카카오 로그인하기"
                this.findViewById<TextView>(R.id.kakao_friends_button).setOnClickListener {
                    kakaoLogin(this)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                TargetNums = data!!.getSerializableExtra("TargetNums") as Array<String>
            }
        }
    }
    fun alaramUpload(){
        var alarmDTO = AlarmDTO()
        if(edittext?.text.toString()==""){
            alarmDTO.Title = "일어나세요!"
        }else{
            alarmDTO.Title = edittext?.text.toString()
        }
        alarmDTO.SoundUrl = "test"
        alarmDTO.TargetDays = TargetDays
        alarmDTO.Targethours = timepicker?.hour
        alarmDTO.Targetminutes = timepicker?.minute
        alarmDTO.Timestamp = System.currentTimeMillis()
        firestore?.collection("SingleAlarms")?.document()?.set(alarmDTO)

        TargetD[0] = TargetDays[0]
        TargetD[1] = TargetDays[1]
        TargetD[2] = TargetDays[2]
        TargetD[3] = TargetDays[3]
        TargetD[4] = TargetDays[4]
        TargetD[5] = TargetDays[5]
        TargetD[6] = TargetDays[6]

        if(edittext?.text.toString()=="") {
            val Salarm = SAlarmEntitiy(System.currentTimeMillis(),"일어나세요","",timepicker?.hour,timepicker?.minute,TargetD,TargetNums)
            insertAlarm(Salarm)
        }else{
            val Salarm = SAlarmEntitiy(System.currentTimeMillis(),edittext?.text.toString(),"",timepicker?.hour,timepicker?.minute,TargetD,TargetNums)
            insertAlarm(Salarm)
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    fun changedays(index : Int){
        if(TargetDays[index]==false) {
            days[index].setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN);
        }
        else{
            days[index].setColorFilter(null)
        }
        TargetDays[index] = !TargetDays[index]
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

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {

        }
        else if (token != null) {
            logged = true
            this.findViewById<TextView>(R.id.kakao_friends_button).text = "친구 추가하기"
            this.findViewById<TextView>(R.id.kakao_friends_button).setOnClickListener {
                Toast.makeText(this,"로딩중",Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, AddFriendsActivity::class.java))
            }
        }

    }


    fun kakaoLogin(context: Context){
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            TODO("Not yet implemented")
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            TODO("Not yet implemented")
        }

        override fun getItemCount(): Int {
            TODO("Not yet implemented")
        }

    }
}