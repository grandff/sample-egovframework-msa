package org.egovframe.cloud.userservice.api.role;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.assertj.core.api.Condition;
import org.egovframe.cloud.userservice.domain.role.Authorization;
import org.egovframe.cloud.userservice.domain.role.AuthorizationRepository;
import org.egovframe.cloud.userservice.domain.role.Role;
import org.egovframe.cloud.userservice.domain.role.RoleAuthorization;
import org.egovframe.cloud.userservice.domain.role.RoleAuthorizationId;
import org.egovframe.cloud.userservice.domain.role.RoleAuthorizationRepository;
import org.egovframe.cloud.userservice.domain.role.RoleRepository;
import org.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * org.egovframe.cloud.userservice.api.role.RoleAuthorizationApiControllerTest
 * <p>
 * ?????? ?????? Rest API ???????????? ????????? ?????????
 *
 * @author ??????????????????????????? jooho
 * @version 1.0
 * @since 2021/07/12
 *
 * <pre>
 * << ????????????(Modification Information) >>
 *
 *     ?????????        ?????????           ????????????
 *  ----------    --------    ---------------------------
 *  2021/07/12    jooho       ?????? ??????
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties
@TestPropertySource(properties = {"spring.config.location=classpath:application-test.yml"})
@ActiveProfiles(profiles = "test")
class RoleAuthorizationApiControllerTest {

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
     * ?????? ??????????????? ???????????????
     */
    @Autowired
    RoleRepository roleRepository;

    /**
     * ?????? ??????????????? ???????????????
     */
    @Autowired
    AuthorizationRepository authorizationRepository;

    /**
     * ?????? ?????? ??????????????? ???????????????
     */
    @Autowired
    RoleAuthorizationRepository roleAuthorizationRepository;

    /**
     * ?????? API ??????
     */
    private static final String URL = "/api/v1/role-authorizations";

    /**
     * ????????? ????????? ?????? ??????
     */
    private final Integer GIVEN_AUTHORIZATION_COUNT = 5;

    private final String ROLE_ID = "_ROLE_1";
    private final String ROLE_NAME = "?????? ???_1";
    private final String ROLE_CONTENT = "?????? ??????_1";

    private final String AUTHORIZATION_NAME_PREFIX = "?????? ???";
    private final String URL_PATTERN_VALUE_PREFIX = "/api/v1/test";
    private final String HTTP_METHOD_VALUE_PREFIX = "GET";

    /**
     * ????????? ?????????
     */
    private Role role = null;
    private final List<Authorization> authorizations = new ArrayList<>();
    private final List<RoleAuthorization> testDatas = new ArrayList<>();

