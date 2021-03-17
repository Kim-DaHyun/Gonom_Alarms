package com.example.sharlam

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kakao.sdk.talk.TalkApiClient
import com.kakao.sdk.talk.model.FriendOrder
import com.kakao.sdk.user.UserApiClient

class AddGroupAddFriends : AppCompatActivity() {
    var k_checkedList : MutableList<Boolean>? = null
    var thumbnails : MutableList<String?> = arrayListOf()
    var nicknames : MutableList<String> = arrayListOf()
    var uuid : MutableList<String> = arrayListOf()
    var friends_size : Int = 0
    var k_outlist : Array<String> = arrayOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group_add_friends)

        UserApiClient.instance.me{ user, error ->

            TalkApiClient.instance.friends(friendOrder= FriendOrder.FAVORITE) { friends, error ->
                if (error != null) {
                    Toast.makeText(this, "친구 목록 불러오기 실패: ${error}", Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    friends_size = friends!!.totalCount
                    for (friend in friends.elements) {
                        thumbnails.add(friend.profileThumbnailImage)
                        nicknames.add(friend.profileNickname)
                        uuid.add(friend.id.toString())
                    }
                    k_checkedList = MutableList(friends_size) { false }
                    Toast.makeText(this, "친구목록가져옴" + friends_size.toString(), Toast.LENGTH_SHORT).show()
                    findViewById<RecyclerView>(R.id.Addgroup_detail_add_kfriends).adapter = KDetailViewRecyclerViewAdapter()
                    findViewById<RecyclerView>(R.id.Addgroup_detail_add_kfriends).layoutManager = LinearLayoutManager(this)
                    k_outlist = k_outlist.plus(user!!.id.toString())
                }
            }
        }









        findViewById<ImageView>(R.id.add_member_btn).setOnClickListener {
            sendSMS()

            val resultIntent = Intent()

            resultIntent.putExtra("KTargetNums",k_outlist)
            setResult(RESULT_OK, resultIntent)
            finish()
        }

    }

    inner class KDetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.kfriends_detail, parent, false)
            return KCustomViewHolder(view)
        }

        inner class KCustomViewHolder(view : View?) : RecyclerView.ViewHolder(view!!){
            var checkbox : CheckBox = view!!.findViewById(R.id.kfriends_checkbox)
            var textview : TextView = view!!.findViewById(R.id.kfriends_name)
            var imageview : ImageView = view!!.findViewById(R.id.kfriends_image)

            fun bind(data1 : MutableList<String?>,data2 : MutableList<String>, data3 : MutableList<Boolean>, num : Int, context : Context) {
                textview.text = data2[num]
                Glide.with(context).load(data1[num]).into(imageview)
                checkbox.isChecked = data3[num]
                checkbox.setOnClickListener {
                    k_checkedList!![num] = checkbox.isChecked
                }
            }

        }
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as KCustomViewHolder).bind(thumbnails,nicknames,k_checkedList!!,position,holder.itemView.context)
        }

        override fun getItemCount(): Int {
            return friends_size
        }

    }



    fun sendSMS(){

        for(position in 0 until k_checkedList!!.size){
            if(k_checkedList!![position]==true){
                k_outlist = k_outlist.plus(uuid[position])
            }
        }
    }
}

