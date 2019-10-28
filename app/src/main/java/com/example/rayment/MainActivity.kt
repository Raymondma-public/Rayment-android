package com.example.rayment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.nav_header_main2.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.log
import android.os.AsyncTask
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.example.rayment.model.Account


class MainActivity : AppCompatActivity() {
    private lateinit var navigationView: NavigationView

    var currentAcc: Account?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigationView = findViewById(R.id.nav_view)
        var headerView: View = navigationView.getHeaderView(0)
        var accountSpinner: Spinner = headerView.findViewById(R.id.accountSpinner)
        Log.d("MyTag", "Start")

//        var emailList=ArrayList<String>()
//        emailList.add("raymondma.public@test.com")
//        emailList.add("raymondma.public2@test.com")
//        emailList.add("raymondma.public3@test.com")
//        updateSpinnerData(emailList, accountSpinner)
        Thread {
            updateSpinner(accountSpinner)
        }.start()


    }

    private fun updateSpinner(accountSpinner: Spinner) {


        Log.d("MyTag", "updateSpinner")
        val queue = Volley.newRequestQueue(this)
        val url: String = "http://192.168.1.194:8080/account/all"


        var response = URL(url).readText()
        Log.d("MyTag", "Response get!!!")


        var emailList = ArrayList<String>()
        var strResp = response.toString()

//        var accountJsonArr = JSONArray(strResp)
//        for (i in 0 .. accountJsonArr.length()-1) {
//            var accountJsonObj = accountJsonArr.getJSONObject(i);
//            emailList.add(accountJsonObj.getString("email"))
//            println(accountJsonObj.getString("email"))
//            Log.d("MyTag", accountJsonObj.getString("email"))
//        }
//
//        updateSpinnerData(emailList, accountSpinner)


        // Request a string response from the provided URL.
        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("", "Response get!!!")


                var emailList = ArrayList<String>()
                var strResp = response.toString()

                var accountJsonArr = JSONArray(strResp)
                for (i in 0..accountJsonArr.length() - 1) {
                    var accountJsonObj = accountJsonArr.getJSONObject(i);

                    var id = accountJsonObj.getInt("id")
                    var email = accountJsonObj.getString("email")
                    var name = accountJsonObj.getString("name")

                    emailList.add(email)
                    Log.d("", email)
                    if (i == 0 && currentAcc == null) {
                        currentAcc = Account(id,name,email)
                    }
                }

                updateSpinnerData(emailList, accountSpinner)

                //                val jsonObj: JSONObject = JSONObject(strResp)
                //                val jsonArray: JSONArray = jsonObj.getJSONArray("items")
                //                var str_user: String = ""
                //                for (i in 0 until jsonArray.length()) {
                //                    var jsonInner: JSONObject = jsonArray.getJSONObject(i)
                //                    str_user = str_user + "\n" + jsonInner.get("login")
                //                }
                //                textView!!.text = "response : $str_user "
            },
            Response.ErrorListener {
                //                it.networkResponse.statusCode
                Toast.makeText(this, "Error !!! ", Toast.LENGTH_SHORT)
                Log.e("", "Ge")
            }
        )
        queue.add(stringReq)
    }

    private fun updateSpinnerData(
        emailList: ArrayList<String>,
        accountSpinner: Spinner
    ) {
        var dataAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emailList)
        accountSpinner.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }

}
