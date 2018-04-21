package com.foxhound.insound.admin

fun <K, V> HashMap<K, V>.getOrDefaultCompat(key: K, v: V): V {
    return if(this.containsKey(key)) this[key]!! else v
}
