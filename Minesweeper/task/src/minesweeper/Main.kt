package minesweeper

import java.util.Scanner
import kotlin.random.Random

val fieldW = 9
val fieldH = 9

class Point(val x:Int, val y:Int)

enum class Location {
    LeftUpper,
    RightUpper,
    LeftLower,
    RightLower,
    Left,
    Right,
    Top,
    Bottom,
    Middle
}

fun countMinesForLocation(x:Int, y:Int, field: Map<Int, String>): Int {
    val places = mapOf(
            Location.LeftUpper to arrayOf(
                Point(1, 0), Point(1, 1), Point(0, 1)
            ),
            Location.RightUpper to arrayOf(
                    Point(-1, 0), Point(-1, 1), Point(0, 1)
            ),
            Location.LeftLower to arrayOf(
                    Point(1, 0), Point(1, -1), Point(0, -1)
            ),
            Location.RightLower to arrayOf(
                    Point(-1, 0), Point(-1, -1), Point(0, -1)
            ),
            Location.Left to arrayOf(
                    Point(0, -1), Point(1, -1), Point(1, 0),
                    Point(1, 1), Point(0, 1)
            ),
            Location.Right to arrayOf(
                    Point(0, -1), Point(-1, -1), Point(-1, 0),
                    Point(-1, 1), Point(0, 1)
            ),
            Location.Top to arrayOf(
                    Point(-1, 0), Point(-1, 1), Point(0, 1),
                    Point(1, 1), Point(1, 0)
            ),
            Location.Bottom to arrayOf(
                    Point(-1, 0), Point(-1, -1), Point(0, -1),
                    Point(1, -1), Point(1, 0)
            ),
            Location.Middle to arrayOf(
                    Point(-1, 0), Point(-1, -1), Point(0, -1),
                    Point(1, -1), Point(1, 0), Point(1, 1),
                    Point(0, 1), Point(-1, 1)
            )
    )
    val key = when {
        x == 0 && y == 0 -> Location.LeftUpper
        x == fieldW -1 && y == 0 -> Location.RightUpper
        x == 0 && y == fieldH - 1 -> Location.LeftLower
        x == fieldW -1 && y == fieldH - 1 -> Location.RightLower
        x > 0 && x < fieldW - 1 && y == 0 -> Location.Top
        x > 0 && x < fieldW - 1 && y == fieldH - 1 -> Location.Bottom
        x == 0 && y > 0 && y < fieldH - 1 -> Location.Left
        x == fieldW - 1 && y > 0 && y < fieldH - 1 -> Location.Right
        else -> Location.Middle
    }

    val placeList = places[key]
    if (placeList != null)
        return placeList.count { p -> checkMine(x + p.x, y + p.y, field) }
    else
        return -1
}

fun markMineCounts(field: MutableMap<Int, String>) {
    for (x in 0 until fieldW) {
        for (y in 0 until fieldH) {
            if (checkMine(x, y, field))
                continue
            val mines = countMinesForLocation(x, y, field)
            if (mines > 0)
                placeThing(x, y, mines.toString()[0], field)
        }
    }
}

fun initField(w:Int, h:Int):MutableMap<Int, String> {
    var field = mutableMapOf<Int, String>()
    val s = CharArray(w) { '.' }
    for (y in 0..h-1) {
        field[y] = String(s)
    }
    return field
}

fun placeThing(x:Int, y:Int, thing:Char, field:MutableMap<Int, String>) {
    val line = field[y]!!
    var ar = line.toCharArray()
    ar[x] = thing
    field[y] = String(ar)
}

fun copyThing(x:Int, y:Int, src:Map<Int, String>, target: MutableMap<Int, String>) {
    val srcline = src[y]!!
    val thing = srcline.toCharArray()[x]
    val targetLine = target[y]!!
    var targetThing = targetLine.toCharArray()
    targetThing[x] = thing
    target[y] = String(targetThing)
}

fun checkThing(x:Int, y:Int, field:Map<Int, String>, things: String): Boolean {
    val line = field[y]!!
    var ar = line.toCharArray()
    return ar[x] in things
}

fun checkMine(x:Int, y:Int, field:Map<Int, String>):Boolean {
    return checkThing(x, y, field, "X")
}

fun checkNumber(x:Int, y:Int, field:Map<Int, String>):Boolean {
    return checkThing(x, y, field, "0123456789")
}

