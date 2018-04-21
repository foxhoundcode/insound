package com.foxhound.insound.admin

import android.os.Parcel
import android.os.Parcelable
import ir.mirrajabi.searchdialog.core.Searchable
import java.io.Serializable

class SearchableString(val key: String? = null, val string: String) : Searchable {
    override fun getTitle(): String = if(key != null) "[$key] $string" else string
    override fun toString(): String = string
}

class GenericCamposEditModel(val values: HashMap<String, String>, val names: HashMap<String, String>, val options: HashMap<String, HashMap<String, String>>, val edit_protection:  Map<String, Boolean>, val create_mandatory: Map<String, Boolean>) : Serializable, Parcelable {
    constructor(source: Parcel) : this(
            source.readSerializable() as HashMap<String, String>,
            source.readSerializable() as HashMap<String, String>,
            source.readSerializable() as HashMap<String, HashMap<String, String>>,
            source.readSerializable() as HashMap<String, Boolean>,
            source.readSerializable() as HashMap<String, Boolean>
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeSerializable(values)
        writeSerializable(names)
        writeSerializable(options)
        writeMap(edit_protection)
        writeMap(create_mandatory)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<GenericCamposEditModel> = object : Parcelable.Creator<GenericCamposEditModel> {
            override fun createFromParcel(source: Parcel): GenericCamposEditModel = GenericCamposEditModel(source)
            override fun newArray(size: Int): Array<GenericCamposEditModel?> = arrayOfNulls(size)
        }
    }
}

class GenericModel(val modulo: String, val url_save: String, val campos_edit: GenericCamposEditModel) : Serializable, Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readSerializable() as GenericCamposEditModel
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(modulo)
        writeString(url_save)
        writeSerializable(campos_edit)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<GenericModel> = object : Parcelable.Creator<GenericModel> {
            override fun createFromParcel(source: Parcel): GenericModel = GenericModel(source)
            override fun newArray(size: Int): Array<GenericModel?> = arrayOfNulls(size)
        }
    }
}