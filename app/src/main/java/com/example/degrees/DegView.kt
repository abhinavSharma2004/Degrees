package com.example.degrees

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.lineSeries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

class DegView(private val dao: DegreeDao) : ViewModel() {

    var datas: List<degreeData>? = null
    var Alldatas: List<degreeData>? = null

    fun insertData(temp: degreeData) {
        viewModelScope.launch {
            dao.addData(temp)
        }
    }

    fun deleteData() {
        viewModelScope.launch {
            dao.deleteData()
        }
    }

    fun getit() {
        viewModelScope.launch {
            datas = dao.alldata()
            Log.d("viewAll", datas!![0].time)
        }
    }

    fun getit50() {
        viewModelScope.launch {
            datas = dao.alldata50()
            Log.d("view", datas!![0].time)
        }
    }

    @Composable
    fun Plot() {
        getit50()
        datas?.let { degrees ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (degrees.isNotEmpty()) {
                    GraphIT(degrees.map { it.xAxis }, Color.Red)
                } else {
                    Text("Data is empty")
                }
                Text(text = "X Axis")
                Spacer(modifier = Modifier.height(8.dp))

                if (degrees.isNotEmpty()) {
                    GraphIT(degrees.map { it.yAxis }, Color.Blue)
                } else {
                    Text("Data is empty")
                }
                Text(text = "Y Axis")
                Spacer(modifier = Modifier.height(8.dp))

                if (degrees.isNotEmpty()) {
                    GraphIT(degrees.map { it.zAxis }, Color.Black)
                } else {
                    Text("Data is empty")
                }
                Text(text = "Z Axis")
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    @Composable
    fun GraphIT(dataList: List<Float>, colorlot : androidx.compose.ui.graphics.Color) {
        Box(modifier = Modifier
            .padding(8.dp)
            .background(color = colorlot, shape = RectangleShape)) {
            val modelProducer = remember { CartesianChartModelProducer.build() }
            LaunchedEffect(Unit) { modelProducer.tryRunTransaction { lineSeries { series(dataList) } } }
            CartesianChartHost(rememberCartesianChart(
                    rememberLineCartesianLayer(),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis(),
                ), modelProducer,)
        }

    }

    fun extractFile(context: Context) {
        getit()
        val stringBuilder = StringBuilder()
        val directory = context.getExternalFilesDir(null)
        val outputFile = File(directory, "Deg_output.txt")

        // Use safe call operator to access datas
        datas?.let { safeDatas ->
            for (data in safeDatas) {
                stringBuilder.append("${data.time},${data.xAxis},${data.yAxis},${data.zAxis}\n")
            }

            val fileOutputStream = FileOutputStream(outputFile)
            fileOutputStream.write(stringBuilder.toString().toByteArray())
            fileOutputStream.close()
            Toast.makeText(context, "file transferred", Toast.LENGTH_SHORT).show()
        } ?: Log.e("getit", "datas is null")
    }
}