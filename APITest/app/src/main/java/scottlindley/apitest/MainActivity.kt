package scottlindley.apitest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_main.*
import scottlindley.apitest.R.layout.activity_main
import android.widget.ArrayAdapter
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        val handleJson = fun (json: Any?) {
            if (json == null) return
            try {
                Log.d("handleJson", json.toString())
                responseTextView.text = json.toString()
            } catch (e : Exception) { Log.e("handleJson", e.toString()) }
        }

        val items = arrayOf("GET", "POST", "PUT", "DELETE")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)

        submitButton.setOnClickListener({
            hitApi(urlInput.text.toString(), spinner.selectedItem.toString(), handleJson)
        })
    }

    fun hitApi(endpoint: String, method: String, cb: (Any?) -> Unit, body: String? = null) {
        val handleResp = fun (request: Request, response: Response, result: Result<Any, FuelError>) {
            Log.d("Request:", request.toString())
            Log.d("Response:", response.toString())
            val (data, error) = result
            if (error == null) {
                cb(data)
            } else {
                cb(error.toString())
            }
        }

        when (method) {
            "GET" -> {
                endpoint.httpGet().responseString { request, response, result ->
                    handleResp(request, response, result)
                }
            }
            "POST" -> {
                if (body != null) {
                    Fuel.post(endpoint).body(body).response { request, response, result ->
                        handleResp(request, response, result)
                    }
                }
                else {
                    Fuel.post(endpoint).response { request, response, result ->
                        handleResp(request, response, result)
                    }
                }
            }
            "PUT" -> {
                Fuel.put(endpoint).response { request, response, result ->
                    handleResp(request, response, result)
                }
            }
            "DELETE" -> {
                Fuel.delete(endpoint).response { request, response, result ->
                    handleResp(request, response, result)
                }
            }
        }
    }
}
