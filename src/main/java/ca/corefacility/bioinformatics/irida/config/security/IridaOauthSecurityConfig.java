package ca.corefacility.bioinformatics.irida.config.security;

import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.ClientDetailsUserDetailsService;
import org.springframework.security.oauth2.provider.error.AbstractOAuth2SecurityExceptionHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.ClientSecretAuthenticationProvider;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.token.*;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.ReflectionUtils;

import ca.corefacility.bioinformatics.irida.oauth2.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import ca.corefacility.bioinformatics.irida.oauth2.OAuth2ResourceOwnerPasswordAuthenticationProvider;
import ca.corefacility.bioinformatics.irida.web.controller.api.exception.CustomOAuth2ExceptionTranslator;
import ca.corefacility.bioinformatics.irida.web.filter.UnauthenticatedAnonymousAuthenticationFilter;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

/**
 * Configuration for REST API security using OAuth2
 */
@Configuration
public class IridaOauthSecurityConfig {
	private static final Logger logger = LoggerFactory.getLogger(IridaOauthSecurityConfig.class);

	private static final String AUTHORITIES_CLAIM = "authorities";

	@Bean
	@Primary
	public ResourceServerTokenServices tokenServices(@Qualifier("clientDetails") ClientDetailsService clientDetails,
			@Qualifier("iridaTokenStore") TokenStore tokenStore) {
		DefaultTokenServices services = new DefaultTokenServices();
		services.setTokenStore(tokenStore);
		services.setSupportRefreshToken(true);
		services.setClientDetailsService(clientDetails);
		return services;
	}

	@Bean
	public ClientDetailsUserDetailsService clientDetailsUserDetailsService(
			@Qualifier("clientDetails") ClientDetailsService clientDetails) {
		ClientDetailsUserDetailsService clientDetailsUserDetailsService = new ClientDetailsUserDetailsService(
				clientDetails);

		return clientDetailsUserDetailsService;
	}

	@Bean
	public WebResponseExceptionTranslator<OAuth2Exception> exceptionTranslator() {
		return new CustomOAuth2ExceptionTranslator();
	}

	/**
	 * Class for configuring the OAuth resource server security
	 */
	@Configuration
	protected static class ResourceServerConfig {

		@Value("${server.base.url}")
		private String serverBase;

		@Bean
		@Order(Ordered.HIGHEST_PRECEDENCE + 2)
		public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
			http.antMatcher("/api/**")
					.authorizeRequests()
					.regexMatchers(HttpMethod.GET, "/api.*")
					.hasAuthority("SCOPE_read")
					.regexMatchers("/api.*")
					.hasAuthority("SCOPE_write");
			http.antMatcher("/api/**").headers().frameOptions().disable();
			http.antMatcher("/api/**")
					.csrf()
					.requireCsrfProtectionMatcher(new AntPathRequestMatcher("/api/oauth/authorize"))
					.disable();
			http.antMatcher("/api/**").csrf().disable();
			http.oauth2ResourceServer().jwt().jwtAuthenticationConverter(jwtAuthenticationConverter());
			// SecurityContextPersistenceFilter appears pretty high up (well
			// before any OAuth related filters), so we'll put our anonymous
			// user filter into the filter chain after that.
			http.antMatcher("/api/**")
					.addFilterAfter(new UnauthenticatedAnonymousAuthenticationFilter("anonymousTokenAuthProvider"),
							SecurityContextPersistenceFilter.class);

			return http.build();
		}

