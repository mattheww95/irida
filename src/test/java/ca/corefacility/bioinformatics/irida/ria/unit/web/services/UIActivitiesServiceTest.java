package ca.corefacility.bioinformatics.irida.ria.unit.web.services;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ca.corefacility.bioinformatics.irida.model.enums.ProjectRole;
import ca.corefacility.bioinformatics.irida.model.event.*;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectSampleJoin;
import ca.corefacility.bioinformatics.irida.model.joins.impl.ProjectUserJoin;
import ca.corefacility.bioinformatics.irida.model.project.Project;
import ca.corefacility.bioinformatics.irida.model.sample.Sample;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.model.user.group.UserGroup;
import ca.corefacility.bioinformatics.irida.model.user.group.UserGroupProjectJoin;
import ca.corefacility.bioinformatics.irida.ria.web.activities.dto.Activity;
import ca.corefacility.bioinformatics.irida.ria.web.dto.list.PagedListResponse;
import ca.corefacility.bioinformatics.irida.ria.web.services.UIActivitiesService;
import ca.corefacility.bioinformatics.irida.service.ProjectEventService;
import ca.corefacility.bioinformatics.irida.service.ProjectService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;

import com.google.common.collect.ImmutableList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class UIActivitiesServiceTest {
	private UIActivitiesService service;

	private final Sample sample = new Sample("SAMPLE_1");
	private final Project project = new Project("PROJECT_1");

	private final Sample sample2 = new Sample("SAMPLE_2");
	private final Project project2 = new Project("PROJECT_2");

	private final Sample sample3 = new Sample("SAMPLE_3");
	private final Project project3 = new Project("PROJECT_3");

	private ProjectEventService eventService;
	private UserService userService;

	User user = new User("somename", "fred@nowhere.ca"," P@12345", "Fred", "Penner", "12221234567");
	User user2 = new User("somename2", "fred2@nowhere.ca"," P@12345", "Fred2", "Penner2", "12221234568");

	@BeforeEach
	public void setUp() {
		ProjectService projectService = Mockito.mock(ProjectService.class);
		eventService = Mockito.mock(ProjectEventService.class);
		MessageSource messageSource = Mockito.mock(MessageSource.class);
		userService = Mockito.mock(UserService.class);
		this.service = new UIActivitiesService(projectService, eventService, messageSource, userService);

		// Create mock data
		project.setId(1l);
		sample.setId(1l);

		project2.setId(2l);
		sample2.setId(2l);

		project3.setId(3l);
		sample3.setId(3l);

		Mockito.when(projectService.read(1l))
				.thenReturn(project);
		Mockito.when(projectService.read(2l))
				.thenReturn(project2);
		Mockito.when(projectService.read(3l))
				.thenReturn(project2);

	}

	@Test
	public void testGetActivitiesForProject() {
		Mockito.when(eventService.getEventsForProject(any(), any()))
				.thenReturn(createMockProjectEvents());
		PagedListResponse response = service.getActivitiesForProject(1l, 0, Locale.ENGLISH);
		assertEquals(4, response.getTotal(), "Should have 4 events");
		assertEquals("project_sample_added", ((Activity)response.getContent().get(0)).getType(), "First should be a project_sample_added event");
		assertEquals("project_user_role_updated", ((Activity)response.getContent().get(1)).getType(), "Second should be a project_user_role_updated event");
		assertEquals("project_user_removed", ((Activity)response.getContent().get(2)).getType(), "Third should be a project_user_removed event");
		assertEquals("project_user_group_added", ((Activity)response.getContent().get(3)).getType(), "Forth should be a project_user_group_added event");
	}

	@Test
	public void testGetAllProjectsRecentActivity() {
		Mockito.when(eventService.getAllProjectsEvents(any()))
				.thenReturn(createMockProjectEvents());
		PagedListResponse response = service.getAllRecentActivities(0, Locale.ENGLISH);
		assertEquals(8, response.getContent().size(), "Should have 8 events");
		assertEquals("project_sample_added", ((Activity)response.getContent().get(0)).getType(), "First should be a project_sample_added event");
		assertEquals("project_user_role_updated", ((Activity)response.getContent().get(1)).getType(), "Second should be a project_user_role_updated event");
		assertEquals("project_user_removed", ((Activity)response.getContent().get(2)).getType(), "Third should be a project_user_removed event");
		assertEquals("project_user_group_added", ((Activity)response.getContent().get(3)).getType(), "Fourth should be a project_user_group_added event");
		assertEquals("project_sample_added", ((Activity)response.getContent().get(4)).getType(), "Fifth should be a project_sample_added event");
		assertEquals("project_user_role_updated", ((Activity)response.getContent().get(5)).getType(), "Sixth should be a project_user_role_updated event");
		assertEquals("project_user_removed", ((Activity)response.getContent().get(6)).getType(), "Seventh should be a project_user_removed event");
		assertEquals("project_user_group_added", ((Activity)response.getContent().get(7)).getType(), "Eighth should be a project_user_group_added event");
	}

	@Test
	public void testGetUserProjectsRecentActivity() {
		Mockito.when(eventService.getEventsForUser(any(), any()))
				.thenReturn(createMockProjectEvents());
		Mockito.when(userService.getUserByUsername("FRED"))
				.thenReturn(user);
		Principal principal = () -> "FRED";
		PagedListResponse response = service.getRecentActivitiesForUser(0, Locale.ENGLISH, principal);
		assertEquals(4, response.getTotal(), "Should have 4 events");
		assertEquals("project_sample_added", ((Activity)response.getContent().get(0)).getType(), "First should be a project_sample_added event");
		assertEquals("project_user_role_updated", ((Activity)response.getContent().get(1)).getType(), "Second should be a project_user_role_updated event");
		assertEquals("project_user_removed", ((Activity)response.getContent().get(2)).getType(), "Third should be a project_user_removed event");
		assertEquals("project_user_group_added", ((Activity)response.getContent().get(3)).getType(), "Forth should be a project_user_group_added event");

		principal = () -> "FRED2";
		Mockito.when(userService.getUserByUsername("FRED2"))
				.thenReturn(user2);
		response = service.getRecentActivitiesForUser(0, Locale.ENGLISH, principal);
		assertEquals("project_sample_added", ((Activity)response.getContent().get(0)).getType(), "First should be a project_sample_added event");
		assertEquals("project_user_role_updated", ((Activity)response.getContent().get(1)).getType(), "Second should be a project_user_role_updated event");
		assertEquals("project_user_removed", ((Activity)response.getContent().get(2)).getType(), "Third should be a project_user_removed event");
		assertEquals("project_user_group_added", ((Activity)response.getContent().get(3)).getType(), "Forth should be a project_user_group_added event");
	}

	private Page<ProjectEvent> createMockProjectEvents() {
		ProjectSampleJoin join = new ProjectSampleJoin(project, sample, true);
		SampleAddedProjectEvent sampleAddedProjectEvent = new SampleAddedProjectEvent(join);

		ProjectUserJoin projectUserJoinUser = new ProjectUserJoin(project, user, ProjectRole.PROJECT_USER);
		UserRoleSetProjectEvent userRoleSetProjectEventUser = new UserRoleSetProjectEvent(projectUserJoinUser);

		UserRemovedProjectEvent userRemovedProjectEvent = new UserRemovedProjectEvent(project, user);

		UserGroup userGroup = new UserGroup("MY GROUP");
		UserGroupProjectJoin userGroupProjectJoin = new UserGroupProjectJoin(project, userGroup,
				ProjectRole.PROJECT_USER);
		UserGroupRoleSetProjectEvent userGroupRoleSetProjectEvent = new UserGroupRoleSetProjectEvent(
				userGroupProjectJoin);

		ProjectSampleJoin join2 = new ProjectSampleJoin(project2, sample2, true);
		SampleAddedProjectEvent sampleAddedProjectEvent2 = new SampleAddedProjectEvent(join2);

		ProjectUserJoin projectUserJoinUser2 = new ProjectUserJoin(project2, user2, ProjectRole.PROJECT_USER);
		UserRoleSetProjectEvent userRoleSetProjectEventUser2 = new UserRoleSetProjectEvent(projectUserJoinUser2);

		UserRemovedProjectEvent userRemovedProjectEvent2 = new UserRemovedProjectEvent(project2, user2);

		UserGroup userGroup2 = new UserGroup("MY GROUP");
		UserGroupProjectJoin userGroupProjectJoin2 = new UserGroupProjectJoin(project2, userGroup2,
				ProjectRole.PROJECT_USER);
		UserGroupRoleSetProjectEvent userGroupRoleSetProjectEvent2 = new UserGroupRoleSetProjectEvent(
				userGroupProjectJoin2);

		return new Page<ProjectEvent>() {
			@Override
			public int getTotalPages() {
				return 1;
			}

			@Override
			public long getTotalElements() {
				return 4;
			}

			@Override
			public <U> Page<U> map(Function<? super ProjectEvent, ? extends U> converter) {
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
				return 4;
			}

			@Override
			public List<ProjectEvent> getContent() {
				return ImmutableList.of(sampleAddedProjectEvent, userRoleSetProjectEventUser, userRemovedProjectEvent, userGroupRoleSetProjectEvent,
						sampleAddedProjectEvent2, userRoleSetProjectEventUser2, userRemovedProjectEvent2, userGroupRoleSetProjectEvent2);
			}

			@Override
			public boolean hasContent() {
				return false;
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
			public Iterator<ProjectEvent> iterator() {
				return null;
			}
		};
	}
}
