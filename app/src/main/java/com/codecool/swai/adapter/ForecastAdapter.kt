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
    private var tempUnit = "Celsius"

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
        val dayAndWeatherType = currentForecast.getDay() + "  •  " + holder.context.getString(currentForecast.getWeatherType())
        holder.forecastIcon.setAnimation(currentForecast.getWeatherIcon())
        holder.forecastDayAndType.text = dayAndWeatherType
        holder.forecastMinMaxTemp.text = currentForecast.temp.getMinMaxTemp(tempUnit)
    }

    override fun getItemCount(): Int {
        return forecastData.size
    }

    fun setForecastData(tempUnit: String, forecastData: List<WeatherForecast.Forecast>) {
        this.forecastData = forecastData
        this.tempUnit = tempUnit
        notifyItemRangeChanged(0, itemCount)
    }

    fun updateForecastTemp(tempUnit: String) {
        if (this.tempUnit != tempUnit) {
            this.tempUnit = tempUnit
            notifyItemRangeChanged(0, itemCount)
        }
    }
}