fun checkFree(x:Int, y:Int, field:Map<Int, String>):Boolean {
    return checkThing(x, y, field, ".")
}

fun printField(field:MutableMap<Int, String>) {
    println(" |123456789|")
    println("-|---------|")
    for ((num, line) in field) {
        val l = line.replace('X', '.')
        println("${num+1}|$l|")
    }
    println("-|---------|")
}

fun placeMines(mines:Int, field:MutableMap<Int, String>):List<Point> {
    var mineCounter = mines
    var minePositions = mutableListOf<Point>()
    while (mineCounter > 0) {
        while (true) {
            var x = Random.nextInt(0, fieldW)
            var y = Random.nextInt(0, fieldH)
            if (!checkMine(x, y, field)) {
                placeThing(x, y, 'X', field)
                minePositions.add(Point(x, y))
                break
            }
        }
        mineCounter--
    }
    return minePositions
}

fun markLocation(x: Int, y: Int, field:MutableMap<Int, String>) {
    val line = field[y]!!
    var ar = line.toCharArray()
    if (ar[x] == '*')
        placeThing(x, y, '.', field)
    else
        placeThing(x, y, '*', field)
}

fun checkWin(field: Map<Int, String>, minePositions: List<Point>): Boolean {
    var marks = 0
    for (x in 0 until fieldW) {
        for (y in 0 until fieldH) {
            if (checkThing(x, y, field, "*")) {
                marks++
                if (minePositions.all{ p -> p.x != x && p.y != y }) {
                    return false
                }
            }
        }
    }
    return minePositions.count() == marks
}

fun markMine(x: Int, y: Int, field: MutableMap<Int, String>) {
    markLocation(x, y, field)
}

fun recursiveMarker(x: Int, y: Int, field: MutableMap<Int, String>, mineField: Map<Int, String>,
                    visited: MutableList<Point>) {
    if (x < 0 || x >= fieldW || y < 0 || y >= fieldH)
        return
    if (visited.any { p -> p.x == x && p.y == y })
        return

    if (checkNumber(x, y, mineField)) {
        copyThing(x, y, mineField, field)
        visited.add(Point(x, y))
        return
    }

    if (checkFree(x, y, mineField)) {
        placeThing(x, y, '/', field)
        visited.add(Point(x, y))
    }

    if (checkThing(x, y, field, "*") && !checkMine(x, y, mineField)) {
        placeThing(x, y, '/', field)
        visited.add(Point(x, y))
    }

    if (checkThing(x, y, field, "*") && checkMine(x, y, mineField)) {
        //placeThing(x, y, '/', field)
        visited.add(Point(x, y))
        return
    }

    for (dx in -1 until 2) {
        for (dy in -1 until 2) {
            if (dx == 0 && dy == 0)
                continue
            recursiveMarker(x + dx, y - dy, field, mineField, visited)
        }
    }
}

fun markFree(x: Int, y: Int, field: MutableMap<Int, String>, mineField: Map<Int, String>,
             minePositions: List<Point>): Boolean {
    if (minePositions.any { p -> p.x == x && p.y == y})
        return false

    var visited = mutableListOf<Point>()
    recursiveMarker(x, y, field, mineField, visited)

    return true
}

fun main() {
    val scanner = Scanner(System.`in`)
    println("How many mines do you want on the field?")
    var mineCount = scanner.nextInt()
    var field = initField(fieldW, fieldH)
    var expfield = field.toMutableMap()
    var minePositions = placeMines(mineCount, field)
    markMineCounts(field)
    printField(expfield)
    while (true) {
        println("Set/delete mine marks (x and y coordinates):")
        val x = scanner.nextInt() - 1
        val y = scanner.nextInt() - 1
        val action = scanner.next()
        if (x < 0 || x >= fieldW || y < 0 || y >= fieldH) {
            println("Bad coordinates, please try again.")
            continue
        }
        if (action == "mine") {
            markMine(x, y, expfield)
        }
        if (action == "free") {
            if (!markFree(x, y, expfield, field, minePositions)) {
                println("You stepped on a mine and failed!")
                break
            }
        }
        printField(expfield)
        if (checkWin(expfield, minePositions)) {
            println("Congratulations! You found all the mines!")
            break
        }
    }
}
