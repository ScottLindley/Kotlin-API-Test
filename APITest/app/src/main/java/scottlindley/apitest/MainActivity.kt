package scottlindley.apitest

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_main.*
import scottlindley.apitest.R.layout.activity_main
import android.widget.ArrayAdapter
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import android.widget.AdapterView.OnItemSelectedListener


class MainActivity : AppCompatActivity() {

    var bodyString: String = "{\n\n}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        initSpinner()
        initBodyView()

        val handleJson = fun (json: Any?) {
            if (json == null) return
            try {
                responseTextView.text = json.toString()
            } catch (e : Exception) { Log.e("handleJson", e.toString()) }
        }

        bodyButton.setOnClickListener({ toggleBodyMode() })

        submitButton.setOnClickListener({
            hitApi(urlInput.text.toString(), methodSelect.selectedItem.toString(), handleJson, bodyString)
        })
    }

    fun initSpinner() {
        val items = arrayOf("GET", "POST", "PUT", "DELETE")
        methodSelect.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        methodSelect.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "POST") {
                    bodyButton.visibility = View.VISIBLE
                }
                else {
                    bodyButton.visibility = View.INVISIBLE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    fun initBodyView() {
        bodyDoneButton.setOnClickListener({
            bodyString = bodyField.text.toString()
            toggleBodyMode()
        })
    }

    fun toggleBodyMode() {
        showHideView(urlInput)
        showHideView(methodSelect)
        showHideView(bodyButton)
        showHideView(submitButton)
        showHideView(bodyView)
        showHideView(responseView)

        bodyField.setText(bodyString)
`
    }

    fun showHideView(view: View) {
        view.visibility = if (view.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun hitApi(endpoint: String, method: String, cb: (Any?) -> Unit, body: String) {
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
                if (body != "{\n\n}") {
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
