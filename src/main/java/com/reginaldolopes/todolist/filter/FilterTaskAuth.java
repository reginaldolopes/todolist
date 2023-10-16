package com.reginaldolopes.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.reginaldolopes.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        var servletPath = request.getServletPath();
        if(servletPath.startsWith("/tasks/")){
            // Pegar a autenticação (usuário e senha)
            var authEncoded = request.getHeader("Authorization")
                .substring("Basic".length()).trim();
            byte[] authDecode = Base64.getDecoder()
                .decode(authEncoded);
            var authString = new String(authDecode);

            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // Validar usuário
            var user = this.userRepository.findByUsername(username);
            if(user == null){
                response.sendError(401, "Usuário sem autorização");
            } else{
                // Validar senha
                var passwordVerify = BCrypt.verifyer()
                    .verify(password.toCharArray(), user.getPassword());
                if(passwordVerify.verified){
                    // Segue viagem  
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else{
                    response.sendError(401, "Usuário sem autorização");
                }
            }
        }else {
            filterChain.doFilter(request, response);
        }        
    }
}    
