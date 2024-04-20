package com.example.degrees

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.CartesianChartHost
import com.patrykandpatrick.vico.compose.chart.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.chart.rememberCartesianChart
import com.patrykandpatrick.vico.core.model.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.model.lineSeries
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.navigation.NavController



class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var db: DegreeDB
    private lateinit var view: DegView
    private lateinit var sensorManager: SensorManager

    private var accelerometer: Sensor? = null
    private var magnet: Sensor? = null

    private var accX by mutableStateOf(0f)
    private var accY by mutableStateOf(0f)
    private var accZ by mutableStateOf(0f)
    private var lastUpdate: Long = 0;


    private var DBool by mutableStateOf(0)

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = DegreeDB.getInstance(applicationContext)
        view = DegView(db.Dao())
        setContent {
            MyApp()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        magnet?.let{
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event?.sensor?.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        accZ = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        accX = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
        accY = Math.toDegrees(orientationAngles[2].toDouble()).toFloat()

        if (DBool == 1) {
            event?.let {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastUpdate) >= 200) {
                    lastUpdate = currentTime
                    val currentTime = getCurrentTime()

                    val temp = degreeData(
                        id = 0,
                        time = currentTime,
                        xAxis = accX,
                        yAxis = accY,
                        zAxis = accZ
                    )
                    view.insertData(temp)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }


    @Composable
    fun MyApp() {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "main") {
            composable(route = "main") {
                mainscreen(navController);
            }
            composable(route = "plot_screen"){
                view.Plot()
            }
        }
    }

    @Composable
    fun mainscreen(navController: NavController){
        MaterialTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                SensorOrientationDisplay(accX, accY, accZ, navController)
            }
        }
    }



    @Composable
    fun SensorOrientationDisplay(X: Float, Y: Float, Z: Float, navController : NavController) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "X Axis: ${"%.2f".format(X)}°")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Y Axis: ${"%.2f".format(Y)}°")
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Z Axis: ${"%.2f".format(Z)}°")
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { if (DBool == 0) DBool = 1
                          else if (DBool == 1) DBool = 0},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .padding(start = 24.dp, end = 24.dp)
            ) {
                Text(if (DBool == 0) "Fill Database"
                else "Stop Filling")
            }
            Button(
                onClick = { DBool = 0
                          view.deleteData()},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .padding(start = 24.dp, end = 24.dp)
            ) {
                Text("Empty Database")
            }
            Button(
                onClick = { DBool = 0
                    navController.navigate("plot_screen")},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .padding(start = 24.dp, end = 24.dp)
            ) {
                Text("Display Graphs")
            }
            Button(
                onClick = { DBool = 0
                    view.extractFile(context = applicationContext)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .padding(start = 24.dp, end = 24.dp)
            ) {
                Text("Get File")
            }
        }
    }
}

fun getCurrentTime(): String {
    val currentTimeMillis = System.currentTimeMillis()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormat.format(currentTimeMillis)
}
