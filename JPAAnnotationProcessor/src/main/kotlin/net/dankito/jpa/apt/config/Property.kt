package net.dankito.jpa.apt.config

import java.lang.reflect.Field
import java.lang.reflect.Method


data class Property(val field: Field, val getter: Method?, val setter: Method?)
