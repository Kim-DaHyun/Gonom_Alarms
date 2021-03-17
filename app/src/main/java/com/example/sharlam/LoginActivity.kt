package com.example.sharlam

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sharlam.navigation.model.UserDTO
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

import com.kakao.sdk.auth.model.OAuthToken

import com.kakao.sdk.user.UserApiClient


class LoginActivity : AppCompatActivity() {
    lateinit var iv : ImageView
    lateinit var btn : Button

    var backKeyPressedTime : Long = 0 // For BackKey double pushed function
    var toast : Toast? = null // For exit notice


    var auth : FirebaseAuth? = null
    //var googleSignInClient : GoogleSignInClient? = null
    //var GOOGLE_LOGIN_CODE = 9001

    var firestore : FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        /* 구글 로그인 비활성화
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        */

        //DATABASE connect
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        //If already logged, skipping animation
        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
            if(error == null && tokenInfo != null) moveToMainPage()
            else animating()
        }

        //google login btn
        btn = findViewById(R.id.login_activity_google_login_button)
        btn.setOnClickListener {
            //googleLogin()
        }

        //kakao login btn
        btn = findViewById(R.id.login_activity_kakao_login_button)
        btn.setOnClickListener {
            kakaoLogin()
        }

        //offline btn
        iv = findViewById(R.id.login_activity_offline_image)
        iv.setOnClickListener {
            moveToMainPage()
        }

        backKeyPressedTime = System.currentTimeMillis()-2000
        toast = Toast.makeText(this, "한번 더 누르면 종료됩니다. 꿀잠!", Toast.LENGTH_SHORT)
    }

    private fun animating(){
        var animation = AnimationUtils.loadAnimation(this, R.anim.login_a_move_to_top)
        iv = findViewById(R.id.login_activity_logo_image)
        iv.startAnimation(animation)

        Handler().postDelayed({
            iv = findViewById(R.id.login_activity_offline_image)
            animation = AnimationUtils.loadAnimation(this, R.anim.login_a_fadein)
            iv.visibility = View.VISIBLE

            btn = findViewById(R.id.login_activity_google_login_button)
            //btn.startAnimation(animation)
            //btn.visibility = View.VISIBLE
            //구글 버튼 비활성화

            btn = findViewById(R.id.login_activity_naver_login_button)
            //btn.startAnimation(animation)
            //btn.visibility = View.VISIBLE
            //네이버 버튼 비활성화

            btn = findViewById(R.id.login_activity_kakao_login_button)
            btn.startAnimation(animation)
            btn.visibility = View.VISIBLE

            animation = AnimationUtils.loadAnimation(this, R.anim.login_a_pump)
            iv.startAnimation(animation)

        }, 300)
    }
    override fun onBackPressed() {

        //super.onBackPressed()
        if(System.currentTimeMillis()>backKeyPressedTime+2000){
            backKeyPressedTime = System.currentTimeMillis()
            toast?.show()
        }
        else{
            toast?.cancel()
            finish()
        }
    }

    /* with move to MainPage with google Login
    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
            finish()
        }

    }
     */

    val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Toast.makeText(this,"위 버튼을 누르면 비 로그인으로 이용 가능합니다!",Toast.LENGTH_LONG).show()
        }
        else if (token != null) {
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
            moveToMainPage()
        }

    }


    fun moveToMainPage(){
        startActivity(Intent(this, MainActivity::class.java))
        overridePendingTransition(R.anim.activity_slide_in_left,R.anim.activity_slide_out_right)
        finish()
    }

    fun kakaoLogin(){

        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    /* google Login
    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }
    */

    /* ActivityResult for GoogleLogin
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result!!.isSuccess){
                var account = result.signInAccount
                //Second google
                firebaseAuthWithGoogle(account)
            }
        }
    }
    */

    /* FirebaseAuth With Google
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        moveMainPage(task.result?.user)
                    }else{
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
    }
     */
}