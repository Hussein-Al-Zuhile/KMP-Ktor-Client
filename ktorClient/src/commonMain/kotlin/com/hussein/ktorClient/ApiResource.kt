package com.hussein.ktorClient

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


interface ApiResource {
    val parent: ApiResourceParent?
        get() = null
}