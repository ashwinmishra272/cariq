package com.cariq.backend.controller;

import com.cariq.backend.dto.RecommendRequest;
import com.cariq.backend.model.Car;
import com.cariq.backend.model.Session;
import com.cariq.backend.repository.CarRepository;
import com.cariq.backend.repository.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, properties = "openai.api.key=test-key")
@TestPropertySource(properties = "openai.api.key=test-key")
class RecommendControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CarRepository carRepository;

    @MockitoBean
    private SessionRepository sessionRepository;

    @Autowired
    private RecommendController recommendController;

    private MockMvc mockMvc;
    private HttpClient mockHttpClient;

    @SuppressWarnings("unchecked")
    private HttpResponse<String> mockHttpResponse;

    private Car swift;    // 6.65L  Petrol
    private Car creta;    // 18.5L  Petrol
    private Car nexon;    // 10.5L  Petrol
    private Car nexonEv;  // 19.95L Electric

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        mockHttpResponse = mock(HttpResponse.class);
        mockHttpClient   = mock(HttpClient.class);
        ReflectionTestUtils.setField(recommendController, "httpClient", mockHttpClient);

        swift   = car(1L, "Maruti",  "Swift",     665000.0, "Petrol");
        creta   = car(2L, "Hyundai", "Creta",    1850000.0, "Petrol");
        nexon   = car(3L, "Tata",    "Nexon",    1050000.0, "Petrol");
        nexonEv = car(4L, "Tata",    "Nexon EV", 1995000.0, "Electric");
    }

    // ── happy path ─────────────────────────────────────────────────────────────

    @Test
    void recommend_validRequest_returnsShortlistWithReasoningAndTradeoff() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexon, nexonEv));
        when(carRepository.findById(1L)).thenReturn(Optional.of(swift));
        when(carRepository.findById(3L)).thenReturn(Optional.of(nexon));
        stubOpenAi("{\"shortlist\":[\"1\",\"3\"],\"reasoning\":{\"1\":\"Great city mileage\",\"3\":\"Top safety score\"},\"tradeoff\":\"Swift is cheaper but Nexon is safer\"}");
        stubSession("session-abc");

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("10-20 lakhs", "city", "", "fuel efficiency"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId", is("session-abc")))
                .andExpect(jsonPath("$.shortlist", hasSize(2)))
                .andExpect(jsonPath("$.reasoning['1']", is("Great city mileage")))
                .andExpect(jsonPath("$.reasoning['3']", is("Top safety score")))
                .andExpect(jsonPath("$.tradeoff", containsString("cheaper")));
    }

    // ── budget parsing ─────────────────────────────────────────────────────────

    @Test
    void recommend_singleBudget_usesItAsUpperBound() throws Exception {
        // "8 lakhs" → max 8L = 800,000 → only swift (6.65L) passes the filter
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexon, nexonEv));
        when(carRepository.findById(1L)).thenReturn(Optional.of(swift));
        stubOpenAi("{\"shortlist\":[\"1\"],\"reasoning\":{\"1\":\"Only affordable option\"},\"tradeoff\":\"Very limited at this budget\"}");
        stubSession("s1");

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("8 lakhs", "city", "", "mileage"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortlist", hasSize(1)))
                .andExpect(jsonPath("$.shortlist[0].model", is("Swift")));
    }

    @Test
    void recommend_budgetRange_usesUpperBoundAsFilter() throws Exception {
        // "10-15 lakhs" → max 15L = 1,500,000 → swift + nexon qualify
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexon, nexonEv));
        when(carRepository.findById(1L)).thenReturn(Optional.of(swift));
        when(carRepository.findById(3L)).thenReturn(Optional.of(nexon));
        stubOpenAi("{\"shortlist\":[\"1\",\"3\"],\"reasoning\":{\"1\":\"Budget pick\",\"3\":\"Safe choice\"},\"tradeoff\":\"Nexon is safer\"}");
        stubSession("s2");

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("10-15 lakhs", "city/family", "NCAP", "safety"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortlist", hasSize(2)));
    }

    @Test
    void recommend_underBudgetPhrase_parsesCorrectly() throws Exception {
        // "under 20 lakhs" → max 20L → all 4 cars qualify (nexonEv = 19.95L < 20L)
        when(carRepository.findAll()).thenReturn(List.of(swift, creta, nexon, nexonEv));
        when(carRepository.findById(1L)).thenReturn(Optional.of(swift));
        stubOpenAi("{\"shortlist\":[\"1\"],\"reasoning\":{\"1\":\"Best value\"},\"tradeoff\":\"Plenty of options under 20L\"}");
        stubSession("s3");

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("under 20 lakhs", "city", "", "mileage"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortlist", hasSize(1)));
    }

    // ── error handling ─────────────────────────────────────────────────────────

    @Test
    void recommend_openAiThrowsException_returns500WithMessage() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift));
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("20 lakhs", "city", "", "safety"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Recommendation failed")));
    }

    @Test
    void recommend_openAiReturnsMalformedJson_returns500() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift));
        when(mockHttpResponse.body()).thenReturn(
                objectMapper.writeValueAsString(Map.of(
                        "choices", List.of(Map.of("message", Map.of("content", "not-valid-json")))
                ))
        );
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("20 lakhs", "city", "", "safety"))))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Recommendation failed")));
    }

    @Test
    void recommend_openAiReturnsUnknownCarId_shortlistIsEmpty() throws Exception {
        when(carRepository.findAll()).thenReturn(List.of(swift));
        when(carRepository.findById(99L)).thenReturn(Optional.empty());
        stubOpenAi("{\"shortlist\":[\"99\"],\"reasoning\":{\"99\":\"Phantom car\"},\"tradeoff\":\"No real options\"}");
        stubSession("s4");

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("20 lakhs", "city", "", "safety"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortlist", hasSize(0)));
    }

    @Test
    void recommend_noCarsMatchBudget_openAiCalledWithEmptyList() throws Exception {
        // creta and nexonEv both > 5L — nothing passes the filter
        when(carRepository.findAll()).thenReturn(List.of(creta, nexonEv));
        stubOpenAi("{\"shortlist\":[],\"reasoning\":{},\"tradeoff\":\"No cars fit this budget\"}");
        stubSession("s5");

        mockMvc.perform(post("/api/recommend")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req("5 lakhs", "city", "", "mileage"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shortlist", hasSize(0)));
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private void stubOpenAi(String innerContent) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of("message", Map.of("content", innerContent)))
        ));
        when(mockHttpResponse.body()).thenReturn(body);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockHttpResponse);
    }

    private void stubSession(String id) {
        Session session = new Session();
        session.setId(id);
        when(sessionRepository.save(any(Session.class))).thenReturn(session);
    }

    private RecommendRequest req(String budget, String use, String extra, String... priorities) {
        RecommendRequest r = new RecommendRequest();
        r.setBudget(budget);
        r.setUse(use);
        r.setPriorities(List.of(priorities));
        r.setExtra(extra);
        return r;
    }

    private Car car(Long id, String make, String model, Double price, String fuelType) {
        Car c = new Car();
        c.setId(id);
        c.setMake(make);
        c.setModel(model);
        c.setPrice(price);
        c.setFuelType(fuelType);
        return c;
    }
}