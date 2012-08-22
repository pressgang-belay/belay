package org.jboss.pressgangccms.util.test.unit.gwt;

import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import net.sf.ipsedixit.integration.junit.JUnit4IpsedixitTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Base unit test class.
 *
 * @author kamiller@redhat.com (Katie Miller)
 */
@RunWith(JUnit4IpsedixitTestRunner.class)
public class BaseUnitTest {

    @Before
    public void initialiseMocks() {
        MockitoAnnotations.initMocks(this);
    }

    // Required to avoid initialisation error
    @Test
    public void sanityCheck() {
        assertThat(true, is(true));
    }

    /**
     * Custom stubber to help with tests using asynchronous callbacks.
     * Modelled from http://blog.reflectedcircle.co.uk/2009/12/08/gwt-rpc-asynccallback-testing-using-mockito/.
     */
    public static class CallbackMockStubber {
        public static <T> Stubber callSuccessWith(final String data) {
            return Mockito.doAnswer(new Answer<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                    final java.lang.Object[] args = invocationOnMock.getArguments();
                    ((Callback) args[args.length - 1]).onSuccess(data);
                    return null;
                }
            });
        }

        public static <T> Stubber callFailureWith(final Throwable error) {
            return Mockito.doAnswer(new Answer<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                    final Object[] args = invocationOnMock.getArguments();
                    ((Callback) args[args.length - 1]).onFailure(error);
                    return null;
                }
            });
        }

        public static <T> Stubber callOnResponseReceivedWith(final Request request, final Response response) {
            return Mockito.doAnswer(new Answer<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                    final Object[] args = invocationOnMock.getArguments();
                    ((RequestCallback) args[args.length - 1]).onResponseReceived(request, response);
                    return null;
                }
            });
        }

        public static <T> Stubber callOnErrorWith(final Request request, final Throwable exception) {
            return Mockito.doAnswer(new Answer<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T answer(InvocationOnMock invocationOnMock) throws Throwable {
                    final Object[] args = invocationOnMock.getArguments();
                    ((RequestCallback) args[args.length - 1]).onError(request, exception);
                    return null;
                }
            });
        }
    }
}
