package com.example.hackathonseal.services;

import com.example.hackathonseal.exception.AppException;
import com.example.hackathonseal.models.Enum.ErrorCode;
import com.example.hackathonseal.models.Enum.UserRole;
import com.example.hackathonseal.models.dto.request.CategoryRequest;
import com.example.hackathonseal.models.dto.response.CategoryResponse;
import com.example.hackathonseal.models.entity.Category;
import com.example.hackathonseal.models.entity.Event;
import com.example.hackathonseal.models.entity.User;
import com.example.hackathonseal.repo.CategoryRepository;
import com.example.hackathonseal.repo.EventRepository;
import com.example.hackathonseal.repo.JudgeAssignmentRepository;
import com.example.hackathonseal.repo.UserRepository;
import com.example.hackathonseal.repo.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JudgeAssignmentRepository judgeAssignmentRepository;

    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private Event event;
    private Category category;
    private User mentor;
    private User judge;

    @BeforeEach
    void setUp() {
        event = Event.builder().id(1L).build();

        mentor = User.builder()
                .id(10L)
                .email("mentor@gmail.com")
                .fullName("Mentor User")
                .role(UserRole.MENTOR)
                .build();

        judge = User.builder()
                .id(20L)
                .email("judge@gmail.com")
                .fullName("Judge User")
                .role(UserRole.JUDGE)
                .build();

        category = Category.builder()
                .id(5L)
                .name("Software Engineering")
                .event(event)
                .mentors(new ArrayList<>())
                .judges(new ArrayList<>())
                .build();

        category.getMentors().add(mentor);
        category.getJudges().add(judge);
    }

    @Test
    void unassignMentorFromCategory_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(userRepository.findById(10L)).thenReturn(Optional.of(mentor));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(category.getMentors().contains(mentor));

        CategoryResponse response = categoryService.unassignMentorFromCategory(1L, 5L, 10L);

        assertNotNull(response);
        assertEquals(0, response.getMentors().size());
        assertFalse(category.getMentors().contains(mentor));
    }

    @Test
    void unassignJudgeFromCategory_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(userRepository.findById(20L)).thenReturn(Optional.of(judge));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(judgeAssignmentRepository).deleteByCategoryIdAndJudgeId(5L, 20L);

        assertTrue(category.getJudges().contains(judge));

        CategoryResponse response = categoryService.unassignJudgeFromCategory(1L, 5L, 20L);

        assertNotNull(response);
        assertEquals(0, response.getJudges().size());
        assertFalse(category.getJudges().contains(judge));
        verify(judgeAssignmentRepository, times(1)).deleteByCategoryIdAndJudgeId(5L, 20L);
    }

    @Test
    void unassignMentorFromCategory_NotBelongToEvent_ThrowsException() {
        Event otherEvent = Event.builder().id(2L).build();
        category.setEvent(otherEvent);

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        AppException exception = assertThrows(AppException.class, () ->
                categoryService.unassignMentorFromCategory(1L, 5L, 10L)
        );

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND.getCode(), exception.getErrorCode());
        assertEquals("Category does not belong to this event", exception.getMessage());
    }

    @Test
    void updateCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Updated Category Name");
        request.setDescription("Updated Description");

        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.updateCategory(1L, 5L, request);

        assertNotNull(response);
        assertEquals("Updated Category Name", response.getName());
        assertEquals("Updated Description", response.getDescription());
    }

    @Test
    void deleteCategory_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(teamRepository.existsByCategoryId(5L)).thenReturn(false);
        doNothing().when(judgeAssignmentRepository).deleteByCategoryId(5L);
        doNothing().when(categoryRepository).delete(any(Category.class));

        assertDoesNotThrow(() -> categoryService.deleteCategory(1L, 5L));

        verify(judgeAssignmentRepository, times(1)).deleteByCategoryId(5L);
        verify(categoryRepository, times(1)).delete(category);
    }

    @Test
    void deleteCategory_InUseByTeams_ThrowsException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(teamRepository.existsByCategoryId(5L)).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () ->
                categoryService.deleteCategory(1L, 5L)
        );

        assertEquals(ErrorCode.CATEGORY_IN_USE.getCode(), exception.getErrorCode());
    }
}