    /**
     * ????????? ?????? ??? ??????
     */
    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new CharacterEncodingFilter("UTF-8"))
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // ?????? ??????
        role = roleRepository.save(Role.builder()
                .roleId(ROLE_ID)
                .roleName(ROLE_NAME)
                .roleContent(ROLE_CONTENT)
                .sortSeq(1)
                .build());

        // ?????? ??????
        for (int i = 1; i <= GIVEN_AUTHORIZATION_COUNT; i++) {
            authorizations.add(authorizationRepository.save(Authorization.builder()
                    .authorizationName(AUTHORIZATION_NAME_PREFIX + "_" + i)
                    .urlPatternValue(URL_PATTERN_VALUE_PREFIX + "_" + i)
                    .httpMethodCode(HTTP_METHOD_VALUE_PREFIX + "_" + i)
                    .sortSeq(i)
                    .build()));
        }
    }

    /**
     * ????????? ?????? ??? ??????
     */
    @AfterEach
    void tearDown() {
        // ?????? ??????
        authorizationRepository.deleteAll(authorizations);
        authorizations.clear();

        // ?????? ??????
        roleRepository.delete(role);
    }

    /**
     * ?????? ?????? ????????? ?????? ??????
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void ??????_??????_?????????_??????_??????() throws Exception {
        // given
        insertTestDatas();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("roleId", role.getRoleId());
        params.add("keywordType", "urlPatternValue");
        params.add("keyword", URL_PATTERN_VALUE_PREFIX);
        params.add("page", "0");
        params.add("size", "10");

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.get(URL)
                .params(params));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfElements").value(authorizations.size()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].roleId").value(role.getRoleId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].authorizationNo").value(authorizations.get(0).getAuthorizationNo()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].createdAt").value(true));

        deleteTestDatas();
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void ??????_??????_??????_??????() throws Exception {
        // given
        List<Map<String, Object>> requestDtoList = new ArrayList<>();
        for (int i = 1; i <= authorizations.size(); i++) {
            if (i % 2 == 0) continue; //????????? ??????

            Map<String, Object> params = new HashMap<>();
            params.put("roleId", role.getRoleId());
            params.put("authorizationNo", authorizations.get(i - 1).getAuthorizationNo());

            requestDtoList.add(params);
        }

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.post(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(requestDtoList)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());

        String responseData = resultActions.andReturn().getResponse().getContentAsString();
        JSONArray jsonArray = new JSONArray(responseData);

        assertThat(jsonArray.length()).isEqualTo(requestDtoList.size());

        List<RoleAuthorization> entityList = roleAuthorizationRepository.findAll(Sort.by(Sort.Direction.ASC, "roleAuthorizationId.authorizationNo"));
        for (int i = entityList.size() - 1; i >= 0; i--) {
            if (!entityList.get(i).getRoleAuthorizationId().getRoleId().equals(role.getRoleId())) {
                entityList.remove(i);
            }
        }
        assertThat(entityList).isNotNull();
        assertThat(entityList.size()).isEqualTo(requestDtoList.size());
        assertThat(entityList)
                .isNotEmpty()
                .has(new Condition<>(l -> l.get(0).getRoleAuthorizationId().getRoleId().equals(role.getRoleId()) && l.get(0).getRoleAuthorizationId().getAuthorizationNo().compareTo(authorizations.get(0).getAuthorizationNo()) == 0,
                        "RoleAuthorizationApiControllerTest.saveList authorizationNo eq 1"))
                .has(new Condition<>(l -> l.get(1).getRoleAuthorizationId().getRoleId().equals(role.getRoleId()) && l.get(1).getRoleAuthorizationId().getAuthorizationNo().compareTo(authorizations.get(2).getAuthorizationNo()) == 0,
                        "RoleAuthorizationApiControllerTest.saveList authorizationNo eq 3"));

        for (int i = entityList.size() - 1; i >= 0; i--) {
            deleteTestData(entityList.get(i).getRoleAuthorizationId().getRoleId(), entityList.get(i).getRoleAuthorizationId().getAuthorizationNo());
        }
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void ??????_??????_??????_??????() throws Exception {
        // given
        insertTestDatas();

        List<Map<String, Object>> requestDtoList = new ArrayList<>();
        for (RoleAuthorization testData : testDatas) {
            Map<String, Object> params = new HashMap<>();
            params.put("roleId", testData.getRoleAuthorizationId().getRoleId());
            params.put("authorizationNo", testData.getRoleAuthorizationId().getAuthorizationNo());

            requestDtoList.add(params);
        }

        // when
        ResultActions resultActions = mvc.perform(MockMvcRequestBuilders.put(URL)
                .accept(MediaType.APPLICATION_JSON)
                .contentType("application/json;charset=UTF-8")
                .content(objectMapper.writeValueAsString(requestDtoList)));

        // then
        resultActions
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        List<RoleAuthorization> entityList = roleAuthorizationRepository.findAll(Sort.by(Sort.Direction.ASC, "roleAuthorizationId.authorizationNo"));
        for (int i = entityList.size() - 1; i >= 0; i--) {
            if (!entityList.get(i).getRoleAuthorizationId().getRoleId().equals(role.getRoleId())) {
                entityList.remove(i);
            }
        }
        assertThat(entityList).isNotNull();
        assertThat(entityList.size()).isZero();
    }

    /**
     * ?????? ?????? ??????????????? ??????/?????? ?????????
     */
    @Test
    @Disabled
    void ??????_??????_??????_??????() {
        // given
        final Integer authorizationNo = authorizations.get(0).getAuthorizationNo();

        roleAuthorizationRepository.save(RoleAuthorization.builder()
                .roleId(role.getRoleId())
                .authorizationNo(authorizationNo)
                .build());

        // when
        Optional<RoleAuthorization> roleAuthorization = selectData(role.getRoleId(), authorizationNo);

        // then
        assertThat(roleAuthorization).isPresent();

        RoleAuthorization entity = roleAuthorization.get();
        assertThat(entity.getRoleAuthorizationId().getRoleId()).isEqualTo(role.getRoleId());
        assertThat(entity.getRoleAuthorizationId().getAuthorizationNo()).isEqualTo(authorizationNo);
    }

    /**
     * ????????? ????????? ??????
     */
    private void insertTestDatas() {
        // ?????? ?????? ??????
        for (int i = 1; i <= authorizations.size(); i++) {
            if (i % 2 == 0) continue; //?????? ?????? ????????? ??????

            testDatas.add(roleAuthorizationRepository.save(RoleAuthorization.builder()
                    .roleId(role.getRoleId())
                    .authorizationNo(authorizations.get(i - 1).getAuthorizationNo())
                    .build()));
        }
    }

    /**
     * ????????? ????????? ??????
     */
    private void deleteTestDatas() {
        // ?????? ?????? ??????
        roleAuthorizationRepository.deleteAll(testDatas);
        testDatas.clear();
    }

    /**
     * ????????? ????????? ?????? ??????
     */
    private void deleteTestData(String roleId, Integer authorizationNo) {
        roleAuthorizationRepository.deleteById(RoleAuthorizationId.builder()
                .roleId(roleId)
                .authorizationNo(authorizationNo)
                .build());
    }

    /**
     * ????????? ????????? ?????? ??????
     *
     * @param roleId          ?????? id
     * @param authorizationNo ?????? ??????
     * @return Optional<RoleAuthorization> ?????? ?????? ?????????
     */
    private Optional<RoleAuthorization> selectData(String roleId, Integer authorizationNo) {
        return roleAuthorizationRepository.findById(RoleAuthorizationId.builder()
                .roleId(roleId)
                .authorizationNo(authorizationNo)
                .build());
    }

}