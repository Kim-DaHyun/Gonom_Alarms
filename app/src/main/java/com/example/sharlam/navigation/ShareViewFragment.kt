package com.example.sharlam.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sharlam.AddFriendsActivity
import com.example.sharlam.AddGroupActivity
import com.example.sharlam.MainActivity
import com.example.sharlam.R
import com.example.sharlam.navigation.model.GroupAlarms
import com.example.sharlam.navigation.model.GroupidDTO
import com.example.sharlam.navigation.model.UserDTO
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.kakao.sdk.user.UserApiClient


class ShareViewFragment : Fragment(){


    var activity : MainActivity? = null

    var storage : FirebaseStorage? = null
    var firestore : FirebaseFirestore? = null
    var GroupIDs : MutableList<String> = arrayListOf()
    var GroupUrl : MutableList<String> = arrayListOf()
    val ADD_GROUP_REQUEST_CODE : Int = 103

    var uuid : Array<String> = arrayOf()
    var groupname : String = ""
    var imageurl : String = ""


    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity


        //init
        storage = FirebaseStorage.getInstance()
        firestore = FirebaseFirestore.getInstance()




    }

    override fun onDetach() {
        super.onDetach()
        activity = null


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_share, container, false)

        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if(error == null && tokenInfo != null){
                UserApiClient.instance.me{ user, error ->
                    val UserProperty = firestore!!.collection("UserIDs").document(user!!.id.toString())
                    UserProperty.get().addOnSuccessListener{ document ->
                        //val doc = document as UserDTO
                        if(document.exists()) {
                            if ((document.get("groupSize") as Long) > 0) {
                                UserProperty.collection("GroupIDs").get()
                                    .addOnSuccessListener { documents ->
                                        for (document in documents) {
                                            val GName = document.get("groupName") as String
                                            GroupIDs.add(GName)
                                            firestore!!.collection("GroupAlarms").document(GName).get().addOnSuccessListener { Urldocument ->
                                                GroupUrl.add(Urldocument.get("imageUrl") as String)
                                                if(documents.size() == GroupUrl.size){
                                                    view?.findViewById<RecyclerView>(R.id.share_frag_detailgroup_recyclerview)?.adapter = DetailViewRecylerViewAdapter()
                                                    view?.findViewById<RecyclerView>(R.id.share_frag_detailgroup_recyclerview)?.layoutManager = LinearLayoutManager(activity)
                                                }
                                            }
                                        }
                                    }
                                view?.findViewById<ImageView>(R.id.share_frag_background_image)!!.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
            }else{
                Toast.makeText(container!!.context,"로그인이 필요합니다.",Toast.LENGTH_SHORT).show()
                Handler().postDelayed({
                    if(activity?.getIndex()==1) activity?.ChangeFragment(3)
                },1000)
            }
        }


        view?.findViewById<ImageView>(R.id.share_fragement_add_alarm_btn)!!.setOnClickListener {

            val intent = Intent(context,AddGroupActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivityForResult(intent, ADD_GROUP_REQUEST_CODE)
        }


        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == ADD_GROUP_REQUEST_CODE){
            if(resultCode == AppCompatActivity.RESULT_OK){
                uuid = data!!.getSerializableExtra("KTargetNums") as Array<String>
                groupname = data!!.getStringExtra("GroupName") + System.currentTimeMillis().toString()
                imageurl = data!!.getStringExtra("uri")
                for(uid in uuid) {

                    firestore!!.collection("UserIDs").document(uid).get().addOnSuccessListener { document ->
                        var doc = UserDTO()
                        doc.groupSize = (document.get("groupSize") as Long).toInt() + 1
                        doc.accountTimeStamp = document.get("accountTimeStamp") as Long
                        if(document.get("male")!=null) doc.male = document.get("male") as Boolean
                        if(document.get("age")!=null) doc.age = document.get("age") as Int
                        doc.UserID = document.get("userID") as String
                        firestore!!.collection("UserIDs").document(uid).set(doc)

                    }
                    var groupidDTO = GroupidDTO()
                    groupidDTO.groupName = groupname
                    groupidDTO.joinTimeStamp = System.currentTimeMillis()
                    firestore!!.collection("UserIDs").document(uid).collection("GroupIDs").document().set(groupidDTO)

                }
                var groupalarms = GroupAlarms()
                groupalarms.imageUrl = imageurl
                groupalarms.groupName = groupname
                groupalarms.joinTimeStamp = System.currentTimeMillis()
                groupalarms.members = uuid.toList()
                firestore!!.collection("GroupAlarms").document(groupname).set(groupalarms)
                GroupIDs.add(groupname)
                GroupUrl.add(imageurl)
                view?.findViewById<RecyclerView>(R.id.share_frag_detailgroup_recyclerview)?.adapter = DetailViewRecylerViewAdapter()
                view?.findViewById<RecyclerView>(R.id.share_frag_detailgroup_recyclerview)?.layoutManager = LinearLayoutManager(activity)
            }
        }
    }


    inner class DetailViewRecylerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
        var ContentDTO = GroupIDs
        var ContentDTOs = GroupUrl
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.group_detail,parent,false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewholder = (holder as CustomViewHolder).itemView


            viewholder.findViewById<TextView>(R.id.Groupname).text = ContentDTO[position]
            if(ContentDTOs[position]!="") Glide.with(viewholder.context).load(ContentDTOs[position]).into(viewholder.findViewById(R.id.Groupimage))
        }

        override fun getItemCount(): Int {
            return ContentDTO.size
        }

    }

}