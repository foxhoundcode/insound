package com.foxhound.insound.admin

import ir.mirrajabi.searchdialog.core.Searchable

data class CampusModel(val id: String, val name: String, val status: String) : Searchable {
    override fun getTitle(): String = name
}