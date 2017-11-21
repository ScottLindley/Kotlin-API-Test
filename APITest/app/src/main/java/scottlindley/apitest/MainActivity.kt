package scottlindley.apitest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import scottlindley.apitest.R.id.responseTextView
import scottlindley.apitest.R.layout.activity_main

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        FuelManager.instance.basePath = "https://snapsmedia.io/api/"

        val endpoint = "https://snapsmedia.io/api/discover/"

        val handleJson = fun (json: String?) {
            Log.d("handleJson", json)
            if (json == null) return
            findViewById<TextView>(responseTextView).setText(json)
        }

        hitApi(endpoint, handleJson)
    }

    fun hitApi(endpoint: String, cb: (String?) -> Unit) {
        endpoint.httpGet().responseString { request, response, result ->
            Log.d("Request:", request.toString())
            Log.d("Response:", response.toString())
            val (data, error) = result
            if (error == null) {
                cb(data)
            } else {
                cb(error.toString())
            }
        }
    }
}
