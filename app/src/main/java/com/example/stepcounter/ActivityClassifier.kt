package com.example.stepcounter
import android.content.Context
import java.io.BufferedReader
import java.io.InputStream
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.absoluteValue
import kotlin.math.round

class ActivityClassifier(number:Int) {
    lateinit var RawData:Array<DoubleArray>
    var readalready:Boolean=false

    private fun extractFeatures(dataArray: Array<DoubleArray>): DoubleArray {
        val ax = mutableListOf<Double>()
        val ay = mutableListOf<Double>()
        val az = mutableListOf<Double>()
        val sx = mutableListOf<Double>()
        val sy = mutableListOf<Double>()
        val sz = mutableListOf<Double>()

        for (i in 0 until dataArray.size - 1) {
            if (dataArray[i].size == 3) {
                ax.add(dataArray[i][0])
                ay.add(dataArray[i][1])
                az.add(dataArray[i][2])
                sx.add(dataArray[i][0])
                sy.add(dataArray[i][1])
                sz.add(dataArray[i][2])
            }
        }

        val result:DoubleArray = doubleArrayOf(ax.average(), ay.average(),az.average(),sx.standardDeviation(),sy.standardDeviation(),sz.standardDeviation())
        return result
    }

    fun List<Double>.average(): Double {
        return if (isEmpty()) 0.0 else sum() / size
    }

    fun List<Double>.standardDeviation(): Double {
        val average = average()
        val sumOfSquares = sumOf { (it - average).pow(2) }
        return sqrt(sumOfSquares / size)
    }

    fun ReadRaw(context: Context) {
            if(!readalready) {
                val stream: InputStream = context.resources.openRawResource(R.raw.wsdm)
                stream.reset()
                val reader: BufferedReader = stream.bufferedReader()
                var size: Int = 0
                while (reader.readLine() != null) size += 1
                stream.reset()
                //Log.e("Info", "Found Lines: " +size.toString())


                val array: MutableList<DoubleArray> = mutableListOf<DoubleArray>()
                var indx = 0
                var Lines = reader.readLine()
                while (Lines != null && indx < size) {
                    //Log.e("Info", Lines.toString())
                    val ints: DoubleArray = DoubleArray(4)
                    val lineValues = Lines.split(',')
                    for (x in 2..5) {
                        //Log.e("Info", indx.toString())
                        if (lineValues.size == 6)
                            if (lineValues[x] == "") ints[x - 2] = 0.0
                            else ints[x - 2] = lineValues[x].replace(';', ' ').toDouble()
                    }
                    array.add(ints)
                    indx += 1
                    Lines = reader.readLine()
                }
                RawData = array.toTypedArray()
            }
            readalready=true
        }
    fun Extract(indx:Int):String{
        val i=(indx*100)-1
        var classification="Not Classifiable"
        val oneLine=extractFeatures(RawData.sliceArray(i..i+100))
        val model=Model()
        val result=model.score(oneLine)
        val classes= arrayOf("Downstairs","Jogging","Sitting","Standing","Upstairs","Walking")
        for(x in 0..5)
            if(result[x]==1.0) classification=classes[x]
        return classification
    }

}