import java.awt.*
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import javax.imageio.ImageIO
import kotlin.random.Random
import kotlin.system.exitProcess
import java.sql.*;
import kotlin.io.path.Path

/*
TODO
 fix positioning (typ klar)
 rotate (typ klar)
 Section täker hela bilden med skriver bara inom sig själv
 transform
 coloration (gradient)
 Remove hard letters
 Add numbers??
 Ability to run without removing previous entrys

 TODO: FIX RANDOM AND PASSWORD

 */
fun main(args: Array<String>) {

    // Arguments
    var n = 4     // Number of images to generate. Default 100
    var dp = true   // Empty primary table before insert. Default true
    var h = false   // Print help message
    var p = "img/"    // Path to directory images will be created inside. Default ./

    // parse arguments
    var i = 0
    while (i < args.size) {
        try {
            when (args[i]) {
                "-h" -> h = true
                "-dp" -> dp = false
                "-n" -> {
                    n = args[i + 1].toInt()
                    // N must be >= 1
                    check(n < 1) { "N must be greater then 0." }
                    i++
                }
                "-p" -> {
                    p = args[i + 1]
                    i++
                }
                else -> throw Exception("Invalid argument.")
            }
        } catch (e: Exception) {
            print("Invalid arguments. Use -h for help.")
            exitProcess(1)
        }
        i++
    }

    // If -h flag was passed print help message
    if (h) printHelp()

    // Create database connection
    Class.forName("org.mariadb.jdbc.Driver")
    val url = "jdbc:mariadb://localhost/CAPTCHA"
    val username = "CAPTCHA"
    val password = "imggen" // TODO: CHANGE TO ENV
    var con: Connection
    try {
        con = DriverManager.getConnection(url, username, password)
    } catch (e: SQLException) {
        print(e.message)
        exitProcess(2)
    }


    // If dp flag was not passed empty table
    if (dp) emptyTable(con)

    // Generate n images and insert them into the table
    generateImages(n, p, con)

}

fun printHelp() {
    print("usage: imggen [-h] [-n] <num> [-dp] \n\nCreate images and insert into database table.\n\nArguments:\n-n\tThe number of integers to be generated.\n-h\tPrint help message.\n-dp\tDo not empty table flag.\n")
}

fun emptyTable(con: Connection) {
    try {
        val query = "DELETE FROM images;"
        val st = con.createStatement()
        val rs = st.execute(query)
        st.close()
    } catch (e: SQLException) {
        print(e.message)
        exitProcess(2)
    }
}

