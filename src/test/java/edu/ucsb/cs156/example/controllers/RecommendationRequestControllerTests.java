package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  private RecommendationRequest sampleRequest() {
    return RecommendationRequest.builder()
        .requesterEmail("student@ucsb.edu")
        .professorEmail("professor@ucsb.edu")
        .explanation("Graduate school recommendation")
        .dateRequested(LocalDateTime.parse("2026-10-20T12:00:00"))
        .dateNeeded(LocalDateTime.parse("2026-12-01T12:00:00"))
        .done(false)
        .build();
  }

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    List<RecommendationRequest> requests = Arrays.asList(sampleRequest());

    when(recommendationRequestRepository.findAll()).thenReturn(requests);

    MvcResult result =
        mockMvc
            .perform(get("/api/RecommendationRequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();

    assertEquals(mapper.writeValueAsString(requests), result.getResponse().getContentAsString());
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "Graduate school recommendation")
                .param("dateRequested", "2026-10-20T12:00:00")
                .param("dateNeeded", "2026-12-01T12:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "student@ucsb.edu")
                .param("professorEmail", "professor@ucsb.edu")
                .param("explanation", "Graduate school recommendation")
                .param("dateRequested", "2026-10-20T12:00:00")
                .param("dateNeeded", "2026-12-01T12:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post() throws Exception {
    RecommendationRequest request = sampleRequest();
    request.setDone(true);

    when(recommendationRequestRepository.save(eq(request))).thenReturn(request);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/RecommendationRequest/post")
                    .param("requesterEmail", "student@ucsb.edu")
                    .param("professorEmail", "professor@ucsb.edu")
                    .param("explanation", "Graduate school recommendation")
                    .param("dateRequested", "2026-10-20T12:00:00")
                    .param("dateNeeded", "2026-12-01T12:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(eq(request));

    assertEquals(mapper.writeValueAsString(request), result.getResponse().getContentAsString());
  }
}
