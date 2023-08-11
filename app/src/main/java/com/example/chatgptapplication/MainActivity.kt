package com.example.chatgptapplication

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.Volley.*
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    lateinit var  queryEdt:TextInputEditText
    lateinit var messsageRV:RecyclerView
    lateinit var messageRVAdapter: MessageRVAdapter
    var messageList = ArrayList<MessageRVModel>()
    var url = "https://api.openai.com/v1/chat/completions"


    private fun getResponse(query: String) {
        queryEdt.setText("")
        val queue:RequestQueue = Volley.newRequestQueue(applicationContext)
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("model", "gpt-3.5-turbo");
        //jsonObject.put("model", "gpt-4");

        val messages = JSONArray()
        val message = JSONObject()
        message.put("role", "user");
        //message.put("content", "What are your functionalities?");
        message.put("content", query);
        messages.put(message);
        jsonObject.put("messages", messages);

        /*
        jsonObject?.put("temperature", 0)
        jsonObject?.put("max_token", 7)
        jsonObject?.put("top_p", 1)
        jsonObject?.put("frequency_penalty", 0.0)
        jsonObject?.put("presence_penalty", 0.0)

         */
        val postRequest: JsonObjectRequest = object : JsonObjectRequest(Method.POST, url, jsonObject, Response.Listener {
           val responseMsg: String = it.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            messageList.add(MessageRVModel(responseMsg,"bot"))
            messageRVAdapter.notifyDataSetChanged()
            }, Response.ErrorListener {
                Toast.makeText(applicationContext,"Fail to get response..", Toast.LENGTH_SHORT).show()
            }){
                override fun getHeaders(): MutableMap<String, String> {
                    val params: MutableMap<String,String> = HashMap()
                    params["Content-Type"] = "application/json"
                    params["Authorization"] = "Bearer myopenAIkey"
                    return params
                }
            }
        postRequest.setRetryPolicy(object:RetryPolicy{
            override fun getCurrentTimeout(): Int {
                return 50000
            }

            override fun getCurrentRetryCount(): Int {
                return 50000
            }
            override fun retry(error:VolleyError){

            }
        })
        queue.add(postRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        queryEdt = findViewById(R.id.idEdtQuery)
        messsageRV = findViewById(R.id.idRVMessage)
        messageRVAdapter = MessageRVAdapter(messageList)
        messsageRV.layoutManager = LinearLayoutManager(applicationContext)
        messsageRV.adapter = messageRVAdapter
        queryEdt.setOnEditorActionListener(TextView.OnEditorActionListener{textView,i, keyEvent ->
            if(i == EditorInfo.IME_ACTION_SEND){
                if(queryEdt.text.toString().length > 0){
                    messageList.add(MessageRVModel(queryEdt.text.toString(),"user"))
                    messageRVAdapter.notifyDataSetChanged()
                    getResponse(queryEdt.text.toString())
                }
                else{
                    Toast.makeText(this,"Please enter your query...", Toast.LENGTH_SHORT).show()
                }
                return@OnEditorActionListener true
            }
            false

        })
    }
}