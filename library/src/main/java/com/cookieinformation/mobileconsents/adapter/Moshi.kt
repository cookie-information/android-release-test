package com.cookieinformation.mobileconsents.adapter

import com.squareup.moshi.Moshi

internal val moshi = Moshi.Builder().add(uuidAdapter).build()
