package org.example.githubproxy;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
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
        int delayMs = 1000;

        stubFor(WireMock.get(urlPathEqualTo("/users/test-user/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(delayMs)
                        .withBody("""
                                [
                                    {"name": "repo-1", "owner": {"login": "test-user"}, "fork": false},
                                    {"name": "repo-fork", "owner": {"login": "test-user"}, "fork": true},
                                    {"name": "repo-2", "owner": {"login": "test-user"}, "fork": false}
                                ]
                                """)));

        stubFor(WireMock.get(urlPathEqualTo("/repos/test-user/repo-1/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(delayMs)
                        .withBody("""
                                [{"name": "main", "commit": {"sha": "sha123"}}]
                                """)));

        stubFor(WireMock.get(urlPathEqualTo("/repos/test-user/repo-2/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(delayMs)
                        .withBody("""
                                [{"name": "main", "commit": {"sha": "sha456"}}]
                                """)));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        mockMvc.perform(get("/api/repos/test-user")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].repositoryName").value("repo-1"))
                .andExpect(jsonPath("$[1].repositoryName").value("repo-2"))
                .andExpect(jsonPath("$[0].branches[0].name").value("main"))
                .andExpect(jsonPath("$[0].branches[0].lastCommitSha").value("sha123"));

        stopWatch.stop();
        long time = stopWatch.getTime();
        System.out.println("Time: " + time);

        verify(3, getRequestedFor(urlMatching(".*")));
        assertThat(time).isBetween(2000L, 3100L);
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