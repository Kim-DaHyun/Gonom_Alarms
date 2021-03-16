package com.example.sharlam.navigation

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.sharlam.MainActivity
import com.example.sharlam.R
import com.kakao.sdk.user.UserApiClient


class ShareViewFragment : Fragment(){


    var activity : MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = getActivity() as MainActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_share, container, false)

        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if(error == null && tokenInfo != null){

            }else{
                Toast.makeText(container!!.context,"로그인이 필요합니다.",Toast.LENGTH_SHORT).show()
                Handler().postDelayed({
                    if(activity?.getIndex()==1) activity?.ChangeFragment(3)
                },1000)
            }
        }


        return view
    }
}