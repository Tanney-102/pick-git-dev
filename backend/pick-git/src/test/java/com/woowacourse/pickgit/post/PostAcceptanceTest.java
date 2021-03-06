package com.woowacourse.pickgit.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.woowacourse.pickgit.authentication.application.dto.OAuthProfileResponse;
import com.woowacourse.pickgit.authentication.domain.OAuthClient;
import com.woowacourse.pickgit.authentication.presentation.dto.OAuthTokenResponse;
import com.woowacourse.pickgit.common.FileFactory;
import com.woowacourse.pickgit.exception.dto.ApiErrorResponse;
import com.woowacourse.pickgit.post.application.dto.PostDto;
import com.woowacourse.pickgit.post.domain.dto.RepositoryResponseDto;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import(PostTestConfiguration.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PostAcceptanceTest {

    private static final String USERNAME = "jipark3";

    @LocalServerPort
    int port;

    @MockBean
    private OAuthClient oAuthClient;

    private List<MultipartFile> images;
    private String githubRepoUrl;
    private List<String> tags;
    private String content;

    private Map<String, Object> request;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        images = List.of(
            FileFactory.getTestImage1(),
            FileFactory.getTestImage2()
        );
        githubRepoUrl = "https://github.com/woowacourse-teams/2021-pick-git";
        tags = List.of("java", "spring");
        content = "this is content";

        Map<String, Object> body = new HashMap<>();
        body.put("githubRepoUrl", githubRepoUrl);
        body.put("tags", tags);
        body.put("content", content);
        request = body;
    }

    @DisplayName("???????????? ???????????? ????????????.")
    @Test
    void write_LoginUser_Success() {
        // given
        String token = ?????????_????????????().getToken();

        // when
        RestAssured
            .given().log().all()
            .auth().oauth2(token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .formParams(request)
            .multiPart("images", FileFactory.getTestImage1File())
            .multiPart("images", FileFactory.getTestImage2File())
            .when()
            .post("/api/posts")
            .then().log().all()
            .statusCode(HttpStatus.CREATED.value())
            .extract();
    }

    @DisplayName("???????????? ????????????. - ???????????? ?????? ????????? ????????? ????????? ????????????.")
    @Test
    void read_LoginUser_Success() {
        String token = ?????????_????????????().getToken();

        requestToWritePostApi(token, HttpStatus.CREATED);
        requestToWritePostApi(token, HttpStatus.CREATED);
        requestToWritePostApi(token, HttpStatus.CREATED);

        List<PostDto> response = RestAssured
            .given().log().all()
            .auth().oauth2(token)
            .when()
            .get("/api/posts?page=0&limit=3")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract()
            .as(new TypeRef<List<PostDto>>() {
            });

        assertThat(response).hasSize(3);
    }

    private ExtractableResponse<Response> requestToWritePostApi(String token, HttpStatus httpStatus) {
        return RestAssured
            .given().log().all()
            .auth().oauth2(token)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .formParams(request)
            .multiPart("images", FileFactory.getTestImage1File())
            .multiPart("images", FileFactory.getTestImage2File())
            .when()
            .post("/api/posts")
            .then().log().all()
            .statusCode(httpStatus.value())
            .extract();
    }

    @DisplayName("???????????? ???????????? ????????? ??? ??????. - ???????????? ?????? ????????? ?????? ?????? (Authorization header O)")
    @Test
    void write_GuestUserWithToken_Fail() {
        // given
        String token = "Bearer guest";

        // when
        requestToWritePostApi(token, HttpStatus.UNAUTHORIZED);
    }

    @DisplayName("???????????? ???????????? ????????? ??? ??????. - ????????? ?????? ?????? (Authorization header X)")
    @Test
    void write_GuestUserWithoutToken_Fail() {
        // when
        RestAssured
            .given().log().all()
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .formParams(request)
            .multiPart("images", FileFactory.getTestImage1File())
            .multiPart("images", FileFactory.getTestImage2File())
            .when()
            .post("/api/posts")
            .then().log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .extract();
    }

    @DisplayName("???????????? Repository ????????? ????????? ??? ??????.")
    @Test
    void showRepositories_LoginUser_Success() {
        // given
        String token = ?????????_????????????().getToken();

        // when
        List<RepositoryResponseDto> response =
            request(token, USERNAME, HttpStatus.OK.value())
                .as(new TypeRef<List<RepositoryResponseDto>>() {
                });

        // then
        assertThat(response).hasSize(2);
    }

    @DisplayName("????????? ???????????? ?????? ?????? ????????? ????????????. - 500 ??????")
    @Test
    void showRepositories_InvalidAccessToken_500Exception() {
        // given
        String token = ?????????_????????????().getToken();

        // when
        request(token + "hi", USERNAME, HttpStatus.UNAUTHORIZED.value());
    }

    @DisplayName("???????????? ???????????? ?????? ?????? ????????? ????????????. - 500 ??????")
    @Test
    void showRepositories_InvalidUsername_400Exception() {
        // given
        String token = ?????????_????????????().getToken();

        // when
        ApiErrorResponse response =
            request(token, USERNAME + "pika", HttpStatus.INTERNAL_SERVER_ERROR.value())
            .as(ApiErrorResponse.class);

        // then
        assertThat(response.getErrorCode()).isEqualTo("V0001");
    }

    private ExtractableResponse<Response> request(String token, String username, int statusCode) {
        return RestAssured
            .given().log().all()
            .auth().oauth2(token)
            .when()
            .get("/api/github/{username}/repositories", username)
            .then().log().all()
            .statusCode(statusCode)
            .extract();
    }

    private OAuthTokenResponse ?????????_????????????() {
        OAuthTokenResponse response = ?????????_??????().as(OAuthTokenResponse.class);
        assertThat(response.getToken()).isNotBlank();
        return response;
    }

    private ExtractableResponse<Response> ?????????_??????() {
        // given
        String oauthCode = "1234";
        String accessToken = "oauth.access.token";

        OAuthProfileResponse oAuthProfileResponse = new OAuthProfileResponse(
            "pick-git-login", "image", "hi~", "github.com/",
            null, null, null, null
        );

        // mock
        when(oAuthClient.getAccessToken(oauthCode)).thenReturn(accessToken);
        when(oAuthClient.getGithubProfile(accessToken)).thenReturn(oAuthProfileResponse);

        // when
        return RestAssured
            .given().log().all()
            .accept(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .get("/api/afterlogin?code=" + oauthCode)
            .then().log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();
    }
}
