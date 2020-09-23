package com.codecool.swai.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.codecool.swai.R
import com.codecool.swai.model.WeatherForecast
import kotlinx.android.synthetic.main.forecast_item.view.*

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ViewHolder>() {

    private var forecastData : List<WeatherForecast.Forecast> = ArrayList()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context
        val forecastIcon: LottieAnimationView = itemView.forecastWeatherLogo
        val forecastDayAndType: TextView = itemView.forecastDayAndType
        val forecastMinMaxTemp: TextView = itemView.forecastMinMaxTemp
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.forecast_item, parent, false)
        return ViewHolder(itemView)
    }

    @ExperimentalStdlibApi
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentForecast = forecastData[position]
        val dayAndWeatherType = currentForecast.getDay() + "  •  " + holder.context.getString(currentForecast.weather[0].getWeatherType())
        holder.forecastIcon.setAnimation(currentForecast.weather.first().getWeatherIcon())
        holder.forecastDayAndType.text = dayAndWeatherType
        holder.forecastMinMaxTemp.text = currentForecast.temp.getMinMaxTempCelsius()
    }

    override fun getItemCount(): Int {
        return forecastData.size
    }

    fun setForecastData(forecastData: List<WeatherForecast.Forecast>) {
        this.forecastData = forecastData
        notifyDataSetChanged()
    }
}