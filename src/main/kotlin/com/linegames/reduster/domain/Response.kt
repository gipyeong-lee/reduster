package com.linegames.reduster.domain

import org.springframework.http.HttpStatus

data class ApiResponse(val message:String,val body: Any?)

data class CommandResponse(val server : String?, val value:String?)
