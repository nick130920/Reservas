package co.edu.usco.reservas.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    //@Autowired
    private CustomUserDetails customUserDetails;
    //@Autowired
    private JwtGenerador jwtGenerador;

    private String obtenerTokenDeSolicitud(HttpServletRequest request){
        String bearerToken =request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7, bearerToken.length());
        }
        return null;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = obtenerTokenDeSolicitud(request);
        if (StringUtils.hasText(token) && jwtGenerador.validarToken(token)){
            String username = jwtGenerador.obtenerUsernameDeJwt(token);
            UserDetails userDetails = customUserDetails.loadUserByUsername(username);

            // Cargar roles de usuario desde la base de datos (a través del UserDetails)
            List<SimpleGrantedAuthority> userAuthorities = userDetails.getAuthorities().stream()
                    .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                    .toList();
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userAuthorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
