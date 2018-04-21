package com.foxhound.insound.admin

import android.os.*
import android.support.v7.app.*
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.github.kittinunf.fuel.gson.responseObject
import com.github.kittinunf.fuel.httpGet
import kotlinx.android.synthetic.main.activity_menu.*
import org.jetbrains.anko.intentFor
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat
import ir.mirrajabi.searchdialog.core.SearchResultListener
import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat
import ir.mirrajabi.searchdialog.SimpleSearchFilter
import ir.mirrajabi.searchdialog.core.BaseFilter


class MenuActivity : AppCompatActivity() {

    private var loadingDialog: MaterialDialog? = null

    var campusId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_menu)
        title = "Menu"

        intent.getStringExtra("campusId")?.let {
            campusId = if(campusId.isEmpty()) it else campusId
        }

        buttonStudentIncome.setOnClickListener {
            loadingDialog = MaterialDialog.Builder(this)
                    .title(R.string.app_name)
                    .content(R.string.app_loading)
                    .progress(true, 0).show()
            requestStudents()
        }

        buttonOtherExpenses.setOnClickListener {
            startActivity(intentFor<GenericFormActivity>("genericUrl" to "/form_otherexpenses?campus_id=" + campusId, "campusId" to campusId))
        }

    }

    private fun requestStudents() = Statics.HOST.let { it -> it + "/list_students?campus_id=" + campusId }.httpGet().responseObject<List<StudentModel>> { _, _, result ->
        if(result.component1() != null) selectStudent(result.component1()!!) else error()
    }

    private fun selectStudent(listStudents: List<StudentModel>){
        loadingDialog?.dismiss()
        SimpleSearchDialogCompat(this, "Seleccionar Estudiante",
                "Buscar", null, ArrayList(listStudents),
                SearchResultListener<StudentModel> { dialog, item, position ->
                    startActivity(intentFor<GenericFormActivity>("genericUrl" to "/form_studentincome?campus_id=" + campusId + "&student_id=" + item.id, "campusId" to campusId))
                    dialog.dismiss()
                }).show()
        /*
        AlertDialog.Builder(this).apply {
            setTitle("Seleccionar Estudiante")
            setItems(listStudents.map { "[" + it.id + "] " + it.name + " " + it.surname }.toTypedArray()) { dialog, which ->
                startActivity(intentFor<GenericFormActivity>("genericUrl" to "/form_studentincome?campus_id=" + campusId + "&student_id=" + listStudents[which].id, "campusId" to campusId))
                dialog.dismiss()
            }
        }.show()
        */
    }

    private fun error(){
        loadingDialog?.dismiss()
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
    }
}