package org.egovframe.cloud.userservice.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egovframe.cloud.common.domain.Role;
import org.egovframe.cloud.userservice.api.user.dto.UserResponseDto;
import org.egovframe.cloud.userservice.api.user.dto.UserSaveRequestDto;
import org.egovframe.cloud.userservice.api.user.dto.UserUpdateRequestDto;
import org.egovframe.cloud.userservice.domain.user.User;
import org.egovframe.cloud.userservice.domain.user.UserRepository;
import org.egovframe.cloud.userservice.domain.user.UserStateCode;
import org.egovframe.cloud.userservice.service.user.UserService;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
@ActiveProfiles(profiles = "test")
class UserApiControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TestRestTemplate restTemplate;

    //    private static final String USER_SERVICE_URL = "http://localhost:8000/user-service";
    private static final String TEST_COM = "@test.com";
    private static final String TEST_EMAIL = System.currentTimeMillis() + TEST_COM;
    private static final String TEST_PASSWORD = "test1234!";

    /**
     * API ??????
     */
    private static final String URL = "/api/v1/users";

    /**
     * WebApplicationContext
     */
    @Autowired
    private WebApplicationContext context;

    /**
     * MockMvc
     */
    private MockMvc mvc;

    /**
     * ObjectMapper
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * ????????? ?????????
     */
    private List<User> datas = new ArrayList<>();
    private final Integer GIVEN_DATA_COUNT = 10;
    private final String USER_NAME_PREFIX = "USER";
    private final String TOKEN = "1234567890";
    private final String DECRYPTED_PASSWORD = "test1234!";
    private final String ENCRYPTED_PASSWORD = "$2a$10$Xf9rt9ziTa3AXCuxG2TTruCC0RKCG62ukI6cHrptHnTMgCrviC8j.";

    /**
     * ????????? ?????? ??? ??????
     */
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8"))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @Order(Integer.MAX_VALUE)
    public void cleanup() {
        // ????????? ??? ????????? ??????
        List<User> users = userRepository.findByEmailContains("test.com");
        users.forEach(user -> userRepository.deleteById(user.getId()));
    }

    @Test
    @Order(Integer.MIN_VALUE)
    public void ?????????_????????????() {
        // given
        UserSaveRequestDto userSaveRequestDto = UserSaveRequestDto.builder()
                .userName("?????????")
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roleId(Role.USER.getKey())
                .userStateCode("01")
                .build();
        userService.save(userSaveRequestDto);

        // when
        UserResponseDto findUser = userService.findByEmail(TEST_EMAIL);

        // then
        assertThat(findUser.getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    @Order(2)
    public void ?????????_????????????() {
        // given
        UserResponseDto findUser = userService.findByEmail(TEST_EMAIL);
        UserUpdateRequestDto userUpdateRequestDto = UserUpdateRequestDto.builder()
                .userName("???????????????")
                .email(TEST_EMAIL)
                .roleId(Role.USER.getKey())
                .userStateCode("01")
                .build();

        // when
        userService.update(findUser.getUserId(), userUpdateRequestDto);
        UserResponseDto updatedUser = userService.findByEmail(TEST_EMAIL);

        // then
        assertThat(updatedUser.getUserName()).isEqualTo("???????????????");
    }

    @Test
    public void ?????????_????????????() {
        // given
        UserSaveRequestDto userSaveRequestDto = UserSaveRequestDto.builder()
                .userName("?????????")
                .email("email")
                .password("test")
                .build();

        String url = "/api/v1/users";

        RestClientException restClientException = Assertions.assertThrows(RestClientException.class, () -> {
            restTemplate.postForEntity(url, userSaveRequestDto, Long.class);
        });
        System.out.println("restClientException.getMessage() = " + restClientException.getMessage());
    }

    @Test
    public void ?????????_???????????????() throws Exception {
        // given
        JSONObject loginJson = new JSONObject();
        loginJson.put("email", TEST_EMAIL);
        loginJson.put("password", TEST_PASSWORD);

        String url = "/login";

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, loginJson.toString(), String.class);
        responseEntity.getHeaders().entrySet().forEach(System.out::println);
        assertThat(responseEntity.getHeaders().containsKey("access-token")).isTrue();
    }

    @Test
    public void ?????????_?????????_??????????????????() throws Exception {
        // given
        JSONObject loginJson = new JSONObject();
        loginJson.put("email", TEST_EMAIL);
        loginJson.put("password", "test");

        String url = "/login";

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, loginJson.toString(), String.class);
        System.out.println("responseEntity = " + responseEntity);
        assertThat(responseEntity.getHeaders().containsKey("access-token")).isFalse();
    }

    /**
     * ????????? ????????? ?????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void ?????????_?????????_??????_??????() throws Exception {
        // given
        insertUsers();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("keywordType", "userName");
        params.add("keyword", USER_NAME_PREFIX);
        params.add("page", "0");
        params.add("size", "10");

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(URL)
                .params(params));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(GIVEN_DATA_COUNT))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].userName").value(USER_NAME_PREFIX + "1"));

        deleteUsers();
    }

    /**
     * ????????? ?????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void ?????????_??????_??????() throws Exception {
        // given
        User entity = insertUser();

        final String userId = entity.getUserId();

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(URL + "/" + userId));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(userId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userName").value(entity.getUserName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(entity.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.roleId").value(entity.getRole().getKey()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userStateCode").value(entity.getUserStateCode()));

        deleteUser(entity.getId());
    }

    /**
     * ????????? ?????? ?????? ?????? ?????????
     * ?????? ???????????? ???????????? ?????? ?????????..
     */
    @Test
    void ?????????_??????_??????_??????_?????????() throws Exception {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("provider", "google");
        params.put("token", TOKEN);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL + "/social")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value())); // org.egovframe.cloud.common.exception.BusinessMessageException: ??????????????? ?????? ????????? ????????? ??? ????????????.
    }

    /**
     * ????????? ?????? ?????? ?????????
     */
    @Test
    void ?????????_??????_??????_?????????() throws Exception {
        // given
        User entity = insertUser();

        Map<String, Object> params = new HashMap<>();
        params.put("email", "1" + TEST_COM);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL + "/exists")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        deleteUser(entity.getId());
    }

    /**
     * ????????? ?????? ?????? ?????????
     */
    @Test
    void ?????????_??????_??????_?????????() throws Exception {
        // given
        final String email = "test_join" + TEST_COM;

        Map<String, Object> params = new HashMap<>();
        params.put("userName", USER_NAME_PREFIX + "1");
        params.put("email", email);
        params.put("password", DECRYPTED_PASSWORD);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL + "/join")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.content().string("true"));

