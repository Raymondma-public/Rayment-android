package com.example.rayment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rayment.model.Account
import com.example.rayment.model.CurrencyAccount
import com.google.android.material.navigation.NavigationView
import org.json.JSONArray
import java.lang.Exception
import java.net.URL


class MainActivity : AppCompatActivity() {
    private lateinit var navigationView: NavigationView

    var currentAcc: Account?=null
    var accountMap =HashMap<String,Account>();
    var queue: RequestQueue? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        queue= Volley.newRequestQueue(this)

        navigationView = findViewById(R.id.nav_view)
        var headerView: View = navigationView.getHeaderView(0)
        var accountSpinner: Spinner = headerView.findViewById(R.id.accountSpinner)
        Log.d("MyTag", "Start")

        accountSpinner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("MyTag","onNothingSelected")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                var accountBalanceTV=findViewById<TextView>(R.id.accountBalanceTV)

//                val preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
//                preferences.edit().putString("account_id", ""+id).commit()
                var email=accountSpinner.selectedItem.toString()
                var ac: Account = accountMap.get(email)!!


                Thread{
                    Log.d("MyTag"," "+ac.id+" "+ac.name+" "+ac.email)
                val url: String =
                    """http://192.168.1.194:8080/payment/balance?account_id=${ac?.id}&curr=HKD"""
                    Log.d("MyTag",url)
                    try {
                        var response = URL(url).readText()
                        accountBalanceTV.text = response
                    }catch (e:Exception){
                        Log.e("MyTag",e.message)
                    }
                }.start()
            }

        }

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

        val url: String = "http://192.168.1.194:8080/account/all"



        accountMap=HashMap<String,Account>()
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
                    var currencyAccountJSONArr = accountJsonObj.getJSONArray("currencyAccountList")

                    var currencyAccountMap=HashMap<String,CurrencyAccount>()
                    for (i in 0 .. currencyAccountJSONArr.length()-1 ){
                        var currentAcc=currencyAccountJSONArr.getJSONObject(i)
                        var id=currentAcc.getInt("id")
                        var curr=currentAcc.getString("currency")
                        var balance=currentAcc.getDouble("balance")
                        currencyAccountMap.put(curr,CurrencyAccount(id,curr,balance))
                    }

//                    var accountBalanceTV=findViewById<TextView>(R.id.accountBalanceTV)
//                    accountBalanceTV.text=currencyAccountMap.

                    emailList.add(email)
                    Log.d("", email)
                    if (i == 0 && currentAcc == null) {
                        currentAcc = Account(id,name,email,currencyAccountMap)
                        val preferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                        preferences.edit().putString("account_id", ""+id).commit()
                    }

                    Log.d("MyTag"," "+id+" "+name+" "+email+" "+currencyAccountMap)
                    var ac=Account(id,name,email,currencyAccountMap)
                    accountMap.put(email,ac)
                    Log.d("MyTag"," "+ac.id+" "+ac.name+" "+ac.email)

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
        queue?.add(stringReq)
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
