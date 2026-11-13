package com.zjude.web.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zjude.framework.security.handle.AuthenticationEntryPointImpl;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class AuthenticationEntryPointImplTest
{
    @Test
    void shouldReturnUnauthorizedHttpStatusWhenAuthenticationFails() throws Exception
    {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/common/upload/avatar");
        MockHttpServletResponse response = new MockHttpServletResponse();

        new AuthenticationEntryPointImpl().commence(request, response,
                new BadCredentialsException("Authentication required"));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"code\":401"));
        assertTrue(response.getContentAsString().contains("/common/upload/avatar"));
    }
}