fun generateImages(n: Int, p: String, con: Connection) {
    // Random /// FIX THIS LATER
    val rand = Random

    val imgHeight = 150
    val imgWidth = 300
    val sectorWidth = imgWidth / 4
    val sectorHeight = imgHeight
    val sectorMargin = 4
    val maxFontSize = 66
    val minFontSize = 40
    val fontWidthSub = 25 // Used to try and fix positioning
    val charGenMax = 90
    val charGenMin = 65
    val maxStrokeSize = 6
    val minStrokeSize = 2
    val minLines = 2
    val maxLines = 8
    val maxLinesSector = 2
    val minLinesSector = 1
    val minOval = 1
    val maxOval = 3
    val minOvalWidth = 20
    val maxOvalWidth = 150
    val maxDegree = 30.0

    check(maxFontSize < sectorWidth - 2 * sectorMargin) { "Max font size is to large." }

    check(imgHeight < imgWidth) { "Image must be higher then it is wide" }

    for (i in 0 until n) {
        var text = ""
        // Main image
        val img = BufferedImage(imgWidth, imgHeight, 1)
        val g = img.createGraphics()
        // Set background color
        g.background = Color.WHITE
        g.fill(Rectangle(imgWidth, imgHeight))

        // Sector images
        val s1 = BufferedImage(sectorWidth, sectorHeight, 2)
        val s2 = BufferedImage(sectorWidth, sectorHeight, 2)
        val s3 = BufferedImage(sectorWidth, sectorHeight, 2)
        val s4 = BufferedImage(sectorWidth, sectorHeight, 2)
        val sectors = arrayOf(s1, s2, s3, s4)
        for (sectorImage in sectors) {
            // Get graphics
            val s = sectorImage.createGraphics()

            // Set font
            val fontSize = rand.nextInt(maxFontSize - minFontSize) + minFontSize
            val font = Font("Dialog", Font.PLAIN, fontSize)
            s.font = font

            // Draw text
            val charString = ((rand.nextInt(charGenMax - charGenMin) + charGenMin).toChar()).toString()
            val posX = rand.nextInt(sectorWidth - maxFontSize - 2 * sectorMargin + fontWidthSub) + sectorMargin
            val posY = rand.nextInt(sectorHeight - maxFontSize - 2 * sectorMargin) + sectorMargin + fontSize
            s.color = Color.BLACK
            // Rotate before text draw
            val degree = rand.nextDouble(2 * maxDegree) - maxDegree // Between -maxDegree and +maxDegree
            s.rotate(Math.toRadians(degree), (posX + fontSize / 2).toDouble(), (posY - fontSize / 2).toDouble())

            s.drawString(charString, posX, posY)
            text += charString

            // Draw lines in sector
            val lines = rand.nextInt(maxLinesSector - minLinesSector) + minLinesSector
            for (i in 1..lines) {
                val stokeSize = rand.nextInt(maxStrokeSize - minStrokeSize) + minStrokeSize
                s.color = Color.BLACK
                s.stroke = BasicStroke(stokeSize.toFloat())
                val x1 = rand.nextInt(sectorWidth)
                val y1 = rand.nextInt(sectorHeight)
                val x2 = rand.nextInt(sectorWidth)
                val y2 = rand.nextInt(sectorHeight)
                s.drawLine(x1, y1, x2, y2)
            }

        }

        // Create final image
        g.drawImage(s1, 0, 0, null)
        g.drawImage(s2, sectorWidth, 0, null)
        g.drawImage(s3, 2 * sectorWidth, 0, null)
        g.drawImage(s4, 3 * sectorWidth, 0, null)

        // Draw lines over whole image
        val lines = rand.nextInt(maxLines - minLines) + minLines
        for (i in 1..lines) {
            val stokeSize = rand.nextInt(maxStrokeSize - minStrokeSize) + minStrokeSize
            g.color = Color.BLACK
            g.stroke = BasicStroke(stokeSize.toFloat())
            val x1 = rand.nextInt(imgWidth)
            val y1 = rand.nextInt(imgHeight)
            val x2 = rand.nextInt(imgWidth)
            val y2 = rand.nextInt(imgHeight)
            g.drawLine(x1, y1, x2, y2)
        }

        // Draw ovals
        val ovals = rand.nextInt(maxOval - minOval) + minOval
        for (i in 1..ovals) {
            val stokeSize = rand.nextInt(maxStrokeSize - minStrokeSize) + minStrokeSize
            g.color = Color.BLACK
            g.stroke = BasicStroke(stokeSize.toFloat())
            val x = rand.nextInt(imgWidth)
            val y = rand.nextInt(imgHeight)
            val w = rand.nextInt(maxOvalWidth - minOvalWidth) + minOvalWidth
            val h = rand.nextInt(imgHeight)
            g.drawOval(x, y, w, h)
        }

        // Create file
        val path = Path(p).toAbsolutePath().toString() + "/img$i.png"
        ImageIO.write(img, "png", File(path))

        //Save to database
        try {
            val query = "INSERT INTO images VALUES ($i, '$text', '$path');"
            val st = con.createStatement()
            val rs = st.execute(query)
            st.close()
        } catch (e: SQLException) {
            print(e.message)
            exitProcess(2)
        }
        println("Img Gen")
        println("Solution is $text")
    }
}