//        userRepository.findByEmail("test_join" + TEST_COM).ifPresent(u -> deleteUser(u.getId()));

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(String.format("[%s]???????????? ????????????.", email)));
        assertThat(user.getUserName()).isEqualTo(USER_NAME_PREFIX + "1");
        assertThat(user.getEmail()).isEqualTo(email);

        deleteUser(user.getId());
    }

    /**
     * ????????? ???????????? ?????? ?????????
     */
    @Test
    void ?????????_????????????_??????_?????????() throws Exception {
        // given
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "give928@gmail.com");

        User user = insertUser(userData);

        Map<String, Object> params = new HashMap<>();
        params.put("userName", user.getUserName());
        params.put("emailAddr", user.getEmail());
        params.put("mainUrl", "http://localhost:4000");
        params.put("changePasswordUrl", "http://localhost:4000/user/password/change");

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL + "/password/find")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // ?????? ?????? ????????? ???????????? ???????????? ??????.

        deleteUser(user.getId());
    }

    /**
     * ????????? ???????????? ?????? ????????? ?????? ?????????
     */
    @Test
    void ?????????_????????????_??????_?????????_??????_?????????() throws Exception {
        // given
        final String token = TOKEN;

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(URL + "/password/valid/" + token)
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8"));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("false"));
    }

    /**
     * ????????? ???????????? ?????? ?????? ?????????
     */
    @Test
    void ?????????_????????????_??????_??????_?????????() throws Exception {
        // given
        Map<String, Object> params = new HashMap<>();
        params.put("tokenValue", TOKEN);
        params.put("password", DECRYPTED_PASSWORD);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put(URL + "/password/change")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value())); // org.egovframe.cloud.common.exception.BusinessMessageException: ??????????????? ?????????????????????. ???????????? ?????? ?????????????????? ????????????.
    }

    /**
     * ????????? ???????????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "USER", username = "test-user")
    void ?????????_????????????_??????_?????????() throws Exception {
        // given
        final String userId = "test-user";

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);

        User entity = insertUser(userData);

        final Long userNo = entity.getId();

        Map<String, Object> params = new HashMap<>();
        params.put("provider", "password");
        params.put("password", DECRYPTED_PASSWORD);
        params.put("newPassword", "P@ssw0rd1");

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put(URL + "/password/update")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        User user = selectUser(userNo).orElseThrow(() -> new UsernameNotFoundException(String.format("[%d]???????????? ????????????.", userNo)));
        assertThat(user.getEncryptedPassword()).isNotEqualTo(entity.getEncryptedPassword());

        deleteUser(entity.getId());
    }

    /**
     * ????????? ???????????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "USER", username = "test-user")
    void ?????????_????????????_??????_?????????() throws Exception {
        // given
        final String userId = "test-user";

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);

        User entity = insertUser(userData);

        Map<String, Object> params = new HashMap<>();
        params.put("password", DECRYPTED_PASSWORD);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL + "/password/match")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        deleteUser(entity.getId());
    }

    /**
     * ????????? ???????????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "USER", username = "test-user")
    void ?????????_????????????_??????_?????????() throws Exception {
        // given
        final String userId = "test-user";

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);

        User entity = insertUser(userData);

        final Long userNo = entity.getId();
        final String userName = "TEST-USER";
        final String email = "2" + TEST_COM;

        Map<String, Object> params = new HashMap<>();
        params.put("provider", "password");
        params.put("password", DECRYPTED_PASSWORD);
        params.put("userName", userName);
        params.put("email", email);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put(URL + "/info/" + userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(userId));

        User user = selectUser(userNo).orElseThrow(() -> new UsernameNotFoundException(String.format("[%d]???????????? ????????????.", userNo)));
        assertThat(user.getUserName()).isEqualTo(userName);
        assertThat(user.getEmail()).isEqualTo(email);

        deleteUser(entity.getId());
    }

    /**
     * ????????? ?????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "USER", username = "test-user")
    void ?????????_??????_??????_?????????() throws Exception {
        // given
        final String userId = "test-user";

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);

        User entity = insertUser(userData);

        final Long userNo = entity.getId();

        Map<String, Object> params = new HashMap<>();
        params.put("provider", "password");
        params.put("password", DECRYPTED_PASSWORD);

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL + "/leave")
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(params)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        User user = selectUser(userNo).orElseThrow(() -> new UsernameNotFoundException(String.format("[%d]???????????? ????????????.", userNo)));
        assertThat(user.getUserStateCode()).isEqualTo(UserStateCode.LEAVE.getKey());

        deleteUser(entity.getId());
    }

    /**
     * ????????? ?????? ?????????
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void ?????????_??????_?????????() throws Exception {
        // given
        final String userId = "test-user";

        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);

        User entity = insertUser(userData);

        final Long userNo = entity.getId();

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.delete(URL + "/delete/" + userId)
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8"));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        User user = selectUser(userNo).orElseThrow(() -> new UsernameNotFoundException(String.format("[%d]???????????? ????????????.", userNo)));
        assertThat(user.getUserStateCode()).isEqualTo(UserStateCode.DELETE.getKey());

        deleteUser(entity.getId());
    }

    /**
     * ????????? ????????? ??????
     */
    private void insertUsers() {
        for (int i = 1; i <= GIVEN_DATA_COUNT; i++) {
            datas.add(userRepository.save(User.builder()
                    .userId(UUID.randomUUID().toString())
                    .encryptedPassword(ENCRYPTED_PASSWORD)
                    .userName(USER_NAME_PREFIX + i)
                    .email(i + TEST_COM)
                    .role(Role.USER)
                    .userStateCode(UserStateCode.NORMAL.getKey())
                    .build()));
        }
    }

    /**
     * ????????? ????????? ??????
     */
    private void deleteUsers() {
        if (datas != null) {
            if (!datas.isEmpty()) userRepository.deleteAll(datas);

            datas.clear();
        }
    }

    /**
     * ????????? ????????? ?????? ??????
     *
     * @return User ????????? ?????????
     */
    private User insertUser() {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", "1" + TEST_COM);

        return insertUser(userData);
    }
    private User insertUser(Map<String, Object> userData) {
        return userRepository.save(User.builder()
                .userId(userData.get("userId") != null ? (String) userData.get("userId") : UUID.randomUUID().toString())
                .encryptedPassword(ENCRYPTED_PASSWORD)
                .userName(USER_NAME_PREFIX + "1")
                .email(userData.get("email") != null ? (String) userData.get("email") : "1" + TEST_COM)
                .role(Role.USER)
                .userStateCode(UserStateCode.NORMAL.getKey())
                .build());
    }

    /**
     * ????????? ????????? ?????? ??????
     */
    private void deleteUser(Long userNo) {
        userRepository.deleteById(userNo);
    }

    /**
     * ????????? ????????? ?????? ??????
     *
     * @param userNo ????????? ??????
     * @return Optional<User> ?????????
     */
    private Optional<User> selectUser(Long userNo) {
        return userRepository.findById(userNo);
    }

}