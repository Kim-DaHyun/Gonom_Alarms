package com.example.sharlam.navigation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sharlam.MainActivity
import com.example.sharlam.R
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import org.w3c.dom.Text

class SettingViewFragment : Fragment(){

    var logged : Boolean = false
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_setting,container,false)

        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if(error == null && tokenInfo != null){
                logged = true
                view.findViewById<TextView>(R.id.btn_log).text = "Logout"
            }else{
                logged = false
                view.findViewById<TextView>(R.id.btn_log).text = "Login"
            }
        }

        view.findViewById<TextView>(R.id.btn_log).setOnClickListener {
            Toast.makeText(container!!.context,logged.toString(),Toast.LENGTH_SHORT).show()
            if(logged){
                UserApiClient.instance.unlink { error ->
                    if(error != null){
                        Toast.makeText(container.context,"연결 끊기 실패.",Toast.LENGTH_SHORT).show()
                    }
                    else {
                        view.findViewById<TextView>(R.id.btn_log).text = "Login"
                        logged = false
                        Toast.makeText(container.context,"연결 끊기 성공. 토큰 삭제 됨",Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                kakaoLogin(container.context)
                logged = true
            }
        }

        return view
    }

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {

        }
        else if (token != null) {
            view!!.findViewById<TextView>(R.id.btn_log).text = "Logout"
        }

    }


    fun kakaoLogin(context: Context){
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }
}