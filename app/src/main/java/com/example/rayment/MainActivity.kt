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
import com.android.volley.ClientError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rayment.exceptions.BizException
import com.example.rayment.model.Account
import com.example.rayment.model.ResponseDTO
import com.example.rayment.spinner.MenuAccountSpinnerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.account_spinner_item.*
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {
    private lateinit var navigationView: NavigationView
    private lateinit var amountET: EditText
    private lateinit var phoneET: EditText

    var currentEmail: String? = null;
    var queue: RequestQueue? = null
    var accountSpinner: Spinner? = null

    var handlerUpdateSpinner: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            var accountList = msg?.obj as ArrayList<Account>;
            updateSpinnerData(accountList, accountSpinner)
        }
    }

    var handlerUpdateBalance: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            accountBalanceTV.text = msg?.obj.toString()
        }
    }

    var handlerShowToast: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            Toast.makeText(Application.ctx, msg?.obj as String?, Toast.LENGTH_SHORT).show()
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
                var view = accountSpinner?.selectedView
                var emailTV = view?.findViewById<TextView>(R.id.emailTV)
                var email = (emailTV?.text ?: "").toString()
                Log.d("ManiActivity", """email: ${email}""");
                if (email?.isEmpty()) {
                    return;
                }

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
            """${getResources().getString(R.string.base_url)}/payment/balance?account_id=${acId}&curr=HKD"""
        Log.d("MyTag", url)
        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                val responseDTO = Gson().fromJson(response, ResponseDTO::class.java)


                val message = obtain();
                message.obj = responseDTO.obj
                handlerUpdateBalance.sendMessage(message)

            },
            Response.ErrorListener {
                var jsonString: String = String(it.networkResponse.data)
                val responseDTO = Gson().fromJson(jsonString, ResponseDTO::class.java);
                Toast.makeText(this, responseDTO.message, Toast.LENGTH_SHORT).show()
                Log.e("", """${responseDTO.message}""")
                val message = obtain();
                message.obj = 0.0
                handlerUpdateBalance.sendMessage(message)
            })
        queue?.add(stringReq)
    }

    private fun getAccountIdByEmail(email: String): Int? {
        val url: String =
            """${getResources().getString(R.string.base_url)}/account/getAccountId?type=email&value=${URLEncoder.encode(
                email,
                "UTF-8"
            )}"""

        var acId: Int? = null;
        var responseString = RequestFuture.newFuture<String>()


        var request: StringRequest = StringRequest(url, responseString, responseString);
        queue?.add(request);


        var response: String? = responseString?.get(3, TimeUnit.SECONDS)
        val responseDTO = Gson().fromJson(response, ResponseDTO::class.java)
        acId = responseDTO.obj.toString().toDouble().toInt()
        Log.d("MyTag", """${acId} acId""")


        return acId;
    }

    private fun getAccountIdByPhone(phone: String): Int? {
        val url: String =
            """${getResources().getString(R.string.base_url)}/account/getAccountId?type=phone&value=${URLEncoder.encode(
                phone,
                "UTF-8"
            )}"""


        var acId: Int? = null;
        var responseString = RequestFuture.newFuture<String>()


        var request: StringRequest = StringRequest(url, responseString, responseString);
        queue?.add(request);


        var response: String? = responseString?.get(3, TimeUnit.SECONDS)
        val responseDTO = Gson().fromJson(response, ResponseDTO::class.java)
        acId = responseDTO.obj.toString().toDouble().toInt()
        Log.d("MyTag", """${acId} acId""")

        return acId;
    }

    private fun updateSpinner() {

        Log.d("MyTag", "updateSpinner")
        val url: String = "${getResources().getString(R.string.base_url)}/account/all"

        val stringReq = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                Log.d("", "Response get!!!")
                val responseDTO = Gson().fromJson(response, ResponseDTO::class.java);
                val listType = object : TypeToken<ArrayList<Account>>() {}.type

                val accounts: List<Account> =
                    Gson().fromJson(Gson().toJson(responseDTO.obj), listType);


                val message = obtain();
                message.obj = accounts as Object
                handlerUpdateSpinner.sendMessage(message)

                Log.d("MainActivity", """accounts.size:  ${accounts.size}""")
                if (accounts?.size > 0 && !((accounts[0].email?.isBlank()) ?: false)) {
                    Thread {
                        var acId: Int? = getAccountIdByEmail(accounts[0].email!!)
                        currentEmail = accounts[0].email!!
                        requestAndUpdateBalance(acId)
                    }.start()
                }

            },
            Response.ErrorListener {
                //                it.networkResponse.statusCode
                Toast.makeText(this, "Error !!! ", Toast.LENGTH_SHORT).show()
                Log.e("", "error")
            }
        )
        queue?.add(stringReq)
    }

    private fun updateSpinnerData(
        accountList: ArrayList<Account>,
        accountSpinner: Spinner?
    ) {
        var dataAdapter = MenuAccountSpinnerAdapter(this, accountList)
        accountSpinner?.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }

    fun ct(view: View) {
        Thread {
            Log.d("MyTag", amountET.text.toString())
            var amount: Double? = null
            var phone: String? = null
            try {
                amount = amountET.text.toString().toDouble()

            } catch (e: NumberFormatException) {
                Log.e("MyTag", e.message, e)
                return@Thread
            }
            try {
                phone = phoneET.text.toString()
            } catch (e: NumberFormatException) {
                Log.e("MyTag", e.message, e)
                return@Thread
            }

            var currentAcId: Int? = null
            var toAccountId: Int? = null
            try {
                currentAcId = getAccountIdByEmail(currentEmail.toString())
                toAccountId = getAccountIdByPhone(phone.toString())
            } catch (e: ExecutionException) {
                var rootCause: Throwable = e;
                while (rootCause.cause != null && rootCause.cause != rootCause) {
                    rootCause = rootCause.cause!!
                }
                if (rootCause is ClientError) {

                    var jsonString: String = String(rootCause.networkResponse.data)
                    val responseDTO = Gson().fromJson(jsonString, ResponseDTO::class.java);
                    val message = obtain();
                    message.obj = responseDTO.message
                    handlerShowToast.sendMessage(message)



                    Log.e("", """${responseDTO.message}""")

//                    throw BizException(responseDTO.message);
                }
                Log.d("", e.message, e)
                return@Thread
            }
            Log.d("MyTag", "currentEmail.toString(): " + currentEmail.toString())
            Log.d("MyTag", "currentAcId: " + currentAcId)

            val url: String =
                """${getResources().getString(R.string.base_url)}/payment/CT?from_acc_id=${currentAcId}&to_acc_id=${toAccountId}&curr=HKD&amount=${amount}"""

            val stringReq = StringRequest(
                Request.Method.PUT, url,
                Response.Listener<String> { response ->
                    val responseDTO = Gson().fromJson(response, ResponseDTO::class.java);


                    Toast.makeText(this, responseDTO.message, Toast.LENGTH_SHORT).show()
                    Thread {
                        //Update UI Balance
                        var acId: Int? = getAccountIdByEmail(currentEmail.toString())
                        requestAndUpdateBalance(acId)
                    }.start()
                },
                Response.ErrorListener {
                    //                it.networkResponse.statusCode
                    var jsonString: String = String(it.networkResponse.data)
                    val responseDTO = Gson().fromJson(jsonString, ResponseDTO::class.java);
                    Toast.makeText(this, responseDTO.message, Toast.LENGTH_SHORT).show()
                    Log.e("", """${responseDTO.message}""")
                }
            )
            queue?.add(stringReq)


        }.start()
    }


}
