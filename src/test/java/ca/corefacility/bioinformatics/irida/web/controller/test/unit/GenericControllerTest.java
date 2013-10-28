package ca.corefacility.bioinformatics.irida.web.controller.test.unit;

import ca.corefacility.bioinformatics.irida.exceptions.EntityNotFoundException;
import ca.corefacility.bioinformatics.irida.model.enums.Order;
import ca.corefacility.bioinformatics.irida.service.CRUDService;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.ResourceCollection;
import ca.corefacility.bioinformatics.irida.web.assembler.resource.RootResource;
import ca.corefacility.bioinformatics.irida.web.controller.api.GenericController;
import ca.corefacility.bioinformatics.irida.web.controller.api.exception.GenericsException;
import ca.corefacility.bioinformatics.irida.web.controller.test.unit.support.IdentifiableTestEntity;
import ca.corefacility.bioinformatics.irida.web.controller.test.unit.support.IdentifiableTestResource;
import com.google.common.net.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link GenericController}.
 *
 * @author Franklin Bristow <franklin.bristow@phac-aspc.gc.ca>
 */
@SuppressWarnings("unused")
public class GenericControllerTest {

    private static final String RELATED_IDENTIFIABLE_TEST_ENTITY_KEY = "related";
    private GenericController<IdentifiableTestEntity, IdentifiableTestResource> controller;
    private CRUDService<Long, IdentifiableTestEntity> crudService;
    private IdentifiableTestEntity entity;
    private Map<String, Object> updatedFields;
    private Long identifier;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        crudService = mock(CRUDService.class);
        entity = new IdentifiableTestEntity();
        identifier = 1L;
        entity.setId(identifier);
        controller = new GenericController<IdentifiableTestEntity, IdentifiableTestResource>(crudService,
                IdentifiableTestEntity.class, IdentifiableTestResource.class){};
        updatedFields = new HashMap<>();
    }

    @Test
    public void testCreateBadEntity() {
        IdentifiableTestResource r = new IdentifiableTestResource(entity);

        when(crudService.create(entity)).thenThrow(
                new ConstraintViolationException(new HashSet<ConstraintViolation<?>>()));

        try {
            controller.create(r);
            fail();
        } catch (ConstraintViolationException ex) {
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testCreateGoodEntity() {
        IdentifiableTestResource resource = new IdentifiableTestResource(entity);
        when(crudService.create(entity)).thenReturn(entity);

        ResponseEntity<String> mav = controller.create(resource);
        assertEquals(HttpStatus.CREATED, mav.getStatusCode());
        assertTrue(mav.getHeaders().getFirst(HttpHeaders.LOCATION).
                endsWith(identifier.toString()));
    }

    @Test
    public void testDeleteEntity() throws InstantiationException, IllegalAccessException {
        ModelMap modelMap = controller.delete(2L);
        RootResource rootResource = (RootResource) modelMap.get(GenericController.RESOURCE_NAME);
        Link l = rootResource.getLink(GenericController.REL_COLLECTION);
        assertNotNull(l);
        assertEquals("http://localhost/generic", l.getHref());
    }

    @Test
    public void testDeleteInvalidEntity() {
        doThrow(new EntityNotFoundException("not found")).when(crudService).delete(2L);

        try {
            controller.delete(2L);
        } catch (EntityNotFoundException e) {
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testGetResource() {
        when(crudService.read(identifier)).thenReturn(entity);
        ModelMap model = null;

        try {
            model = controller.getResource(identifier);
        } catch (GenericsException e) {
            fail();
        }

        assertTrue(model.containsKey("resource"));
        IdentifiableTestResource resource = (IdentifiableTestResource) model.get("resource");
        assertTrue(resource.getLink(Link.REL_SELF).getHref().endsWith(identifier.toString()));
    }

    @Test
    public void testGetBadResource() {
        when(crudService.read(identifier)).thenThrow(new EntityNotFoundException("not found"));
        try {
            controller.getResource(identifier);
        } catch (EntityNotFoundException e) {
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUpdate() throws InstantiationException, IllegalAccessException {
        when(crudService.update(identifier, updatedFields)).thenReturn(entity);
        ModelMap response = controller.update(identifier, updatedFields);
        RootResource r = (RootResource) response.get(GenericController.RESOURCE_NAME);
        assertNotNull(r.getLink(Link.REL_SELF));
        assertNotNull(r.getLink(GenericController.REL_COLLECTION));
    }

    @Test
    public void testUpdateBadResource() throws InstantiationException, IllegalAccessException {
        when(crudService.update(identifier, updatedFields)).thenThrow(new EntityNotFoundException("not found"));
        try {
            controller.update(identifier, updatedFields);
            fail();
        } catch (EntityNotFoundException e) {
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testUpdateFailsConstraints() throws InstantiationException, IllegalAccessException {
        when(crudService.update(identifier, updatedFields)).thenThrow(
                new ConstraintViolationException(new HashSet<ConstraintViolation<?>>()));
        try {
            controller.update(identifier, updatedFields);
            fail();
        } catch (ConstraintViolationException e) {
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void testListResources() throws InstantiationException, IllegalAccessException {
        int totalResources = 400;
        List<IdentifiableTestEntity> entities = new ArrayList<>();
        entities.add(entity);
        when(crudService.list(2, 20, "nonNull", Order.DESCENDING)).thenReturn(entities);
        when(crudService.count()).thenReturn(totalResources);
        ModelMap mav = controller.listResources(2, 20, "nonNull", Order.DESCENDING);
        assertNotNull(mav.get(GenericController.RESOURCE_NAME));
        Object o = mav.get(GenericController.RESOURCE_NAME);
        assertTrue(o instanceof ResourceCollection);
        @SuppressWarnings("unchecked")
        ResourceCollection<IdentifiableTestResource> collection = (ResourceCollection<IdentifiableTestResource>) o;
        assertEquals(6, collection.getLinks().size());
        assertEquals(totalResources, collection.getTotalResources());

        for (IdentifiableTestResource r : collection) {
            assertEquals(1, r.getLinks().size());
            Link link = r.getLink(Link.REL_SELF);
            assertTrue(link.getHref().endsWith(entity.getId().toString()));
        }
    }

    @Test
    public void testListResourcesNoSortProperty() throws InstantiationException, IllegalAccessException {
        int totalResources = 400;
        List<IdentifiableTestEntity> entities = new ArrayList<>();
        entities.add(entity);
        when(crudService.list(2, 20, Order.DESCENDING)).thenReturn(entities);
        when(crudService.count()).thenReturn(totalResources);
        ModelMap mav = controller.listResources(2, 20, null, Order.DESCENDING);
        assertNotNull(mav.get(GenericController.RESOURCE_NAME));
        Object o = mav.get(GenericController.RESOURCE_NAME);
        assertTrue(o instanceof ResourceCollection);
        @SuppressWarnings("unchecked")
        ResourceCollection<IdentifiableTestResource> collection = (ResourceCollection<IdentifiableTestResource>) o;
        assertEquals(6, collection.getLinks().size());
        assertEquals(totalResources, collection.getTotalResources());

        for (IdentifiableTestResource r : collection) {
            assertEquals(1, r.getLinks().size());
            Link link = r.getLink(Link.REL_SELF);
            assertTrue(link.getHref().endsWith(entity.getId().toString()));
        }

        verify(crudService).list(2, 20, Order.DESCENDING);
        verify(crudService, times(0)).list(2, 20, null, Order.DESCENDING);
    }
}
