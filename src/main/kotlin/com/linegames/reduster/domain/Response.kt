package com.linegames.reduster.domain

import org.springframework.http.HttpStatus

data class ApiResponse(val message:String,val body: Any?)

data class CommandResponse(val server : Int, val value:String?)

data class ServerInfoResponse(val uri:String,val port: Int, val hashKey: String)
