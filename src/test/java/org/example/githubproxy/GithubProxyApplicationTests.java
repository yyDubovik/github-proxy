package org.example.githubproxy;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "github.api.url"))
class GithubProxyApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUserRepositoriesExcludingForks() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/users/test-user/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                    {"name": "repo-1", "owner": {"login": "test-user"}, "fork": false},
                                    {"name": "repo-fork", "owner": {"login": "test-user"}, "fork": true}
                                ]
                                """)));

        stubFor(WireMock.get(urlPathEqualTo("/repos/test-user/repo-1/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [{"name": "main", "commit": {"sha": "sha123"}}]
                                """)));

        mockMvc.perform(get("/api/repos/test-user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].repositoryName").value("repo-1"))
                .andExpect(jsonPath("$[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha").value("sha123"));
    }

    @Test
    void shouldReturn404JsonWhenUserNotFound() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/users/unknown/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"message": "Not Found"}
                                """)));

        mockMvc.perform(get("/api/repos/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }
}