package com.example.rayment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.os.Message.obtain
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
import com.example.rayment.model.ResponseDTO
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.net.URL
import java.net.URLEncoder


class MainActivity : AppCompatActivity() {
    private lateinit var navigationView: NavigationView
    private lateinit var amountET: EditText
    private lateinit var phoneET: EditText

    var currentEmail: String? = null;
    var queue: RequestQueue? = null
    var accountSpinner: Spinner? = null

    var handlerUpdateSpinner: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            var emailList = msg?.obj as ArrayList<String>;
            updateSpinnerData(emailList, accountSpinner)
        }
    }

    var handlerUpdateBalance: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            accountBalanceTV.text = msg?.obj.toString()
        }
    }

    var handlerShowToast: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            Toast.makeText(Application.ctx, msg?.obj as String?, Toast.LENGTH_SHORT)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        queue = Volley.newRequestQueue(this)

        navigationView = findViewById(R.id.nav_view)
        amountET = findViewById(R.id.dollorET) as EditText
        phoneET = findViewById(R.id.phoneET) as EditText
        var headerView: View = navigationView.getHeaderView(0)
        accountSpinner = headerView.findViewById(R.id.accountSpinner)
        Log.d("MyTag", "Start")

        accountSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("MyTag", "onNothingSelected")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {


                var accountBalanceTV = findViewById<TextView>(R.id.accountBalanceTV)
                var email = accountSpinner?.selectedItem.toString()
                currentEmail = email;
                Thread {
                    var acId: Int? = getAccountIdByEmail(email)
                    requestAndUpdateBalance(acId)
                }.start()
            }

        }

        Thread {
            updateSpinner()
        }.start()


    }

    private fun requestAndUpdateBalance(acId: Int?) {
        val url: String =
            """http://192.168.1.194:8080/payment/balance?account_id=${acId}&curr=HKD"""
        Log.d("MyTag", url)
        try {
            var response = URL(url).readText()
            val responseDTO = Gson().fromJson(response, ResponseDTO::class.java)


            val message = obtain();
            message.obj = responseDTO.obj
            handlerUpdateBalance.sendMessage(message)

        } catch (e: Exception) {
            Log.e("MyTag", "error here", e)
            val message = obtain();
            message.obj = 0
            handlerUpdateBalance.sendMessage(message)

            val errorMessage = obtain();
            errorMessage.obj = e.message
            handlerShowToast.sendMessage(errorMessage)
        }
    }

    private fun getAccountIdByEmail(email: String): Int? {

        val urlGetId: String =
            """http://192.168.1.194:8080/account/getAccountId?type=email&value=${URLEncoder.encode(
                email,
                "UTF-8"
            )}"""
        Log.d("MyTag", urlGetId)

        var acId: Int? = null;
        try {
            var response = URL(urlGetId).readText()
            val responseDTO = Gson().fromJson(response, ResponseDTO::class.java)
            acId = responseDTO.obj.toString().toDouble().toInt()
            Log.d("MyTag", """${acId} acId""")
        } catch (e: Exception) {
            Log.e("MyTag", "error here", e)
        }
        return acId
    }

    private fun getAccountIdByPhone(phone: String): Int? {
        val urlGetId: String =
            """http://192.168.1.194:8080/account/getAccountId?type=phone&value=${URLEncoder.encode(
                phone,
                "UTF-8"
            )}"""
        Log.d("MyTag", urlGetId)

        var acId: Int? = null;
        try {
            var response = URL(urlGetId).readText()
            Log.d("MyTag", """response""")
            val responseDTO = Gson().fromJson(response, ResponseDTO::class.java)
            acId = responseDTO.obj.toString().toDouble().toInt()
            Log.d("MyTag", """${acId} acId""")
        } catch (e: Exception) {
            Log.e("MyTag", "error here", e)
        }
        return acId
    }

    private fun updateSpinner() {

        Log.d("MyTag", "updateSpinner")
        val url: String = "http://192.168.1.194:8080/account/all"

        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("", "Response get!!!")
                val responseDTO = Gson().fromJson(response, ResponseDTO::class.java);
                val listType = object : TypeToken<ArrayList<Account>>() {}.type

                val accounts: List<Account> =
                    Gson().fromJson(Gson().toJson(responseDTO.obj), listType);
                val emailList = accounts.map { ac -> ac.email };

                val message = obtain();
                message.obj = emailList as Object
                handlerUpdateSpinner.sendMessage(message)


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
        accountSpinner: Spinner?
    ) {
        var dataAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, emailList)
        accountSpinner?.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }

    fun ct(view: View) {

        Thread {
            Log.d("MyTag", amountET.text.toString())
            var amount = amountET.text.toString().toDouble()
            var phone = phoneET.text.toString()
            var currentAcId: Int? = getAccountIdByEmail(currentEmail.toString())
            var toAccountId: Int? = getAccountIdByPhone(phone.toString())

            Log.d("MyTag", "currentEmail.toString(): " + currentEmail.toString())
            Log.d("MyTag", "currentAcId: " + currentAcId)


            val url: String =
                """http://192.168.1.194:8080/payment/CT?from_acc_id=${currentAcId}&to_acc_id=${toAccountId}&curr=HKD&amount=${amount}"""

            val stringReq = StringRequest(
                Request.Method.PUT, url,
                Response.Listener<String> { response ->
                    val responseDTO = Gson().fromJson(response, ResponseDTO::class.java);


                    Toast.makeText(this, responseDTO.message, Toast.LENGTH_SHORT)
                    Thread {
                        //Update UI Balance
                        var acId: Int? = getAccountIdByEmail(currentEmail.toString())
                        requestAndUpdateBalance(acId)
                    }.start()
                },
                Response.ErrorListener {
                    //                it.networkResponse.statusCode
                    Toast.makeText(this, "Error !!! ", Toast.LENGTH_SHORT)
                    Log.e("", "Ge")
                }
            )
            queue?.add(stringReq)


        }.start()
    }
}
