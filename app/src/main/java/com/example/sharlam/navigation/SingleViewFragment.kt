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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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

@SuppressLint("StaticFieldLeak")
class SingleViewFragment : Fragment(){

    lateinit var SalarmDB : SAlarmDatabase // Room SAlarm Database
    var alarmList : List<SAlarmEntitiy> = listOf<SAlarmEntitiy>() //SAlarm model list

    val ADD_ALARM_REQUEST_CODE : Int = 100

    @SuppressLint("CutPasteId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_single,container,false)
        SalarmDB = SAlarmDatabase.getInstance(container!!.context)!!

        // For Add Alarm Activity
        var Btn_Add_Alarm : ImageView = view.findViewById(R.id.single_fragement_add_alarm_btn)
        Btn_Add_Alarm.setOnClickListener {
            //Start Add Alarm Activity and get result to updating room db
            startActivityForResult(Intent(view.context,AddAlarmActivity::class.java),ADD_ALARM_REQUEST_CODE)
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

//            try {
//                val smsManager: SmsManager = SmsManager.getDefault()
//                smsManager.sendTextMessage(Lists[i], null, "확인용", null, null)
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
        }
        Toast.makeText(context,A,Toast.LENGTH_LONG).show()
        val defaultFeed = FeedTemplate(
                content = Content(
                        title = "딸기 치즈 케익",
                        description = "#케익 #딸기 #삼평동 #카페 #분위기 #소개팅",
                        imageUrl = "http://mud-kage.kakao.co.kr/dn/Q2iNx/btqgeRgV54P/VLdBs9cvyn8BJXB3o7N8UK/kakaolink40_original.png",
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

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewholder = (holder as CustomViewHolder).itemView


            //remove with Long Click
            viewholder.setOnLongClickListener {
                val CONTACTS = contentDTOs[position]
                removeAlarm(CONTACTS)
                true
            }

            //send SMS with Short Click
            viewholder.setOnClickListener {
                val CONTACTS = contentDTOs[position]

                sendSMS(CONTACTS.TargetNums,CONTACTS.KTargetNums,view!!.context)

            }

            viewholder.findViewById<TextView>(R.id.detail_time).text = contentDTOs[position].Targethours.toString() + " : " + contentDTOs[position].Targetminutes.toString()
            viewholder.findViewById<TextView>(R.id.detail_title).text = contentDTOs[position].title

            val Weekdays : Byte = contentDTOs[position].TargetDays

            if(Weekdays.and(0x80.ushr(0).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_monday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(Weekdays.and(0x80.ushr(1).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_tuesday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(Weekdays.and(0x80.ushr(2).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id._detail_days_picker_wednesday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(Weekdays.and(0x80.ushr(3).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_thursday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(Weekdays.and(0x80.ushr(4).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_friday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(Weekdays.and(0x80.ushr(5).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_saturday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(Weekdays.and(0x80.ushr(6).toByte())!=0.toByte())
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_sunday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN);

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



}