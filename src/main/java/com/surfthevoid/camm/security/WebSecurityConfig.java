package com.surfthevoid.camm.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private AuthenticationEntryPoint authEntryPoint;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
				.anyRequest().authenticated()
				.and().httpBasic()
				.authenticationEntryPoint(authEntryPoint);
	}

	@Autowired
	public void configureGlobal(
			@Value("#{systemProperties.cammuser}") String user,
			@Value("#{systemProperties.cammpassword}") String password,
			AuthenticationManagerBuilder auth) throws Exception {
		auth.inMemoryAuthentication()
			.withUser(user)
			.password(password)
			.roles("USER");
	}
	
	@Bean
	public BCryptPasswordEncoder encoder() {
		//online generator: https://www.bcrypt.fr/
	    return new BCryptPasswordEncoder();
	}
}
