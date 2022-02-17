package ca.corefacility.bioinformatics.irida.ria.unit.web.oauth;

import java.util.*;
import java.util.function.Function;

import javax.validation.*;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.corefacility.bioinformatics.irida.model.RemoteAPI;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.RemoteAPIAjaxController;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxCreateItemSuccessResponse;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxFormErrorResponse;
import ca.corefacility.bioinformatics.irida.ria.web.ajax.dto.ajax.AjaxResponse;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableRequest;
import ca.corefacility.bioinformatics.irida.ria.web.models.tables.TableResponse;
import ca.corefacility.bioinformatics.irida.ria.web.rempoteapi.dto.RemoteAPITableModel;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIRemoteAPIService;
import ca.corefacility.bioinformatics.irida.service.RemoteAPIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Testing for {@link RemoteAPIAjaxController}
 */
public class RemoteAPIAjaxControllerTest {
	private RemoteAPIService remoteAPIService;
	private UIRemoteAPIService uiRemoteAPIService;
	private RemoteAPIAjaxController controller;
	private MessageSource messageSource;

	private final RemoteAPI REMOTE_API_01 = new RemoteAPI("Toronto", "http://toronto.nowhere", "", "toronto", "123456");
	private final RemoteAPI REMOTE_API_02 = new RemoteAPI("Washington", "http://washington.nowhere", "", "washington",
			"654321");

	@BeforeEach
	public void init() {
		remoteAPIService = mock(RemoteAPIService.class);
		uiRemoteAPIService = mock(UIRemoteAPIService.class);
		messageSource = mock(MessageSource.class);
		controller = new RemoteAPIAjaxController(remoteAPIService, uiRemoteAPIService, messageSource);

		Page<RemoteAPI> remoteAPIPage = new Page<>() {
			@Override
			public int getTotalPages() {
				return 10;
			}

			@Override
			public long getTotalElements() {
				return 104;
			}

			@Override
			public <U> Page<U> map(Function<? super RemoteAPI, ? extends U> function) {
				return null;
			}

			@Override
			public int getNumber() {
				return 0;
			}

			@Override
			public int getSize() {
				return 0;
			}

			@Override
			public int getNumberOfElements() {
				return 104;
			}

			@Override
			public List<RemoteAPI> getContent() {
				return List.of(REMOTE_API_01);
			}

			@Override
			public boolean hasContent() {
				return true;
			}

			@Override
			public Sort getSort() {
				return null;
			}

			@Override
			public boolean isFirst() {
				return false;
			}

			@Override
			public boolean isLast() {
				return false;
			}

			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public boolean hasPrevious() {
				return false;
			}

			@Override
			public Pageable nextPageable() {
				return null;
			}

			@Override
			public Pageable previousPageable() {
				return null;
			}

			@Override
			public Iterator<RemoteAPI> iterator() {
				return null;
			}
		};

		when(remoteAPIService.search(any(), anyInt(), anyInt(), any(Sort.Direction.class),
				any(String.class))).thenReturn(remoteAPIPage);
		when(remoteAPIService.read(1L)).thenReturn(REMOTE_API_01);
		when(remoteAPIService.read(2L)).thenReturn(REMOTE_API_02);
	}

	@Test
	public void getAjaxAPIListTest() {
		TableRequest request = new TableRequest();
		request.setSortColumn("label");
		request.setSortDirection("ascend");
		request.setCurrent(0);
		request.setPageSize(10);

		TableResponse<RemoteAPITableModel> response = controller.getAjaxAPIList(request);
		verify(remoteAPIService, times(1)).search(any(), anyInt(), anyInt(), any(Sort.Direction.class),
				any(String.class));
		assertEquals(1, response.getDataSource().size(), "Should have 1 Remote API");
	}

	@Test
	public void testPostCreateRemoteAPI_goodRequest() {
		RemoteAPI client = new RemoteAPI("NEW CLIENT", "http://example.com", "", "CLIENT_ID", "CLIENT_SECRET");
		RemoteAPI createdClient = new RemoteAPI("NEW CLIENT", "http://example.com", "", "CLIENT_ID", "CLIENT_SECRET");
		createdClient.setId(1L);

		when(remoteAPIService.create(client)).thenReturn(createdClient);

		ResponseEntity<AjaxResponse> response = controller.postCreateRemoteAPI(client, Locale.ENGLISH);
		assertEquals("Should have a response status of 200", response.getStatusCode(), HttpStatus.OK);
		AjaxCreateItemSuccessResponse ajaxCreateItemSuccessResponse = (AjaxCreateItemSuccessResponse) response.getBody();
		assert ajaxCreateItemSuccessResponse != null;
		assertEquals("Should return the id of the newly created remote api", 1L, ajaxCreateItemSuccessResponse.getId());
	}

	@Test
	public void testPostCreateRemoteAPI_badRequest() {
		RemoteAPI client = new RemoteAPI(null, null, "", "CLIENT_ID", "CLIENT_SECRET");

		Configuration<?> configuration = Validation.byDefaultProvider().configure();
		ValidatorFactory factory = configuration.buildValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<RemoteAPI>> violations = validator.validate(client);

        HashSet<ConstraintViolation<?>> constraintViolations = new HashSet<>(violations);


		when(remoteAPIService.create(client)).thenThrow(new ConstraintViolationException(constraintViolations));

		ResponseEntity<AjaxResponse> response = controller.postCreateRemoteAPI(client, Locale.ENGLISH);
		verify(remoteAPIService, times(1)).create(client);
		assertEquals("Should return a 422 response", response.getStatusCode(), HttpStatus.UNPROCESSABLE_ENTITY);
		AjaxFormErrorResponse formErrorsResponse = (AjaxFormErrorResponse)response.getBody();
		assert formErrorsResponse != null;
		Map<String, String> formErrors = formErrorsResponse.getErrors();
		assertEquals("Form should have 3 errors", 3, formErrors.keySet()
				.size());
		assertTrue("Should have a serviceUri error", formErrors.containsKey("serviceURI"));
		assertTrue("Should have a name error", formErrors.containsKey("name"));
		assertTrue("Should have a label error", formErrors.containsKey("label"));
	}
}