		@Bean
		public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
			final JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
			final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
			jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_CLAIM);
			jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
			jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
			return jwtAuthenticationConverter;
		}
	}

	//@EnableResourceServer
	//@ComponentScan(basePackages = "ca.corefacility.bioinformatics.irida.repositories.remote")
	//@Order(Ordered.HIGHEST_PRECEDENCE + 2)
	protected static class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

		@Autowired
		private ResourceServerTokenServices tokenServices;

		@Autowired
		private WebResponseExceptionTranslator<OAuth2Exception> exceptionTranslator;

		@Override
		public void configure(final ResourceServerSecurityConfigurer resources) {
			resources.resourceId("NmlIrida").tokenServices(tokenServices);
			forceExceptionTranslator(resources, exceptionTranslator);
		}

		@Override
		public void configure(final HttpSecurity httpSecurity) throws Exception {
			httpSecurity.antMatcher("/api/**")
					.authorizeRequests()
					.regexMatchers(HttpMethod.GET, "/api.*")
					.access("#oauth2.hasScope('read')")
					.regexMatchers("/api.*")
					.access("#oauth2.hasScope('read') and #oauth2.hasScope('write')");
			httpSecurity.antMatcher("/api/**").headers().frameOptions().disable();
			httpSecurity.antMatcher("/api/**")
					.csrf()
					.requireCsrfProtectionMatcher(new AntPathRequestMatcher("/api/oauth/authorize"))
					.disable();
			httpSecurity.antMatcher("/api/**").csrf().disable();

			// SecurityContextPersistenceFilter appears pretty high up (well
			// before any OAuth related filters), so we'll put our anonymous
			// user filter into the filter chain after that.
			httpSecurity.antMatcher("/api/**")
					.addFilterAfter(new UnauthenticatedAnonymousAuthenticationFilter("anonymousTokenAuthProvider"),
							SecurityContextPersistenceFilter.class);
		}
	}

	/**
	 * Class for configuring the OAuth authorization server
	 */
	@Configuration(proxyBeanMethods = false)
	protected static class AuthorizationServerConfig {

		private static final String CUSTOM_CONSENT_PAGE_URI = "/api/oauth/consent";

		@Value("${server.base.url}")
		private String serverBase;

		@Autowired
		private OAuth2AuthorizationService authorizationService;

		@Autowired
		private ClientSecretAuthenticationProvider clientSecretAuthenticationProvider;

		@Autowired
		private AuthenticationManager authenticationManager;

		@Autowired
		private OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

		// @formatter:off
		@Bean
		@Order(Ordered.HIGHEST_PRECEDENCE)
		public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
			OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer<>();

			RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

			authorizationServerConfigurer.clientAuthentication(clientAuthentication -> clientAuthentication.authenticationProvider(clientSecretAuthenticationProvider));

			authorizationServerConfigurer.tokenEndpoint((tokenEndpoint) -> tokenEndpoint.accessTokenRequestConverter(
				new DelegatingAuthenticationConverter(Arrays.asList(
					new OAuth2AuthorizationCodeAuthenticationConverter(),
					new OAuth2RefreshTokenAuthenticationConverter(),
					new OAuth2ClientCredentialsAuthenticationConverter(),
					new OAuth2ResourceOwnerPasswordAuthenticationConverter()))
			));

			authorizationServerConfigurer.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI));

			http
				.requestMatcher(endpointsMatcher)
				.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated())
				.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
				.apply(authorizationServerConfigurer)
				.and()
				.exceptionHandling(exceptions -> exceptions
					.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")));

			addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(http);

			return http.build();
		}
		// @formatter:on

		private void addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(HttpSecurity http) {
			OAuth2ResourceOwnerPasswordAuthenticationProvider resourceOwnerPasswordAuthenticationProvider = new OAuth2ResourceOwnerPasswordAuthenticationProvider(
					authenticationManager, authorizationService, tokenGenerator);

			// This will add new authentication provider in the list of existing authentication providers.
			http.authenticationProvider(resourceOwnerPasswordAuthenticationProvider);
		}

		@Bean
		public RegisteredClientRepository registeredClientRepository() {
			RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
					.clientId("test-client")
					.clientSecret("secret")
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
					.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
					.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
					.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
					.authorizationGrantType(AuthorizationGrantType.PASSWORD)
					.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
					.redirectUri(serverBase + "/api/oauth/authorization/token")
					.redirectUri("https://oauth.pstmn.io/v1/callback")
					.scope("read")
					.scope("write")
					.clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
					.build();
			return new InMemoryRegisteredClientRepository(registeredClient);
		}

		@Bean
		public OAuth2AuthorizationService authorizationService(RegisteredClientRepository registeredClientRepository) {
			return new InMemoryOAuth2AuthorizationService();
		}

		@Bean
		public OAuth2AuthorizationConsentService authorizationConsentService(
				RegisteredClientRepository registeredClientRepository) {
			return new InMemoryOAuth2AuthorizationConsentService();
		}

		@Bean
		public ClientSecretAuthenticationProvider oauthClientAuthProvider(
				RegisteredClientRepository registeredClientRepository,
				OAuth2AuthorizationService oAuth2AuthorizationService) {
			ClientSecretAuthenticationProvider clientAuthenticationProvider = new ClientSecretAuthenticationProvider(
					registeredClientRepository, oAuth2AuthorizationService);

			clientAuthenticationProvider.setPasswordEncoder(new PasswordEncoder() {
				@Override
				public boolean matches(CharSequence rawPassword, String encodedPassword) {
					return rawPassword.equals(encodedPassword);
				}

				@Override
				public String encode(CharSequence rawPassword) {
					return rawPassword.toString();
				}
			});

			return clientAuthenticationProvider;
		}

		@Bean
		public OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
			return context -> {
				Set<AuthorizationGrantType> grantTypes = Set.of(AuthorizationGrantType.AUTHORIZATION_CODE,
						AuthorizationGrantType.PASSWORD);
				if (grantTypes.contains(context.getAuthorizationGrantType())
						&& OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
					Authentication principal = context.getPrincipal();
					Set<String> authorities = new HashSet<>();
					for (GrantedAuthority authority : principal.getAuthorities()) {
						authorities.add(authority.getAuthority());
					}
					for (String authorizedScope : context.getAuthorizedScopes()) {
						authorities.add("SCOPE_" + authorizedScope);
					}
					context.getClaims().claim(AUTHORITIES_CLAIM, authorities);
				}
			};
		}

		@Bean
		@SuppressWarnings("unused")
		public OAuth2TokenGenerator<OAuth2Token> oAuth2TokenGenerator(JwtEncoder jwtEncoder,
				OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
			JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
			jwtGenerator.setJwtCustomizer(jwtCustomizer);
			OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
			OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
			return new DelegatingOAuth2TokenGenerator(jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
		}

		@Bean
		public RSAKey rsaKey() {
			return generateRsa();
		}

		@Bean
		public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
			JWKSet jwkSet = new JWKSet(rsaKey);
			return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
		}

		@Bean
		@SuppressWarnings("unused")
		public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
			return new NimbusJwtEncoder(jwkSource);
		}

		@Bean
		public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
			return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
		}

		//@Bean
		//public JwtDecoder jwtDecoder(RSAKey rsaKey) throws JOSEException {
		//	return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();//.withJwkSetUri(serverBase);
		//	//return new NimbusJwtDecoder(jwkSource);
		//}

		@Bean
		public ProviderSettings providerSettings() {
			return ProviderSettings.builder()
					.issuer(serverBase)
					.authorizationEndpoint("/api/oauth/authorize")
					.tokenEndpoint("/api/oauth/token")
					.jwkSetEndpoint("/api/oauth/jwks")
					.tokenRevocationEndpoint("/api/oauth/revoke")
					.tokenIntrospectionEndpoint("/api/oauth/introspect")
					.build();
		}

		private static RSAKey generateRsa() {
			KeyPair keyPair = generateRsaKey();
			RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
			RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
			return new RSAKey.Builder(publicKey).privateKey(privateKey).keyID(UUID.randomUUID().toString()).build();
		}

		private static KeyPair generateRsaKey() {
			KeyPair keyPair;
			try {
				KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
				keyPairGenerator.initialize(2048);
				keyPair = keyPairGenerator.generateKeyPair();
			} catch (Exception ex) {
				throw new IllegalStateException(ex);
			}
			return keyPair;
		}
	}

	/**
	 * Forcibly set the exception translator on the `authenticationEntryPoint` so that we can supply our own errors on
	 * authentication failure. The `authenticationEntryPoint` field on {@link AbstractOAuth2SecurityExceptionHandler} is
	 * marked `private`, and is not accessible for customizing.
	 *
	 * @param configurer          the instance of the configurer that we're customizing
	 * @param exceptionTranslator the {@link WebResponseExceptionTranslator} that we want to set.
	 * @param <T>                 The type of security configurer
	 */
	private static <T> void forceExceptionTranslator(final T configurer,
			final WebResponseExceptionTranslator<OAuth2Exception> exceptionTranslator) {
		try {
			final Field authenticationEntryPointField = ReflectionUtils.findField(configurer.getClass(),
					"authenticationEntryPoint");
			ReflectionUtils.makeAccessible(authenticationEntryPointField);
			final OAuth2AuthenticationEntryPoint authenticationEntryPoint = (OAuth2AuthenticationEntryPoint) authenticationEntryPointField
					.get(configurer);

			logger.debug("Customizing the authentication entry point by brute force.");
			authenticationEntryPoint.setExceptionTranslator(exceptionTranslator);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
			logger.error("Failed to configure the authenticationEntryPoint on ResourceServerSecurityConfigurer.", e);
		}
	}
}
