package net.zicai.component.tool;

/**
 * @author wangdi
 * @date 2026/5/4 15:21
 * @description
 */

import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 天气工具
 * 提供天气查询相关功能（模拟数据）
 */
@Slf4j
@Component
public class WeatherTool {

    private final Random random = new Random();

    /**
     * 获取指定城市的天气信息
     */
    @McpTool(name = "weather_get_current", description = "获取指定城市的当前天气信息")
    public Map<String, Object> getCurrentWeather(
            @McpToolParam(description = "城市名称，如：北京、上海、广州") String city) {

        log.info("mcp-sever getCurrentWeather 被调用 v1-------");
        Map<String, Object> weather = new HashMap<>();
        weather.put("city", city);
        weather.put("temperature", 15 + random.nextInt(20));
        weather.put("humidity", 40 + random.nextInt(40));
        weather.put("windSpeed", 5 + random.nextInt(15));
        weather.put("condition", getRandomCondition());
        weather.put("updateTime", LocalDateTime.now().toString());
        log.info("获取当前天气信息：{}", weather);
        return weather;
    }

    /**
     * 获取未来几天的天气预报
     */
    @McpTool(name = "weather_get_forecast", description = "获取指定城市未来几天的天气预报")
    public Map<String, Object> getWeatherForecast(
            @McpToolParam(description = "城市名称，如：北京、上海、广州") String city,
            @McpToolParam(description = "预报天数，1-7天") int days) {
        log.info("mcp-sever getWeatherForecast 被调用-------");
        if (days < 1 || days > 7) {
            throw new IllegalArgumentException("预报天数必须在1-7天之间");
        }

        Map<String, Object> forecast = new HashMap<>();
        forecast.put("city", city);
        forecast.put("days", days);

        List<Map<String, Object>> dailyForecasts = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            Map<String, Object> dayWeather = new HashMap<>();
            dayWeather.put("day", i + 1);
            dayWeather.put("date", LocalDate.now().plusDays(i).toString());
            dayWeather.put("highTemp", 20 + random.nextInt(15));
            dayWeather.put("lowTemp", 10 + random.nextInt(10));
            dayWeather.put("condition", getRandomCondition());
            dailyForecasts.add(dayWeather);
        }
        forecast.put("forecast", dailyForecasts);
        log.info("获取天气预报：{}", forecast);
        return forecast;
    }

    private String getRandomCondition() {
        String[] conditions = {"晴", "多云", "阴", "小雨", "中雨", "雷阵雨"};
        return conditions[random.nextInt(conditions.length)];
    }
}

