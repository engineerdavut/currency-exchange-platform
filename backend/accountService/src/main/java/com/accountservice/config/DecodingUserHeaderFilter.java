package com.accountservice.config; // veya uygun paket

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger; // Loglama için ekleyelim
import org.slf4j.LoggerFactory; // Loglama için ekleyelim
import org.springframework.core.Ordered; // Filter sıralaması için
import org.springframework.core.annotation.Order; // Filter sıralaması için
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.lang.NonNull;

import java.io.IOException;
import java.io.UnsupportedEncodingException; // URLDecoder için
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10) // Diğer Spring Security filtrelerinden sonra, ama resolver'lardan önce çalışsın
public class DecodingUserHeaderFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(DecodingUserHeaderFilter.class);
    private static final String HEADER_NAME = "X-User";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
                                    throws ServletException, IOException {

        // Gelen X-User header'ını kontrol et
        String originalHeaderValue = request.getHeader(HEADER_NAME);

        if (originalHeaderValue != null && originalHeaderValue.contains("%")) { // Sadece encode edilmişse decode et
            logger.debug("DecodingUserHeaderFilter: Original X-User header: {}", originalHeaderValue);
            HttpServletRequest wrappedRequest = new HttpServletRequestWrapper(request) {
                @Override
                public String getHeader(String name) {
                    String value = super.getHeader(name);
                    if (HEADER_NAME.equalsIgnoreCase(name) && value != null) {
                        try {
                            String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8.name());
                            logger.debug("DecodingUserHeaderFilter: Decoded X-User header: {} -> {}", value, decodedValue);
                            return decodedValue;
                        } catch (IllegalArgumentException | UnsupportedEncodingException e) { // UnsupportedEncodingException pek olası değil
                            logger.warn("DecodingUserHeaderFilter: Failed to decode X-User header value: {}. Error: {}", value, e.getMessage());
                            return value; // Decode edilemezse orijinal değeri döndür
                        }
                    }
                    return value;
                }

                @Override
                public Enumeration<String> getHeaders(String name) {
                    if (HEADER_NAME.equalsIgnoreCase(name)) {
                        List<String> decodedValues = Collections.list(super.getHeaders(name))
                            .stream()
                            .map(v -> {
                                try {
                                    String decoded = URLDecoder.decode(v, StandardCharsets.UTF_8.name());
                                    logger.debug("DecodingUserHeaderFilter: Decoded X-User header (in list): {} -> {}", v, decoded);
                                    return decoded;
                                } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                                    logger.warn("DecodingUserHeaderFilter: Failed to decode X-User header value in list: {}. Error: {}", v, e.getMessage());
                                    return v;
                                }
                            })
                            .collect(Collectors.toList());
                        return Collections.enumeration(decodedValues);
                    }
                    return super.getHeaders(name);
                }

                // getHeaderNames() metodunu da override etmek isteyebilirsiniz, ancak genellikle gerekmeyebilir.
            };
            filterChain.doFilter(wrappedRequest, response);
        } else {
            // Header yoksa veya zaten decode edilmişse (veya % içermiyorsa) doğrudan devam et
            if (originalHeaderValue != null) {
                 logger.debug("DecodingUserHeaderFilter: X-User header '{}' does not need decoding or is not present.", originalHeaderValue);
            }
            filterChain.doFilter(request, response);
        }
    }
}
