package com.example.stepcounter

import android.content.Context
import android.util.Log
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoint
import org.apache.commons.math3.stat.StatUtils
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import java.io.BufferedReader
import java.io.InputStream
import kotlin.math.abs

class StepCounter {
    val steps=0
    val fs = 100.0
    val N = 320
    val ts = 1.25
    val res:Double = (fs / N)
    val ls:Int = (ts * fs).toInt();


    public fun Convert(X: Array<DoubleArray>):Double{
        var i =0
        var stepCount=0.0
        while (i + N < X.size) {
            val subMatrix = X.sliceArray(i until i + N)
            subMatrix.forEach{a-> a.sliceArray(1 until 4)}
            val axisMeans = DoubleArray(subMatrix[0].size) { j ->
                StatUtils.mean(DoubleArray(subMatrix.size) { i -> abs(subMatrix[i][j]) })
            }
            val (maxIndex, maxValue) = axisMeans.withIndex().maxByOrNull{it.value}!!
            Log.e("Info", "The biggest column is: "+ (maxIndex+1).toString())
            val S:DoubleArray = DoubleArray(512)
            var inx=0
            for(c in i until i+N){
                S[inx]=X[c][maxIndex+1]
                inx+=1
            }
            for(c in inx until 511){
                S[inx]=0.0
                inx+=1
            }
            val result: Array<out Complex>? =
                FastFourierTransformer(DftNormalization.STANDARD).transform(S, TransformType.FORWARD)
            if (result != null) {
                for(c in result.indices){
                    S[c]=2* abs(result[c].real)
                }
            }
            Log.e("Info", "Value of S: "+ S[0].toString())
            val w0 = StatUtils.mean(S.sliceArray(0 until 2))
            val wc = StatUtils.mean(S.sliceArray(2 until 7))
            Log.e("Info", "w0: "+ w0.toString() + ", wc: "+ wc.toString())

            val obs: MutableCollection<WeightedObservedPoint> = mutableListOf()
            obs.add(WeightedObservedPoint(1.0,1.0,S[2]))
            obs.add(WeightedObservedPoint(1.0,2.0,S[3]))
            obs.add(WeightedObservedPoint(1.0,3.0,S[4]))
            obs.add(WeightedObservedPoint(1.0,4.0,S[5]))
            obs.add(WeightedObservedPoint(1.0,5.0,S[6]))
            val fitter: PolynomialCurveFitter = PolynomialCurveFitter.create(4)
            val coefficients = fitter.fit(obs)
            val Polynomfun= PolynomialFunction(coefficients)
            val f = { x: Double -> PolynomialFunction(coefficients).derivative().value(x) }
            val maximum = minimize(f,20)
            if ((wc > w0) && (wc > 10)) { //walking condition
                val fw = res * (maximum) //calculate frequency from maximum - warum +1?
                val c = ts * fw //steps in current window
                stepCount += c
                Log.e("Info", "Fw result: " + fw.toString() + ": "+res.toString() + "*" + maximum.toString())
            }

            i+=ls
        }
        return stepCount
    }

    private fun minimize(f:(Double)->Double,steps:Int):Double{
        val array:DoubleArray=DoubleArray(5*steps)
        val Iarray:DoubleArray=DoubleArray(5*steps)
        val increment:Double= (1.0/steps)
        var idx=0
        var i=1.0
        while(idx<array.size){
            array[idx]=if (f(i) > 0) f(i) else f(i)*(-1)
            Iarray[idx]=i
            i+=increment
            idx+=1
        }

        return Iarray[array.indexOfFirst { v->v==array.min() }]
    }

    public fun ReadRaw(context: Context):Array<DoubleArray> {

        val stream: InputStream =context.resources.openRawResource(R.raw.data)
        stream.reset()
        val reader: BufferedReader =stream.bufferedReader()
        var size:Int=0
        while(reader.readLine()!=null) size+=1
        stream.reset()
        //Log.e("Info", "Found Lines: " +size.toString())


        val array: MutableList<DoubleArray> = mutableListOf<DoubleArray>()
        var indx=0
        var Lines = reader.readLine()
        while(Lines!=null && indx<size) {
            //Log.e("Info", Lines.toString())
            var ints: DoubleArray = DoubleArray(4)
            var lineValues = Lines.split(',')
            for (x in 0..3) {
                //Log.e("Info", indx.toString())
                if (lineValues.isNotEmpty()) ints[x] = lineValues[x].toDouble()
            }
            array.add(ints)
            indx+=1
            Lines = reader.readLine()
        }
        return array.toTypedArray()
    }
}