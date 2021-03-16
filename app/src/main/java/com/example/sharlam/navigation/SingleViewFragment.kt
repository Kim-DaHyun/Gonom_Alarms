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

@SuppressLint("StaticFieldLeak")
class SingleViewFragment : Fragment(){

    lateinit var db : SAlarmDatabase
    var alarmList : List<SAlarmEntitiy> = listOf<SAlarmEntitiy>()

    @SuppressLint("CutPasteId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_single,container,false)

        db = SAlarmDatabase.getInstance(container!!.context)!!

        var Btn_Add_Alarm : ImageView = view.findViewById(R.id.btn_add_alarm)
        Btn_Add_Alarm.setOnClickListener {
            startActivityForResult(Intent(view.context,AddAlarmActivity::class.java),100)
        }
        getAllalarms()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).adapter = DetailViewRecylerViewAdapter()
        view.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview).layoutManager = LinearLayoutManager(activity)

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            when(requestCode){
                100 -> {
                    getAllalarms()
                }
            }
        }
    }
    inner class DetailViewRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var contentDTOs : List<SAlarmEntitiy> = alarmList

        init {
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.alarm_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            viewholder.setOnLongClickListener(object : View.OnLongClickListener{
                override fun onLongClick(v: View?): Boolean {
                    val CONTACTS = contentDTOs[position]
                    removeAlarm(CONTACTS)
                    return true
                }
            })
            viewholder.setOnClickListener {
                val CONTACTS = contentDTOs[position]

                if(CONTACTS.TargetNums!!.size!=0){
                    sendSMS(CONTACTS.TargetNums!!,view!!.context)
                }

            }
            viewholder.findViewById<TextView>(R.id.detail_time).text = contentDTOs[position].Targethours.toString() + " : " + contentDTOs[position].Targetminutes.toString()
            viewholder.findViewById<TextView>(R.id.detail_title).text = contentDTOs[position].title.toString()
            if(contentDTOs[position].TargetDays!![0])
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_monday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(contentDTOs[position].TargetDays!![1])
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_tuesday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(contentDTOs[position].TargetDays!![2])
                viewholder.findViewById<ImageView>(R.id._detail_days_picker_wednesday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(contentDTOs[position].TargetDays!![3])
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_thursday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(contentDTOs[position].TargetDays!![4])
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_friday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(contentDTOs[position].TargetDays!![5])
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_saturday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN)
            if(contentDTOs[position].TargetDays!![6])
                viewholder.findViewById<ImageView>(R.id.detail_days_picker_sunday)?.setColorFilter(Color.parseColor("#3CCA75"), PorterDuff.Mode.SRC_IN);

        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }
    }

    fun sendSMS(Lists : Array<String>, context : Context){
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
    }

    fun getAllalarms(){
        val getTask = (object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                alarmList = db.salarmDAO().getAll()
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

    fun removeAlarm(Alarm : SAlarmEntitiy)
    {
        val getTask = (object : AsyncTask<Unit,Unit,Unit>(){
            override fun doInBackground(vararg params: Unit?) {
                db.salarmDAO().delete(Alarm)
                getAllalarms()
            }
        }).execute()

    }
    @SuppressLint("CutPasteId")
    fun setRecyclerView(alarmList : List<SAlarmEntitiy>){
        if(alarmList.size>0)
            view?.findViewById<ImageView>(R.id.single_background)?.visibility = View.INVISIBLE
        else
            view?.findViewById<ImageView>(R.id.single_background)?.visibility = View.VISIBLE
        view?.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview)?.adapter = DetailViewRecylerViewAdapter()
        view?.findViewById<RecyclerView>(R.id.detailviewfragment_recyclerview)?.layoutManager = LinearLayoutManager(activity)
    }
}