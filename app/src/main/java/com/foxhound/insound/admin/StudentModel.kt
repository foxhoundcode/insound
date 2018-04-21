package com.foxhound.insound.admin

import ir.mirrajabi.searchdialog.core.Searchable

data class StudentModel(val id: String, val name: String, val surname: String)  : Searchable {
    override fun getTitle(): String = "[$id] $name $surname"
}