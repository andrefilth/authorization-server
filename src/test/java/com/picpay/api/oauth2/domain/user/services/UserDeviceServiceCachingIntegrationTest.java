/*
 * @(#)UserDeviceServiceTest.java 1.0 05/11/2020
 *
 * Copyright (c) 2020, PicPay S.A. All rights reserved.
 * PicPay S.A. proprietary/confidential. Use is subject to license terms.
 */

package com.picpay.api.oauth2.domain.user.services;

import com.picpay.api.oauth2.BaseTest;
import com.picpay.api.oauth2.domain.user.document.Device;
import com.picpay.api.oauth2.domain.user.document.UserDeviceDocument;
import com.picpay.api.oauth2.domain.user.repository.UserDeviceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.picpay.api.oauth2.domain.user.document.DeviceOs.ANDROID;
import static com.picpay.api.oauth2.domain.user.document.DeviceOs.IOS;
import static java.util.Optional.*;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

/**
 * Class comments go here...
 *
 * @author André Franco
 * @version 1.0 05/11/2020
 */
@ExtendWith(SpringExtension.class)
//@SpringBootTest(classes = OAuth2ServerApplication.class)
class UserDeviceServiceCachingIntegrationTest extends BaseTest {


    @Autowired
    UserDeviceService deviceService;


    @Autowired
    CacheManager cacheManager;


    private final UserDeviceDocument andreDevices = UserDeviceDocument
        .builder()
        .consumerId(andreAndroid.getConsumerId())
        .devices(Set.of(Device
            .builder()
            .deviceId(andreAndroid.getDeviceId())
            .installationId(andreAndroid.getInstallationId())
            .androidId(andreAndroid.getAndroidId())
            .deviceModel(andreAndroid.getDeviceModel())
            .deviceOs(ANDROID)
            .build()))
        .createAt(LocalDateTime.now())
        .build();
    private final UserDeviceDocument joaoDevices = UserDeviceDocument
        .builder()
        .consumerId("17")
        .devices(Set.of(Device
            .builder()
            .deviceId("joao-device")
            .installationId("joao-installation")
            .deviceModel("Iphone")
            .deviceOs(IOS)
            .build()))
        .createAt(LocalDateTime.now())
        .build();


    @BeforeEach
    public void setUp(){
        deviceRepository.save(andreDevices);
        deviceRepository.save(joaoDevices);
    }

    @DisplayName("Dado uma consulta ao banco de dados do conumer, cachear a informação no redis")
    @Test
    public void testcaching(){

        final UserDeviceDocument deviceDocument = deviceService.findDeviceByConsumerId(andreAndroid.getConsumerId());
        final Optional<UserDeviceDocument> usersDevices = getCachedUsersDevices(deviceDocument.getConsumerId());
        assertThat(of(deviceDocument)).isEqualTo(usersDevices);


    }
    @DisplayName("Dado uma consulta ao banco de dados do conumer, cachear a informação no redis e na segunda consulta não ir no banco de dados")
    @Test
    public void testcaching2(){


        final UserDeviceDocument deviceDocument = deviceService.findDeviceByConsumerId(andreAndroid.getConsumerId());

        deviceService.findDeviceByConsumerId(andreAndroid.getConsumerId());
        final Optional<UserDeviceDocument> usersDevices = getCachedUsersDevices(deviceDocument.getConsumerId());
        assertThat(of(deviceDocument)).isEqualTo(usersDevices);
//        verify(deviceRepository, times(1)).findByConsumerId(andreAndroid.getConsumerId());

    }

    private Optional<UserDeviceDocument> getCachedUsersDevices(String consumerId) {
        return ofNullable(cacheManager.getCache("UsersDevices")).map(c -> c.get(consumerId, UserDeviceDocument.class));
    }
    private String getCachedName(String consumerId) {
        final Cache usersDevices = cacheManager.getCache("UsersDevices");
        return usersDevices.getName();
    }


}