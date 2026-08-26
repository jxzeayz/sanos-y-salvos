package cl.duocuc.sanosysalvos.bff.config;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RateLimitConfigTest {

    private RateLimitConfig filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitConfig();
        ReflectionTestUtils.setField(filter, "maxRequestsPerMinute", 2);
        ReflectionTestUtils.setField(filter, "windowMs", 60_000L);
    }

    @Test
    void doFilterInternal_bajoElLimite_dejaPasarLaRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bff/mascotas");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void doFilterInternal_superaElLimite_retorna429() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 2; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/bff/mascotas");
            request.setRemoteAddr("10.0.0.2");
            filter.doFilterInternal(request, new MockHttpServletResponse(), chain);
        }

        MockHttpServletRequest tercera = new MockHttpServletRequest("GET", "/bff/mascotas");
        tercera.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(tercera, response, chain);

        assertThat(response.getStatus()).isEqualTo(429);
    }

    @Test
    void doFilterInternal_actuator_seOmiteDelLimite() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        request.setRemoteAddr("10.0.0.3");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
