package com.example.sharlam.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.media.Image
import android.os.AsyncTask
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharlam.AddAlarmActivity
import com.example.sharlam.MainActivity
import com.example.sharlam.R
import com.example.sharlam.navigation.model.SAlarmDatabase
import com.example.sharlam.navigation.model.SAlarmEntitiy
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import com.kakao.sdk.template.model.TextTemplate
import org.w3c.dom.Text
import kotlin.experimental.and
import kotlin.experimental.xor

@SuppressLint("StaticFieldLeak")
class SingleViewFragment : Fragment(){

    lateinit var SalarmDB : SAlarmDatabase // Room SAlarm Database
    var alarmList : List<SAlarmEntitiy> = listOf() //SAlarm model list
    var onOffList : MutableList<Boolean> = arrayListOf()
    val ADD_ALARM_REQUEST_CODE : Int = 100

    @SuppressLint("CutPasteId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_single,container,false)
        SalarmDB = SAlarmDatabase.getInstance(container!!.context)!!

        // For Add Alarm Activity
        var Btn_Add_Alarm : ImageView = view.findViewById(R.id.single_fragement_add_alarm_btn)
        Btn_Add_Alarm.setOnClickListener {
            //Start Add Alarm Activity and get result to updating room db
            val intent = Intent(view.context,AddAlarmActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivityForResult(intent,ADD_ALARM_REQUEST_CODE)
        }

        getAllalarms()  // SAlarm DB init

        return view
    }


    // get Salarms in Background thread
    fun getAllalarms(){
        val getTask = (object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                alarmList = SalarmDB.salarmDAO().getAll()
            }
            override fun onPostExecute(result: Unit?) {
                super.onPostExecute(result)
                if(activity==null){
                    return
                }
                setRecyclerView(alarmList)
            }
        }).execute()

    }


    //when Alarm List is empty Background set on, or set off
    @SuppressLint("CutPasteId")
    fun setRecyclerView(alarmList : List<SAlarmEntitiy>){

        onOffList = MutableList<Boolean>(alarmList.size) { false }
        if(alarmList.size>0)
            view?.findViewById<ImageView>(R.id.single_frag_background_image)?.visibility = View.INVISIBLE
        else
            view?.findViewById<ImageView>(R.id.single_frag_background_image)?.visibility = View.VISIBLE

        //RecyclerView init
        view?.findViewById<RecyclerView>(R.id.single_frag_detailview_recyclerview)?.adapter = DetailViewRecylerViewAdapter()
        view?.findViewById<RecyclerView>(R.id.single_frag_detailview_recyclerview)?.layoutManager = LinearLayoutManager(activity)
    }

    //after add alarm, re get alarm list
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                ADD_ALARM_REQUEST_CODE -> {
                    getAllalarms()
                }
            }
        }
    }

    // Send Messaging
    fun sendSMS(Lists : Array<String>, KLists : Array<String>, context : Context){
        var A : String = ""
        for(i in 0 until Lists.size) {
            A+=Lists[i]+"\n"

            try {
                val smsManager: SmsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(Lists[i], null, "확인용", null, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Toast.makeText(context,A,Toast.LENGTH_LONG).show()
        val defaultFeed = FeedTemplate(
                content = Content(
                        title = "나는야 퉁퉁이",
                        description = "#알람 #시발 #하기싫다 #힘드네",
                        imageUrl = "https://firebasestorage.googleapis.com/v0/b/sharlam-ccb46.appspot.com/o/logo_character.png?alt=media&token=fea42b2c-8619-491d-a1cb-205d7c0cb4e3",
                        link = Link(
                                webUrl = "https://developers.kakao.com",
                                mobileWebUrl = "https://developers.kakao.com"
                        )
                )
        )
        for(i in 0 until KLists.size){
            TalkApiClient.instance.sendDefaultMessage(KLists.toList(),defaultFeed){ result, error ->
                if(error != null){
                    Toast.makeText(context,error.toString(),Toast.LENGTH_LONG).show()
                }else{
                    Toast.makeText(context,"굳",Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    //Recyclerview
    inner class DetailViewRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : List<SAlarmEntitiy> = alarmList

        init {

        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view){
            val iv = view.findViewById<ImageView>(R.id.detail_image)
            val rl = view.findViewById<RelativeLayout>(R.id.detail_background)

            fun bind(data1 : List<SAlarmEntitiy>,data2 : Byte,data3 : MutableList<Boolean>,num : Int){
                iv.setOnClickListener {
                    if(!data3[num]){
                        iv.setBackgroundResource(R.drawable.logo_character)
                        rl.setBackgroundColor(Color.WHITE)
                        onOffList[num] = true
                    }else{
                        iv.setBackgroundResource(R.drawable.logo_character_sleep)
                        rl.setBackgroundColor(Color.GRAY)
                        onOffList[num] = false
                    }
                    val CONTACTS = data1[num]
                    onOffAlarm(CONTACTS,data2.xor(0x80.ushr(7).toByte()))
                }
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewholder = (holder as CustomViewHolder).itemView


            //remove with Long Click
            viewholder.setOnLongClickListener {
                val CONTACTS = contentDTOs[position]
                onOffList.removeAt(position)
                removeAlarm(CONTACTS)
                true
            }

            //send SMS with Short Click
            viewholder.setOnClickListener {
                val CONTACTS = contentDTOs[position]

                sendSMS(CONTACTS.TargetNums,CONTACTS.KTargetNums,view!!.context)

            }

            var text_time : String = ""

            if(contentDTOs[position].Targethours<12) text_time = "오전 "
            else text_time = "오후 "
            if(contentDTOs[position].Targethours==0||contentDTOs[position].Targethours==12) text_time += "12"
            else{
                if(contentDTOs[position].Targethours%12<10) text_time += "0"
                text_time += (contentDTOs[position].Targethours%12).toString()
            }
            if(contentDTOs[position].Targetminutes<10) text_time += " : 0"
            else text_time += " : "
            text_time += contentDTOs[position].Targetminutes.toString()
            viewholder.findViewById<TextView>(R.id.detail_time).text = text_time

            var Weekdays : Byte = contentDTOs[position].TargetDays
            var text_days : String = ""
            if(Weekdays.and(0x80.ushr(0).toByte())!=0.toByte()) text_days = "월"
            if(Weekdays.and(0x80.ushr(1).toByte())!=0.toByte()){
                if(text_days == "") text_days = "화"
                else text_days += " 화"
            }
            if(Weekdays.and(0x80.ushr(2).toByte())!=0.toByte()){
                if(text_days == "") text_days = "수"
                else text_days += " 수"
            }
            if(Weekdays.and(0x80.ushr(3).toByte())!=0.toByte()){
                if(text_days == "") text_days = "목"
                else text_days += " 목"
            }
            if(Weekdays.and(0x80.ushr(4).toByte())!=0.toByte()){
                if(text_days == "") text_days = "금"
                else text_days += " 금"
            }

            if(Weekdays.and(0x80.ushr(5).toByte())!=0.toByte())
            {
                if(Weekdays.and(0x80.ushr(6).toByte())!=0.toByte()){
                    if(text_days == "") text_days = "주말"
                    else if(text_days == "월 화 수 목 금") text_days = "매일 매일"
                    else text_days += " 토 일"
                }else{
                    if(text_days == "") text_days = "토"
                    else text_days += " 토"
                }
            }else{
                if(Weekdays.and(0x80.ushr(6).toByte())!=0.toByte()){
                    if(text_days == "") text_days = "일"
                    else text_days += " 일"
                }else{
                    if(text_days == "월 화 수 목 금") text_days = "평일 내내"
                }
            }
            viewholder.findViewById<TextView>(R.id.detail_days).text = text_days
            if(Weekdays.and(0x80.ushr(7).toByte())!=0.toByte()){
                viewholder.findViewById<RelativeLayout>(R.id.detail_background).setBackgroundColor(Color.GRAY)
                viewholder.findViewById<ImageView>(R.id.detail_image).setBackgroundResource(R.drawable.logo_character_sleep)
            }else{
                viewholder.findViewById<ImageView>(R.id.detail_image).setBackgroundResource(R.drawable.logo_character)
                onOffList[position] = true
            }
            holder.bind(alarmList,Weekdays,onOffList,position)
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }

    //remove Process
    fun removeAlarm(Alarm : SAlarmEntitiy)
    {
        val getTask = (object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                SalarmDB.salarmDAO().delete(Alarm)
                getAllalarms()
            }
        }).execute()

    }

    //remove Process
    fun onOffAlarm(Alarm : SAlarmEntitiy,Weekdays : Byte)
    {
        val getTask = (object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                SalarmDB.salarmDAO().update(Alarm.TimeStamp,Weekdays)
            }
        }).execute()

    }



}