package com.example.sharlam

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.sharlam.navigation.SettingViewFragment
import com.example.sharlam.navigation.ShareViewFragment
import com.example.sharlam.navigation.SingleViewFragment
import com.example.sharlam.navigation.SongViewFragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenu
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {


    var toast : Toast? = null // For exit notice
    var backKeyPressedTime : Long = 0 // For BackKey double pushed function
    var temp_fragment : Int = -1 // For checking the temp fragment


    //bottom navigation select listener
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.main_bottom_navigation_action_single ->{
                if(temp_fragment==0) return true
                var singleViewFragment = SingleViewFragment()
                replaceFragment(singleViewFragment)
                temp_fragment = 0
                return true
            }
            R.id.main_bottom_navigation_action_share ->{
                if(temp_fragment==1) return true
                var shareViewFragment = ShareViewFragment()
                replaceFragment(shareViewFragment)
                temp_fragment = 1
                return true
            }
            R.id.main_bottom_navigation_action_song ->{
                if(temp_fragment==2) return true
                var songViewFragment = SongViewFragment()
                replaceFragment(songViewFragment)
                temp_fragment = 2
                return true
            }
            R.id.main_bottom_navigation_action_setting ->{
                if(temp_fragment==3) return true
                var settingViewFragment = SettingViewFragment()
                replaceFragment(settingViewFragment)
                temp_fragment = 3
                return true
            }
        }
        return false
    }


    //For get temp fragment page
    fun getIndex() : Int{
        return temp_fragment
    }


    //Change Fragment without Click , By Computing
    fun ChangeFragment(index : Int) : Boolean{
        temp_fragment = index
        var bottomnavigationview : BottomNavigationView = findViewById(R.id.main_activity_bottom_navigation)
        when(index){
            0 ->{
                bottomnavigationview.setSelectedItemId(R.id.main_bottom_navigation_action_single);
                var singleViewFragment = SingleViewFragment()
                replaceFragment(singleViewFragment)
                return true
            }
            1 ->{
                bottomnavigationview.setSelectedItemId(R.id.main_bottom_navigation_action_share);
                var shareViewFragment = ShareViewFragment()
                replaceFragment(shareViewFragment)
                return true
            }
            2 ->{
                bottomnavigationview.setSelectedItemId(R.id.main_bottom_navigation_action_song);
                var songViewFragment = SongViewFragment()
                replaceFragment(songViewFragment)
                return true
            }
            3 ->{
                bottomnavigationview.setSelectedItemId(R.id.main_bottom_navigation_action_setting);
                var settingViewFragment = SettingViewFragment()
                replaceFragment(settingViewFragment)
                return true
            }
        }
        return false
    }

    //For Fragment Change Animation
    fun replaceFragment(Fragment : Fragment){
        val fragmentManager  = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.setCustomAnimations(R.anim.main_a_slide_from_right, R.anim.main_a_slide_to_right, R.anim.main_a_slide_from_right, R.anim.main_a_slide_to_right)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.replace(R.id.main_activity_main_content,Fragment)
        fragmentTransaction.commit()
    }

    @SuppressLint("ShowToast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomnavigationview : BottomNavigationView = findViewById(R.id.main_activity_bottom_navigation)
        bottomnavigationview.setOnNavigationItemSelectedListener(this)
        bottomnavigationview.selectedItemId = R.id.main_bottom_navigation_action_single;


        toast = Toast.makeText(this, "한번 더 누르면 종료됩니다. 꿀잠!", Toast.LENGTH_SHORT)
        backKeyPressedTime = System.currentTimeMillis()-2000
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

}