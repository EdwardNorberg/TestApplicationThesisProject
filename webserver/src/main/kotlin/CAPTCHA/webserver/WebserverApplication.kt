package CAPTCHA.webserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.random.Random
import kotlin.system.exitProcess

@SpringBootApplication
class WebserverApplication

var list = ArrayList<String>()
var con: Connection? = null // database connection

fun main(args: Array<String>) {
    // Create database connection
    Class.forName("org.mariadb.jdbc.Driver")
    val url = "jdbc:mariadb://localhost/CAPTCHA"
    val username = "CAPTCHA"
    val password = "imggen" // TODO: CHANGE TO ENV
    try {
        con = DriverManager.getConnection(url, username, password)
    } catch (e: SQLException) {
        print(e.message)
        exitProcess(2)
    }
    runApplication<WebserverApplication>(*args)
}

@RestController
class CAPTCHAResource(val service: CAPTCHAService) {

    @GetMapping("/")
    fun index(): CAPTCHA = CAPTCHA(Random.nextInt(100), "abc")

    @GetMapping("/img")
    fun getImage(@RequestParam id: String): ResponseEntity<ByteArrayResource> {
       // Database request




        val file = File("img0.png")
        val path=  Paths.get(file.absolutePath)

        val resource: ByteArrayResource = ByteArrayResource(Files.readAllBytes(path))

        list.add(id)
        println("GET $id")

        return ResponseEntity.ok()
            .header("")
            .contentLength(file.length())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource)
    }

    @GetMapping("/solve")
    fun postSolution(@RequestParam id: String, solution: String) {
        service.post(id, solution)
    }
}

data class CAPTCHA(val id: Int, val image: String)
data class Solution(val solution: String)

@Service
class CAPTCHAService( ) {

    fun post(id: String, solution: String) {
        println("POST $solution $id")
        list.remove(id)
    }
}