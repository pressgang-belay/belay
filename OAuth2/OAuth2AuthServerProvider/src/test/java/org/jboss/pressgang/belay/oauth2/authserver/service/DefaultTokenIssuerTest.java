package org.jboss.pressgang.belay.oauth2.authserver.service;

import com.google.common.base.Optional;
import org.jboss.pressgang.belay.oauth2.authserver.data.dao.CodeGrantDao;
import org.jboss.pressgang.belay.oauth2.authserver.data.dao.TokenGrantDao;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.CodeGrant;
import org.jboss.pressgang.belay.oauth2.authserver.data.model.TokenGrant;
import org.jboss.pressgang.belay.util.test.unit.BaseUnitTest;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;
import java.util.logging.Logger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author kamiller@redhat.com (Katie Miller)
 */
public class DefaultTokenIssuerTest extends BaseUnitTest {

    @Mock
    private TokenGrantDao tokenGrantDao;
    @Mock
    private CodeGrantDao codeGrantDao;
    @Mock
    private Optional<TokenGrant> tokenGrantFound;
    @Mock
    private Optional<CodeGrant> codeGrantFound;
    @Mock
    private TokenGrant tokenGrant;
    @Mock
    private TokenGrant anotherTokenGrant;
    @Mock
    private CodeGrant codeGrant;
    @Mock
    private CodeGrant anotherCodeGrant;
    @Mock
    private Logger log;
    @InjectMocks
    private DefaultTokenIssuer tokenIssuer = new DefaultTokenIssuer();

    @Test
    public void shouldGenerateAccessToken() throws Exception {
        // Given a default token issuer and no existing TokenGrants
        when(tokenGrantFound.isPresent()).thenReturn(false);
        when(tokenGrantDao.getTokenGrantFromAccessToken(anyString())).thenReturn(tokenGrantFound);

        // When accessToken() is called
        String result = tokenIssuer.accessToken();

        // Then a non-null, non-empty String is returned
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    public void shouldNotGenerateAccessTokenMatchingExistingTokenFromCurrentGrant() throws Exception {
        // Given a default token issuer and an existing current TokenGrant
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(tokenGrant.getGrantCurrent()).thenReturn(true);
        when(anotherTokenGrant.getGrantCurrent()).thenReturn(false);
        when(tokenGrantDao.getTokenGrantFromAccessToken(anyString())).thenReturn(Optional.of(tokenGrant), Optional.of(anotherTokenGrant));

        // When a token is generated
        String result = tokenIssuer.accessToken();

        // Then the duplicate access token is bypassed and a new token is generated and returned
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
        verify(tokenGrantDao, times(2)).getTokenGrantFromAccessToken(argumentCaptor.capture());
        List<String> tokensGenerated = argumentCaptor.getAllValues();
        assertThat(result.equals(tokensGenerated.get(0)),is(false));
        assertThat(result.equals(tokensGenerated.get(1)),is(true));
    }

    @Test
    public void shouldGenerateRefreshToken() throws Exception {
        // Given a default token issuer and no existing TokenGrants
        when(tokenGrantFound.isPresent()).thenReturn(false);
        when(tokenGrantDao.getTokenGrantFromRefreshToken(anyString())).thenReturn(tokenGrantFound);

        // When refreshToken() is called
        String result = tokenIssuer.refreshToken();

        // Then a non-null, non-empty String is returned
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    public void shouldNotGenerateRefreshTokenMatchingExistingTokenFromCurrentGrant() throws Exception {
        // Given a default token issuer and an existing current TokenGrant
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(tokenGrant.getGrantCurrent()).thenReturn(true);
        when(anotherTokenGrant.getGrantCurrent()).thenReturn(false);
        when(tokenGrantDao.getTokenGrantFromRefreshToken(anyString())).thenReturn(Optional.of(tokenGrant), Optional.of(anotherTokenGrant));

        // When a token is generated
        String result = tokenIssuer.refreshToken();

        // Then the duplicate refresh token is bypassed and a new token is generated and returned
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
        verify(tokenGrantDao, times(2)).getTokenGrantFromRefreshToken(argumentCaptor.capture());
        List<String> tokensGenerated = argumentCaptor.getAllValues();
        assertThat(result.equals(tokensGenerated.get(0)),is(false));
        assertThat(result.equals(tokensGenerated.get(1)),is(true));
    }

    @Test
    public void shouldGenerateAuthCode() throws Exception {
        // Given a default token issuer and no existing CodeGrants
        when(codeGrantFound.isPresent()).thenReturn(false);
        when(codeGrantDao.getCodeGrantFromAuthCode(anyString())).thenReturn(codeGrantFound);

        // When authorizationCode() is called
        String result = tokenIssuer.authorizationCode();

        // Then a non-null, non-empty String is returned
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    public void shouldNotGenerateAuthCodeMatchingExistingCodeFromCurrentGrant() throws Exception {
        // Given a default token issuer and an existing current CodeGrant
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(codeGrant.getGrantCurrent()).thenReturn(true);
        when(anotherCodeGrant.getGrantCurrent()).thenReturn(false);
        when(codeGrantDao.getCodeGrantFromAuthCode(anyString())).thenReturn(Optional.of(codeGrant), Optional.of(anotherCodeGrant));

        // When an auth code is generated
        String result = tokenIssuer.authorizationCode();

        // Then the duplicate code is bypassed and a new code is generated and returned
        assertThat(result, notNullValue());
        assertThat(result.isEmpty(), is(false));
        verify(codeGrantDao, times(2)).getCodeGrantFromAuthCode(argumentCaptor.capture());
        List<String> codesGenerated = argumentCaptor.getAllValues();
        assertThat(result.equals(codesGenerated.get(0)),is(false));
        assertThat(result.equals(codesGenerated.get(1)),is(true));
    }
}
