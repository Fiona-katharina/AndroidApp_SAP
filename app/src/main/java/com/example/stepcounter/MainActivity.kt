package com.example.stepcounter

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.fitting.PolynomialCurveFitter
import org.apache.commons.math3.fitting.WeightedObservedPoint
import org.apache.commons.math3.fitting.WeightedObservedPoints
import org.apache.commons.math3.optim.SimpleBounds
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.BOBYQAOptimizer
import org.apache.commons.math3.stat.StatUtils.mean
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import org.w3c.dom.Text
import java.io.BufferedReader
import java.io.InputStream
import kotlin.math.abs


class MainActivity : ComponentActivity() {
    lateinit var Activity:Activities
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ChangeText(context = this) } }
    }

    @Composable
    fun Text(t: String) {
        Row {
            androidx.compose.material3.Text(text = t)
        }
    }

    @Composable
    fun ChangeText(context: Context) {
        var text by remember { mutableStateOf("Click a button") }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text)

            Button(onClick = { text = count_Steps(context) }) {
                Text("Count steps")
            }
            Button(onClick = { text = classify_Activity(context) }) {
                Text("Classify a random Subject")
            }
            LazyColumn() {
                items(Activity.getActivities()){activity-> Text(activity)}
            }
        }
    }


    fun count_Steps(context: Context): String {
        val StepCounter = StepCounter()
        return StepCounter.Convert(StepCounter.ReadRaw(context)).toString()
    }

    fun classify_Activity(context: Context): String {
        val classifier = ActivityClassifier(100)
        classifier.ReadRaw(context)
        val a=classifier.Extract((0..10982).random())
        Activity.addActivity(a)
        return a
    }
}