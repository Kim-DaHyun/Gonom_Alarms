package com.example.sharlam

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout






class LoadingActivity : AppCompatActivity() {

    lateinit var iv : ImageView
    lateinit var bg : RelativeLayout
    var clickForTransform : Boolean = false // For skipping animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading) // Set Activity

        this.clickForTransform = false // Init

        animating() // Start Animation


        //Skipping Animating Click Listener
        bg = findViewById(R.id.loading_activity_background)
        bg.setOnClickListener {
            clickForTransform = true
            startLoginActivity()
        }

        //After Animating  ( if works stopping animation being twice while loading startLoading Activity.
        Handler().postDelayed({
            if(!clickForTransform) {
                startLoginActivity()
            }
        },3200)

    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(0,0)
    }


    // Start Loading Activity
    fun startLoginActivity(){
        Handler().removeCallbacksAndMessages(null)
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }


    // Animation of Logo Image
    fun animating(){

        //Fade in Logo Image
        var animation = AnimationUtils.loadAnimation(this, R.anim.loading_a_fadein_turn)
        iv = findViewById(R.id.loading_activity_logo_image)
        iv.startAnimation(animation)

        //After Fade in,
        Handler().postDelayed({
            //Shaking Logo Image
            animation = AnimationUtils.loadAnimation(this,R.anim.loading_a_shaking)
            iv.startAnimation(animation)

            //After Shaking
            Handler().postDelayed({
                //turn to Logo Image to original
                animation = AnimationUtils.loadAnimation(this,R.anim.loading_a_turn_to_original)
                iv.startAnimation(animation)
            },1700)
        }, 1050)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
    }
}

