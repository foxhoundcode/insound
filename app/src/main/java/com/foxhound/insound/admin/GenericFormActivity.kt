package com.foxhound.insound.admin

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import android.text.InputType
import android.widget.Button
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import com.github.kittinunf.fuel.httpUpload
import java.io.File
import com.github.kittinunf.fuel.core.Method
import kotlinx.android.synthetic.main.activity_menu.*
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.app.Activity
import android.content.res.Resources
import android.support.v4.app.ActivityCompat
import android.text.Layout
import android.view.View
import android.view.ViewGroup
import com.desmond.squarecamera.CameraActivity
import com.github.kittinunf.fuel.core.DataPart
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat
import ir.mirrajabi.searchdialog.core.SearchResultListener
import ir.mirrajabi.searchdialog.core.Searchable
import org.jetbrains.anko.intentFor


class GenericFormActivity : AppCompatActivity() {

    private var loadingDialog: MaterialDialog? = null
    private var fileValueTextView: TextView? = null
    private var activityResultKey = ""

    var response = HashMap<String, String>()
    var genericUrl = ""
    var campusId = ""

    lateinit var form: GenericModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form_generic)

        intent.getStringExtra("genericUrl")?.let {
            genericUrl = if(genericUrl.isEmpty()) it else genericUrl
        }

        intent.getStringExtra("campusId")?.let {
            campusId = if(campusId.isEmpty()) it else campusId
        }

        loadingDialog = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.app_loading)
                .progress(true, 0).show()

        Thread({
            Statics.HOST.let { it -> it + genericUrl }.httpGet().responseObject<GenericModel> { _, _, result ->
                if(result.component1() != null) showForm(result.component1()!!) else error()
            }
        }).start()
    }

    private fun showForm(form: GenericModel) {
        this.title = form.modulo
        this.form = form

        form.campos_edit.values.apply{
            keys.forEach { key ->
                val titleTextView = TextView(this@GenericFormActivity)
                titleTextView.text = form.campos_edit.names.getOrDefaultCompat(key, "-") + if(form.campos_edit.create_mandatory.getOrDefault(key, false)) " (*)" else ""
                linearLayout.addView(titleTextView)
                when(this[key]) {
                    "selector" -> {
                        val valueTextView = TextView(this@GenericFormActivity)
                        valueTextView.text = "-"
                        linearLayout.addView(valueTextView)
                        val valueButton = Button(this@GenericFormActivity)
                        valueButton.text = "Seleccionar"
                        valueButton.setOnClickListener {
                            SimpleSearchDialogCompat(this@GenericFormActivity, "Seleccionar " + this[key],
                                    "Buscar", null, ArrayList(form.campos_edit.options.getOrDefaultCompat(key, HashMap()).map { SearchableString(it.key, it.value) }),
                                    SearchResultListener<SearchableString> { dialog, item, position ->
                                        valueTextView.text = "[${item.key}] ${item.string}"
                                        response.put(item.key!!, item.string)
                                        dialog.dismiss()
                                    }).show()
                            /*
                            AlertDialog.Builder(this@GenericFormActivity).apply {
                                setTitle("Seleccionar")
                                setItems(form.campos_edit.options.getOrDefaultCompat(key, HashMap()).values.toTypedArray()) { dialog, which ->
                                    val selectedKey = form.campos_edit.options[key]!!.keys.toTypedArray()[which]
                                    val selectedValue = form.campos_edit.options[key]!!.values.toTypedArray()[which]
                                    valueTextView.text = selectedValue
                                    response.put(key, selectedKey)
                                    dialog.dismiss()
                                }
                            }.show()
                            */
                        }
                        linearLayout.addView(valueButton)
                    }
                    "string" -> {
                        val valueEditText = EditText(this@GenericFormActivity)
                        valueEditText.addTextChangedListener(object: TextWatcher{
                            override fun beforeTextChanged(var1: CharSequence, var2: Int, var3: Int, var4: Int){}
                            override fun onTextChanged(var1: CharSequence, var2: Int, var3: Int, var4: Int){}
                            override fun afterTextChanged(var1: Editable) { response.put(key, var1.toString()) }
                        })
                        linearLayout.addView(valueEditText)
                    }
                    "decimal" -> {
                        val valueEditText = EditText(this@GenericFormActivity)
                        valueEditText.inputType = InputType.TYPE_CLASS_NUMBER
                        valueEditText.addTextChangedListener(object: TextWatcher{
                            override fun beforeTextChanged(var1: CharSequence, var2: Int, var3: Int, var4: Int){}
                            override fun onTextChanged(var1: CharSequence, var2: Int, var3: Int, var4: Int){}
                            override fun afterTextChanged(var1: Editable) { response.put(key, var1.toString()) }
                        })
                        linearLayout.addView(valueEditText)
                    }
                    "file" -> {
                        val valueTextView = TextView(this@GenericFormActivity)
                        valueTextView.text = "-"
                        linearLayout.addView(valueTextView)
                        val valueImageButton = Button(this@GenericFormActivity)
                        valueImageButton.text = "Tomar Foto"
                        valueImageButton.setOnClickListener {
                            this@GenericFormActivity.activityResultKey = key
                            this@GenericFormActivity.fileValueTextView = valueTextView
                            requestForCameraPermission()
                        }
                        linearLayout.addView(valueImageButton)
                    }
                    "date" -> {
                        val valueTextView = TextView(this@GenericFormActivity)
                        valueTextView.text = "-"
                        linearLayout.addView(valueTextView)
                        val valueButton = Button(this@GenericFormActivity)
                        valueButton.text = "Seleccionar"
                        val now = java.util.Calendar.getInstance()
                        val value = now.get(java.util.Calendar.YEAR).toString() + "-" + now.get(java.util.Calendar.MONTH).toString() + "-" + now.get(java.util.Calendar.DAY_OF_MONTH).toString()
                        valueTextView.text = value
                        valueButton.setOnClickListener {
                            DatePickerDialog.newInstance({ dialog, year, month, dayOfMonth ->
                                val value = year.toString() + "-" + month.toString() + "-" + dayOfMonth.toString()
                                valueTextView.text = value
                                response.put(key, value)
                                dialog.dismiss()
                            }, now.get(java.util.Calendar.YEAR), now.get(java.util.Calendar.MONTH), now.get(java.util.Calendar.DAY_OF_MONTH)).show(fragmentManager, "date_" + key)
                        }
                        linearLayout.addView(valueButton)
                    }
                }
                val space = View(this@GenericFormActivity)
                space.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 10.px)
                linearLayout.addView(space)
            }
            if(this.count() > 0){
                val submitButton = Button(this@GenericFormActivity)
                submitButton.text = "Guardar"
                submitButton.setOnClickListener { submit() }
                linearLayout.addView(submitButton)
            }else{
                finish()
            }
        }

        loadingDialog?.dismiss()
    }

    private fun submit() = if(validateForm()) requestForm() else Toast.makeText(this, "Faltan completar campos obligatorios", Toast.LENGTH_LONG).show()

    private fun validateForm(): Boolean = form.campos_edit.create_mandatory.filter { it.value }.keys.count { !response.containsKey(it) || (response.containsKey(it) && response[it]!!.isEmpty()) } <= 0

    private fun requestForm() {

        loadingDialog = MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.app_loading)
                .progress(true, 0).show()

        Statics.HOST.let { it -> it + "form_generic/" + form.url_save + "?campus_id=" + campusId }.httpUpload(Method.POST, response.toList()).dataParts { request, url ->
            form.campos_edit.values.filter { p -> p.value == "file" }.keys.map { DataPart(File(response[it]), it) }
        }.responseString { request, response, result ->
            if(result.component1() != null) (if(result.component1().equals("0"))success() else error(result.component1()!!)) else error()
        }
    }

    private val REQUEST_CAMERA = 0
    private val REQUEST_CAMERA_PERMISSION = 0

    // Check for camera permission in MashMallow
    fun requestForCameraPermission() {
        val permission = Manifest.permission.CAMERA
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                // Show permission rationale
            } else {
                // Handle the result in Activity#onRequestPermissionResult(int, String[], int[])
                ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CAMERA_PERMISSION)
            }
        } else {
            // Start CameraActivity
            val startCustomCameraIntent = Intent(this, CameraActivity::class.java)
            startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA)
        }
    }

    // Receive Uri of saved square photo
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == REQUEST_CAMERA) {
            if (resultCode == Activity.RESULT_OK) {
                val path = data.data.path
                if (File(path).exists()) {
                    response.put(activityResultKey, path)
                    fileValueTextView?.text = ""
                } else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun success(){
        loadingDialog?.dismiss()
        Toast.makeText(this, "Guardado exitoso", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun error(error: String? = null){
        loadingDialog?.dismiss()
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
        finish()
    }


    val Int.px: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

}


