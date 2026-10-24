package com.zjude.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.zjude.system.domain.ZjudgBookingSlot;
import com.zjude.system.domain.ZjudgService;
import com.zjude.system.mapper.ZjudgBookingSlotMapper;
import com.zjude.system.mapper.ZjudgServiceMapper;
import com.zjude.system.service.ISysConfigService;

/**
 * 严格城市过滤（zjudg.city.strict）的分支测试。
 */
@ExtendWith(MockitoExtension.class)
class ZjudgCityScopeServiceImplTest
{
    private static final String CONFIG_STRICT = "zjudg.city.strict";

    private static final String CITY_ID = "6107";

    @Mock
    private ZjudgServiceMapper serviceMapper;

    @Mock
    private ZjudgBookingSlotMapper slotMapper;

    @Mock
    private ISysConfigService configService;

    @InjectMocks
    private ZjudgCityScopeServiceImpl service;

    @Test
    void strictIsOffByDefault()
    {
        when(configService.selectConfigByKey(CONFIG_STRICT)).thenReturn(null);

        assertThat(service.strictFilterEnabled()).isFalse();
    }

    @Test
    void strictIsEnabledOnlyForOne()
    {
        when(configService.selectConfigByKey(CONFIG_STRICT)).thenReturn("1");

        assertThat(service.strictFilterEnabled()).isTrue();
    }

    @Test
    void servicesUseLooseQueryWhenStrictOff()
    {
        when(configService.selectConfigByKey(CONFIG_STRICT)).thenReturn("0");
        ZjudgService query = serviceQuery();
        List<ZjudgService> expected = List.of(new ZjudgService());
        when(serviceMapper.selectList(query)).thenReturn(expected);

        assertThat(service.selectServices(query)).isSameAs(expected);
        verify(serviceMapper, never()).selectListByCity(any());
    }

    @Test
    void servicesUseCityOnlyQueryWhenStrictOn()
    {
        when(configService.selectConfigByKey(CONFIG_STRICT)).thenReturn("1");
        ZjudgService query = serviceQuery();
        List<ZjudgService> expected = List.of(new ZjudgService());
        when(serviceMapper.selectListByCity(query)).thenReturn(expected);

        assertThat(service.selectServices(query)).isSameAs(expected);
        verify(serviceMapper, never()).selectList(any());
    }

    @Test
    void servicesIgnoreCityScopeWhenCityMissing()
    {
        ZjudgService query = new ZjudgService();
        query.setStatus("0");
        List<ZjudgService> expected = List.of(new ZjudgService());
        when(serviceMapper.selectList(query)).thenReturn(expected);

        assertThat(service.selectServices(query)).isSameAs(expected);
        // 未选城市时不读开关，也不加城市条件
        verify(configService, never()).selectConfigByKey(CONFIG_STRICT);
        verify(serviceMapper, never()).selectListByCity(any());
    }

    @Test
    void slotsUseLooseQueryWhenStrictOff()
    {
        when(configService.selectConfigByKey(CONFIG_STRICT)).thenReturn("0");
        ZjudgBookingSlot query = slotQuery();
        List<ZjudgBookingSlot> expected = List.of(new ZjudgBookingSlot());
        when(slotMapper.selectList(query)).thenReturn(expected);

        assertThat(service.selectSlots(query)).isSameAs(expected);
        verify(slotMapper, never()).selectListByCity(any());
    }

    @Test
    void slotsUseCityOnlyQueryWhenStrictOn()
    {
        when(configService.selectConfigByKey(CONFIG_STRICT)).thenReturn("1");
        ZjudgBookingSlot query = slotQuery();
        List<ZjudgBookingSlot> expected = List.of(new ZjudgBookingSlot());
        when(slotMapper.selectListByCity(query)).thenReturn(expected);

        assertThat(service.selectSlots(query)).isSameAs(expected);
        verify(slotMapper, never()).selectList(any());
    }

    @Test
    void slotsIgnoreCityScopeWhenCityMissing()
    {
        ZjudgBookingSlot query = new ZjudgBookingSlot();
        query.setStatus("0");
        List<ZjudgBookingSlot> expected = List.of(new ZjudgBookingSlot());
        when(slotMapper.selectList(query)).thenReturn(expected);

        assertThat(service.selectSlots(query)).isSameAs(expected);
        verify(slotMapper, never()).selectListByCity(any());
    }

    private ZjudgService serviceQuery()
    {
        ZjudgService query = new ZjudgService();
        query.setStatus("0");
        query.setCityId(CITY_ID);
        return query;
    }

    private ZjudgBookingSlot slotQuery()
    {
        ZjudgBookingSlot query = new ZjudgBookingSlot();
        query.setStatus("0");
        query.setCityId(CITY_ID);
        return query;
    }
}
