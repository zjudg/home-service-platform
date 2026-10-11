package com.zjude.integrated.sms;

import java.util.Map;

public interface SmsService
{
    void send(String mobile, SmsScene scene, Map<String, String> params);

    void sendCode(String mobile, SmsScene scene, String code);
}
