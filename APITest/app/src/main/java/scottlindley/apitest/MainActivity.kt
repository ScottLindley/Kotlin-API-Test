package scottlindley.apitest

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import kotlinx.android.synthetic.main.activity_main.*
import scottlindley.apitest.R.layout.activity_main
import android.widget.ArrayAdapter
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Spinner
import android.widget.SpinnerAdapter
import com.github.kittinunf.fuel.*
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    var bodyString = "{\n\n}"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        initSpinner()
        initBodyView()

        val updateRespView = fun (json: String?) {
            if (json == null) return
            try {
                responseTextView.text = json
            } catch (e : Exception) { e.printStackTrace() }
        }

        bodyButton.setOnClickListener({ toggleBodyMode() })

        submitButton.setOnClickListener({
            hitApi(urlInput.text.toString(), methodSelect.selectedItem.toString(), updateRespView, bodyString)
        })
    }

    fun initSpinner() {
        val items = arrayOf("GET", "POST", "PATCH", "DELETE")
        methodSelect.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        methodSelect.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                if (selectedItem == "POST" || selectedItem == "PATCH") {
                    bodyButton.visibility = View.VISIBLE
                } else {
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

        if (this.currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
        }

        bodyField.setText(bodyString)
    }

    fun showHideView(view: View) {
        view.visibility = if (view.visibility == View.GONE) View.VISIBLE else View.GONE
    }

    fun hitApi(endpoint: String?, method: String?, cb: (String?) -> Unit, bodyString: String?) {

        if (endpoint == null || endpoint == "") return cb("url required")
        if (method == null) return cb("method required")
        if (method == "POST" || method == "PATCH" && bodyString == null) return cb("body required")

        val postHeaders = mapOf<String, Any>( Pair("Content-Type", "application/json") )

        try { JSONObject(bodyString) }
        catch ( e : Exception ) { return cb("Error parsing body - not valid JSON") }

        val handleResp = fun (request: Request, response: Response, result: Result<Any, FuelError>) {
            Log.d("Request:", request.toString())
            Log.d("Response:", response.toString())
            val (data, error) = result
            if (error == null) {
                if (method == "PATCH" || method == "DELETE") cb(String(data as ByteArray))
                else cb(data as String)
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
                endpoint.httpPost()
                        .body(bodyString!!.toByteArray())
                        .header(postHeaders)
                        .responseString { request, response, result ->
                            handleResp(request, response, result)
                        }
            }
            "PATCH" -> {
                endpoint.httpPatch()
                        .body(bodyString!!.toByteArray())
                        .header(postHeaders)
                        .response { request, response, result ->
                            handleResp(request, response, result)
                        }
            }
            "DELETE" -> {
                endpoint.httpDelete().response { request, response, result ->
                    handleResp(request, response, result)
                }
            }
        }
    }
}
