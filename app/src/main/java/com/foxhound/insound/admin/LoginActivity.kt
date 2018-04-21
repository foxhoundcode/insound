package com.foxhound.insound.admin

import android.os.*
import android.support.v7.app.*
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat
import ir.mirrajabi.searchdialog.core.SearchResultListener
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.intentFor
import java.util.ArrayList

class LoginActivity : AppCompatActivity() {


    private var loadingDialog: MaterialDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        buttonSubmit.setOnClickListener {
            loadingDialog = MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .content(R.string.app_loading)
                    .progress(true, 0).show()

            val parameters = ArrayList<Pair<String, String>>()
            parameters.add("user" to editTextUser.text.toString())
            parameters.add("pass" to editTextPassword.text.toString())
            requestLogin(parameters)
        }

    }

    private fun requestLogin(parameters: ArrayList<Pair<String, String>>) = Statics.HOST.let { it -> it + "/login" }.httpPost(parameters).responseString { _, _, result ->
        if(result.component1() != null && result.component1().equals("1")) requestCampus() else error()
    }

    private fun requestCampus() = Statics.HOST.let { it -> it + "/list_campus" }.httpGet().responseObject<List<CampusModel>> { _, _, result ->
        if(result.component1() != null) selectCampus(result.component1()!!) else error()
    }

    private fun selectCampus(listCampus: List<CampusModel>){
        loadingDialog?.dismiss()
        SimpleSearchDialogCompat(this, "Seleccionar Campus",
                "Buscar", null, ArrayList(listCampus),
                SearchResultListener<CampusModel> { dialog, item, position ->
                    startActivity(intentFor<MenuActivity>("campusId" to item.id))
                    dialog.dismiss()
                    finish()
                }).show()
        /*
        AlertDialog.Builder(this).apply {
            setTitle("Seleccionar Campus")
            setItems(listCampus.filter { it.status == "1" }.map { it.name }.toTypedArray()) { dialog, which ->
                startActivity(intentFor<MenuActivity>("campusId" to listCampus[which].id))
                dialog.dismiss()
                finish()
            }
        }.show()
        */
    }

    private fun error(){
        loadingDialog?.dismiss()
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
    }
}
