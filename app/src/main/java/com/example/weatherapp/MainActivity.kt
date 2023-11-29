package com.example.weatherapp

import android.graphics.Color
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Data ko Fetch karny k liye
        fetchDataWeather("Attock City")
        setSearchCityListener()
    }

    private fun setSearchCityListener() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    fetchDataWeather(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle text change if needed
                return true
            }
        })
    }

    private fun fetchDataWeather(city: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        val response = retrofit.getWeatherData(city, "e87152cc4ee1d143727b88008658ccc8", "metric")

        // callback ka matlb k data kaha se laye ga
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                // check karna h k response aa rha h ya nahi
                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    updateUI(responseBody, city)
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                // Handle failure if needed
                // TODO: Implement appropriate error handling
            }
        })
    }

    private fun updateUI(responseBody: WeatherApp, city: String) {
        // Extract weather details from the response
        val temperature = responseBody.main.temp.toString()
        val humidity = responseBody.main.humidity
        val windspeed = responseBody.wind.speed
        val sunRise = responseBody.sys.sunrise.toLong()
        val sunSet = responseBody.sys.sunset.toLong()
        val seaLevel = responseBody.main.pressure
        val condition = responseBody.weather.firstOrNull()?.main ?: "unknown"
        val maxTemp = responseBody.main.temp_max
        val minTemp = responseBody.main.temp_min

        // Update UI elements with weather details
        binding.temp.text = "$temperature °C"
        binding.weather.text=condition
        binding.humidilty.text = "$humidity %"
        binding.windspeed.text = "$windspeed m/s"
        binding.sunrise.text = "${time(sunRise)}"
        binding.sunset.text = "${time(sunSet)}"
        binding.sea.text = "$seaLevel hPa"
        binding.condition.text = condition
        binding.maxTemp.text = "$maxTemp °C"
        binding.minTemp.text = "$minTemp °C"
        binding.city.text = "$city"
        binding.day.text = dayName(System.currentTimeMillis())
        binding.date.text = date()

        changeImageAccordingToWeatherCondition(condition)
    }

    private fun changeImageAccordingToWeatherCondition(conditions: String) {
        when (conditions) {
            "Clear Sky","Sunny","Clear" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Partly Clouds","Clouds","Overcast","Mist","Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain","Drizzle","Moderate Rain","Showers","Heavy Rain","Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow","Moderate Snow","Heavy Snow","Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }

            // munich naples
            else ->{
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp*1000))
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
