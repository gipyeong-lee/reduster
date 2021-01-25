package com.linegames.reduster

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RedusterApplication

fun main(args: Array<String>) {
	runApplication<RedusterApplication>(*args)
}
