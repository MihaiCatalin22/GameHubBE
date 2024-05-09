package com.gamehub.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamehub.backend.configuration.security.CustomUserDetails;
import com.gamehub.backend.domain.Game;
import com.gamehub.backend.domain.Genre;
import com.gamehub.backend.domain.Purchase;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.PurchaseDTO;
import com.gamehub.backend.persistence.GameRepository;
import com.gamehub.backend.persistence.PurchaseRepository;
import com.gamehub.backend.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PurchaseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PurchaseRepository purchaseRepository;

    @MockBean
    private GameRepository gameRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private Game sampleGame;
    private User sampleUser;
    private Purchase samplePurchase;

    @BeforeEach
    void setup() {
        sampleGame = new Game();
        sampleGame.setId(1L);
        sampleGame.setTitle("Elden Ring");
        sampleGame.setDescription("A challenging action RPG.");
        sampleGame.setGenres(new HashSet<>(Arrays.asList(Genre.ACTION, Genre.RPG)));
        sampleGame.setReleaseDate(new Date());
        sampleGame.setDeveloper("FromSoftware");
        sampleGame.setPrice(59.99);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("TestUser");
        sampleUser.setPasswordHash(passwordEncoder.encode("password123"));
        sampleUser.setPurchases(new ArrayList<>());

        samplePurchase = new Purchase();
        samplePurchase.setId(1L);
        samplePurchase.setUser(sampleUser);
        samplePurchase.setGame(sampleGame);
        samplePurchase.setAmount(59.99);
        samplePurchase.setPurchaseDate(new Date());

        sampleUser.getPurchases().add(samplePurchase);

        when(gameRepository.findById(1L)).thenReturn(Optional.of(sampleGame));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(samplePurchase);
    }

    private void setupAuthentication() {
        User user = new User();
        user.setId(1L);
        user.setUsername("username");
        user.setPasswordHash(passwordEncoder.encode("password"));
        when(userRepository.save(user)).thenReturn(user);

        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
        CustomUserDetails userDetails = new CustomUserDetails(user.getId(), user.getUsername(), user.getPasswordHash(), authorities);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Game game = new Game();
        game.setId(1L);
        game.setTitle("Elden Ring");
        game.setPrice(59.99);
        when(gameRepository.findById(1L)).thenReturn(Optional.of(game));

        Purchase purchase = new Purchase();
        purchase.setId(1L);
        purchase.setUser(user);
        purchase.setGame(game);
        purchase.setAmount(59.99);
        purchase.setPurchaseDate(new Date());
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(purchase);
    }

    @Test
    void purchaseGameTest() throws Exception {
        setupAuthentication();

        mockMvc.perform(post("/purchases/{userId}/game/{gameId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$.amount").value(59.99));
    }

    @Test
    void getPurchasesTest() throws Exception {
        setupAuthentication();
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L);
        String fromDateFormatted = DATE_FORMATTER.format(fromDate);

        PurchaseDTO purchaseDTO = new PurchaseDTO(
                1L,
                "Elden Ring",
                59.99,
                fromDate
        );

        when(purchaseRepository.findByUserIdAndPurchaseDateAfter(1L, fromDate)).thenReturn(List.of(samplePurchase));

        mockMvc.perform(get("/purchases/{userId}", 1L)
                        .param("fromDate", fromDateFormatted))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$[0].amount").value(59.99));
    }

    @Test
    void getPurchasesWithMinAmountTest() throws Exception {
        setupAuthentication();
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L);
        String fromDateFormatted = DATE_FORMATTER.format(fromDate);

        PurchaseDTO purchaseDTO = new PurchaseDTO(
                1L,
                "Elden Ring",
                59.99,
                fromDate
        );

        when(purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountGreaterThanEqual(1L, fromDate, 10.0))
                .thenReturn(List.of(samplePurchase));

        mockMvc.perform(get("/purchases/{userId}", 1L)
                        .param("fromDate", fromDateFormatted)
                        .param("minAmount", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$[0].amount").value(59.99));
    }

    @Test
    void getPurchasesWithMaxAmountTest() throws Exception {
        setupAuthentication();
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L);
        String fromDateFormatted = DATE_FORMATTER.format(fromDate);

        PurchaseDTO purchaseDTO = new PurchaseDTO(
                1L,
                "Elden Ring",
                59.99,
                fromDate
        );

        when(purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountLessThan(1L, fromDate, 100.0))
                .thenReturn(List.of(samplePurchase));

        mockMvc.perform(get("/purchases/{userId}", 1L)
                        .param("fromDate", fromDateFormatted)
                        .param("maxAmount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$[0].amount").value(59.99));
    }

    @Test
    void getPurchasesWithinRangeTest() throws Exception {
        setupAuthentication();
        Date fromDate = new Date(System.currentTimeMillis() - 86400000L);
        String fromDateFormatted = DATE_FORMATTER.format(fromDate);

        PurchaseDTO purchaseDTO = new PurchaseDTO(
                1L,
                "Elden Ring",
                59.99,
                fromDate
        );

        when(purchaseRepository.findByUserIdAndPurchaseDateAfterAndAmountBetween(1L, fromDate, 10.0, 100.0))
                .thenReturn(List.of(samplePurchase));

        mockMvc.perform(get("/purchases/{userId}", 1L)
                        .param("fromDate", fromDateFormatted)
                        .param("minAmount", "10.0")
                        .param("maxAmount", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gameTitle").value("Elden Ring"))
                .andExpect(jsonPath("$[0].amount").value(59.99));
    }
}