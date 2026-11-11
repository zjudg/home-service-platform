package com.zjude.settings.service.impl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Duration;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.zjude.common.exception.ServiceException;
import com.zjude.integrated.map.MapConfigService;
import com.zjude.integrated.map.MapRuntimeConfig;
import com.zjude.settings.domain.MiniLocationInfo;

/**
 * 天地图逆地理编码客户端。Key 只在服务端读取，避免随小程序代码下发。
 */
@Service
public class TiandituGeocoder
{
    private static final Logger log = LoggerFactory.getLogger(TiandituGeocoder.class);
    private static final String GEOCODER_URL = "https://api.tianditu.gov.cn/geocoder";

    @Resource
    private MapConfigService mapConfigService;

    @Resource
    private RestTemplateBuilder restTemplateBuilder;

    public MiniLocationInfo reverseGeocode(BigDecimal longitude, BigDecimal latitude)
    {
        validateCoordinates(longitude, latitude);
        MapRuntimeConfig config = mapConfigService.getRuntimeConfig();
        String key = config == null ? "" : config.tiandituKey();
        if (key == null || key.isBlank())
        {
            throw new ServiceException("后台尚未配置天地图 Key，请先完成天地图设置");
        }

        JSONObject post = new JSONObject();
        post.put("lon", longitude);
        post.put("lat", latitude);
        post.put("ver", 1);
        URI uri = UriComponentsBuilder.fromUriString(GEOCODER_URL)
                .queryParam("postStr", JSON.toJSONString(post))
                .queryParam("type", "geocode")
                .queryParam("tk", key)
                .build()
                .encode()
                .toUri();

        String body;
        try
        {
            RestTemplate client = restTemplateBuilder
                    .setConnectTimeout(Duration.ofSeconds(3))
                    .setReadTimeout(Duration.ofSeconds(5))
                    .build();
            body = client.getForObject(uri, String.class);
        }
        catch (RestClientResponseException exception)
        {
            int status = exception.getStatusCode().value();
            log.warn("天地图逆地理编码服务返回 HTTP {}", status);
            throw new ServiceException("天地图服务请求失败（HTTP " + status + "），请检查服务访问权限或稍后重试");
        }
        catch (ResourceAccessException exception)
        {
            log.warn("无法连接天地图逆地理编码服务，异常类型：{}", exception.getClass().getSimpleName());
            throw new ServiceException("后端无法连接天地图服务，请检查服务器网络或稍后重试");
        }
        catch (RuntimeException exception)
        {
            log.warn("天地图逆地理编码请求失败，异常类型：{}", exception.getClass().getSimpleName());
            throw new ServiceException("天地图服务请求异常，请检查后端服务配置或稍后重试");
        }

        try
        {
            return parseLocation(body);
        }
        catch (ServiceException exception)
        {
            throw exception;
        }
        catch (RuntimeException exception)
        {
            log.warn("天地图逆地理编码响应解析失败，异常类型：{}", exception.getClass().getSimpleName());
            throw new ServiceException("天地图返回数据格式异常，请稍后重试");
        }
    }

    void validateCoordinates(BigDecimal longitude, BigDecimal latitude)
    {
        if (longitude == null || latitude == null
                || longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || longitude.compareTo(BigDecimal.valueOf(180)) > 0
                || latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || latitude.compareTo(BigDecimal.valueOf(90)) > 0)
        {
            throw new ServiceException("定位坐标无效");
        }
    }

    MiniLocationInfo parseLocation(String body)
    {
        JSONObject payload = JSON.parseObject(body);
        JSONObject result = payload == null ? null : payload.getJSONObject("result");
        JSONObject address = result == null ? null : result.getJSONObject("addressComponent");
        if (address == null)
        {
            throw new ServiceException("天地图未能识别当前位置，请稍后重试");
        }

        String province = text(address, "province");
        String city = text(address, "city");
        String county = text(address, "county");
        if (city.isBlank())
        {
            city = isMunicipality(province) ? province : county;
        }
        if (city.isBlank())
        {
            city = province;
        }
        if (city.isBlank())
        {
            throw new ServiceException("天地图未能识别当前位置所属城市");
        }

        MiniLocationInfo location = new MiniLocationInfo();
        location.setProvinceName(province);
        location.setCityName(city);
        location.setDistrictName(county);
        location.setFormattedAddress(text(result, "formatted_address"));
        return location;
    }

    private String text(JSONObject object, String key)
    {
        String value = object.getString(key);
        return value == null ? "" : value.trim();
    }

    private boolean isMunicipality(String province)
    {
        return "北京市".equals(province) || "天津市".equals(province)
                || "上海市".equals(province) || "重庆市".equals(province);
    }
}
