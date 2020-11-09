/*
 * @(#)UserDeviceService.java 1.0 31/10/2020
 *
 * Copyright (c) 2020, PicPay S.A. All rights reserved.
 * PicPay S.A. proprietary/confidential. Use is subject to license terms.
 */

package com.picpay.api.oauth2.domain.user.services;

import com.picpay.api.oauth2.domain.user.document.Device;
import com.picpay.api.oauth2.domain.user.document.UserDeviceDocument;
import com.picpay.api.oauth2.domain.user.repository.UserDeviceRepository;
import com.picpay.api.oauth2.infra.handler.exception.notfound.UserDeviceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static java.lang.String.format;

/**
 * Class comments go here...
 *
 * @author André Franco
 * @version 1.0 20/10/2020
 */
@Slf4j
@RequiredArgsConstructor
@Service
@CacheConfig(cacheNames = "UsersDevices")
public class UserDeviceService {

    private final UserDeviceRepository deviceRepository;

    @Cacheable(key = "#consumerId", condition="#consumerId!=null")
    public UserDeviceDocument findDeviceByConsumerId(final String consumerId){
        log.info("find user: {} ", consumerId);
        return deviceRepository
            .findByConsumerId(consumerId)
            .orElseThrow(() -> new UserDeviceNotFoundException(format("devices do consumer [%s] não encontrado:", consumerId)));
    }

    @CachePut(key = "#consumerId", condition="#UserDeviceDocument.getConsumerId!=null")
    public void updateOrSave(final UserDeviceDocument deviceDocument){
        deviceRepository
            .findByConsumerId(deviceDocument.getConsumerId())
            .ifPresentOrElse(this::update, () -> insert(deviceDocument));
    }

    @CachePut(key = "#consumerId", condition="#consumerId!=null")
    public UserDeviceDocument update(final String consumerId, final Device device) {
        final UserDeviceDocument userDeviceSaved = findDeviceByConsumerId(consumerId);
        userDeviceSaved.addDevices(device);
        final UserDeviceDocument deviceDocument = deviceRepository.save(userDeviceSaved.withUpdateAt(LocalDateTime.now()));
        return deviceDocument;

    }
    @CachePut(key = "#userDeviceDocument.consumerId", condition="#consumerId!=null")
    private UserDeviceDocument update(final UserDeviceDocument deviceDoc) {
        log.info("update list devices");
        final UserDeviceDocument s = deviceDoc.withUpdateAt(LocalDateTime.now());
        return deviceRepository.save(s);
    }
    private void insert(final UserDeviceDocument deviceDocument) {
        log.info("save new user_device");
        deviceRepository.insert(deviceDocument);
    }



}
