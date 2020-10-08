package com.thoughtworks.rslist.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.User;
import com.thoughtworks.rslist.dto.RsEventDto;
import com.thoughtworks.rslist.dto.TradeDto;
import com.thoughtworks.rslist.dto.UserDto;
import com.thoughtworks.rslist.dto.VoteDto;
import com.thoughtworks.rslist.repository.RsEventRepository;
import com.thoughtworks.rslist.repository.TradeRepository;
import com.thoughtworks.rslist.repository.UserRepository;
import com.thoughtworks.rslist.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RsControllerTest {
  @Autowired private MockMvc mockMvc;
  ObjectMapper objectMapper;
  @Autowired UserRepository userRepository;
  @Autowired RsEventRepository rsEventRepository;
  @Autowired VoteRepository voteRepository;
  @Autowired TradeRepository tradeRepository;
  private UserDto userDto;

  @BeforeEach
  void setUp() {
    voteRepository.deleteAll();
    rsEventRepository.deleteAll();
    userRepository.deleteAll();
    tradeRepository.deleteAll();
    userDto =
        UserDto.builder()
            .voteNum(10)
            .phone("188888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("idolice")
            .build();
  }

  @Test
  public void shouldGetRsEventList() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);

    mockMvc
        .perform(get("/rs/list"))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[0]", not(hasKey("user"))))
        .andExpect(status().isOk());
  }

  @Test
  public void shouldGetOneEvent() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.eventName", is("第一条事件")));
    mockMvc.perform(get("/rs/1")).andExpect(jsonPath("$.keyword", is("无分类")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.eventName", is("第二条事件")));
    mockMvc.perform(get("/rs/2")).andExpect(jsonPath("$.keyword", is("无分类")));
  }

  @Test
  public void shouldGetErrorWhenIndexInvalid() throws Exception {
    mockMvc
        .perform(get("/rs/4"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error", is("invalid index")));
  }

  @Test
  public void shouldGetRsListBetween() throws Exception {
    UserDto save = userRepository.save(userDto);

    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();

    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第二条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    rsEventDto = RsEventDto.builder().keyword("无分类").eventName("第三条事件").user(save).build();
    rsEventRepository.save(rsEventDto);
    mockMvc
        .perform(get("/rs/list?start=1&end=2"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第一条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=2&end=3"))
        .andExpect(jsonPath("$", hasSize(2)))
        .andExpect(jsonPath("$[0].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")));
    mockMvc
        .perform(get("/rs/list?start=1&end=3"))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].keyword", is("无分类")))
        .andExpect(jsonPath("$[1].eventName", is("第二条事件")))
        .andExpect(jsonPath("$[1].keyword", is("无分类")))
        .andExpect(jsonPath("$[2].eventName", is("第三条事件")))
        .andExpect(jsonPath("$[2].keyword", is("无分类")));
  }

  @Test
  public void shouldAddRsEventWhenUserExist() throws Exception {

    UserDto save = userRepository.save(userDto);

    String jsonValue =
        "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": " + save.getId() + "}";

    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated());
    List<RsEventDto> all = rsEventRepository.findAll();
    assertNotNull(all);
    assertEquals(all.size(), 1);
    assertEquals(all.get(0).getEventName(), "猪肉涨价了");
    assertEquals(all.get(0).getKeyword(), "经济");
    assertEquals(all.get(0).getUser().getUserName(), save.getUserName());
    assertEquals(all.get(0).getUser().getAge(), save.getAge());
  }

  @Test
  public void shouldAddRsEventWhenUserNotExist() throws Exception {
    String jsonValue = "{\"eventName\":\"猪肉涨价了\",\"keyword\":\"经济\",\"userId\": 100}";
    mockMvc
        .perform(post("/rs/event").content(jsonValue).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldVoteSuccess() throws Exception {
    UserDto save = userRepository.save(userDto);
    RsEventDto rsEventDto =
        RsEventDto.builder().keyword("无分类").eventName("第一条事件").user(save).build();
    rsEventDto = rsEventRepository.save(rsEventDto);

    String jsonValue =
        String.format(
            "{\"userId\":%d,\"time\":\"%s\",\"voteNum\":1}",
            save.getId(), LocalDateTime.now().toString());
    mockMvc
        .perform(
            post("/rs/vote/{id}", rsEventDto.getId())
                .content(jsonValue)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());

    UserDto userDto = userRepository.findById(save.getId()).get();
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(userDto.getVoteNum(), 9);
    assertEquals(newRsEvent.getVoteNum(), 1);
    List<VoteDto> voteDtos =  voteRepository.findAll();
    assertEquals(voteDtos.size(), 1);
    assertEquals(voteDtos.get(0).getNum(), 1);
  }

  @Test
  public void shouldRepeatBuySomeRsEventSuccess() throws Exception {
    userRepository.save(userDto);
    RsEventDto rsEventDto = RsEventDto.builder().eventName("热搜1").keyword("hots").rank(1).voteNum(10).user(userDto).build();
    rsEventDto =rsEventRepository.save(rsEventDto);
    TradeDto tradeDto = TradeDto.builder().amount(8).rank(1).rsEvent(rsEventDto).build();
    tradeRepository.save(tradeDto);
    String jsonValue =
            String.format(
                    "{\"amount\":10,\"rank\":1}");
    mockMvc.perform(post("/rs/buy/{id}",rsEventDto.getId())
            .content(jsonValue)
            .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isOk());
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto.getId()).get();
    assertEquals(newRsEvent.getRank(), 1);
    List<TradeDto> tradeDtos =  tradeRepository.findAll();
    assertEquals(tradeDtos.size(), 1);
    assertEquals(tradeDtos.get(0).getAmount(), 10);
  }

  @Test
  public void shouldFirstBuyRsEventSuccess() throws Exception {
    RsEventDto rsEventDto = RsEventDto.builder().eventName("热搜1").keyword("hots").rank(1).voteNum(10).build();
    rsEventDto =rsEventRepository.save(rsEventDto);
    TradeDto tradeDto = TradeDto.builder().amount(8).rank(1).rsEvent(rsEventDto).build();
    tradeRepository.save(tradeDto);

    RsEventDto rsEventDto1= RsEventDto.builder().eventName("热搜2").keyword("hots").voteNum(10).build();
    rsEventDto1 =rsEventRepository.save(rsEventDto1);


    String jsonValue =
            String.format(
                    "{\"amount\":10,\"rank\":2}");
    mockMvc.perform(post("/rs/buy/{id}",rsEventDto1.getId())
            .content(jsonValue)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto1.getId()).get();
    assertEquals(newRsEvent.getRank(), 2);
    List<TradeDto> tradeDtos =  tradeRepository.findAll();
    assertEquals(tradeDtos.size(), 2);
    assertEquals(tradeDtos.get(1).getAmount(), 10);
  }

  @Test
  public void shouldBuyRsEventSuccess() throws Exception {
    RsEventDto rsEventDto = RsEventDto.builder().eventName("热搜1").keyword("hots").rank(1).voteNum(10).build();
    rsEventDto =rsEventRepository.save(rsEventDto);
    TradeDto tradeDto = TradeDto.builder().amount(8).rank(1).rsEvent(rsEventDto).build();
    tradeRepository.save(tradeDto);

    RsEventDto rsEventDto1= RsEventDto.builder().eventName("热搜2").keyword("hots").voteNum(10).build();
    rsEventDto1 =rsEventRepository.save(rsEventDto1);


    String jsonValue =
            String.format(
                    "{\"amount\":10,\"rank\":1}");
    mockMvc.perform(post("/rs/buy/{id}",rsEventDto1.getId())
            .content(jsonValue)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    RsEventDto newRsEvent = rsEventRepository.findById(rsEventDto1.getId()).get();
    assertEquals(newRsEvent.getRank(), 1);
    List<TradeDto> tradeDtos =  tradeRepository.findAll();
    assertEquals(tradeDtos.size(), 1);
    assertEquals(tradeDtos.get(0).getAmount(), 10);
  }

  @Test
  public void shouldBuyRsEventFailureWhenRsEventIsNull() throws Exception {
    String jsonValue =
            String.format(
                    "{\"amount\":10,\"rank\":1}");
    mockMvc.perform(post("/rs/buy/{id}",4)
            .content(jsonValue)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldBuyRsEventFailureWhenAccountIsSmall() throws Exception {
    RsEventDto rsEventDto = RsEventDto.builder().eventName("热搜3").keyword("hots").rank(1).voteNum(10).build();
    rsEventDto =rsEventRepository.save(rsEventDto);
    TradeDto tradeDto = TradeDto.builder().amount(100).rank(1).rsEvent(rsEventDto).build();
    tradeRepository.save(tradeDto);

    RsEventDto rsEventDto1= RsEventDto.builder().eventName("热搜4").keyword("hots").voteNum(10).build();
    rsEventDto1 =rsEventRepository.save(rsEventDto1);
    String jsonValue =
            String.format(
                    "{\"amount\":10,\"rank\":1}");
    mockMvc.perform(post("/rs/buy/{id}",rsEventDto1.getId())
            .content(jsonValue)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }

  @Test
  public void shouldBuyRsEventFailureWhenRankIsLarger() throws Exception {
    for (int i = 1; i < 5; i++) {
      RsEventDto rsEventDto = RsEventDto.builder().eventName("热搜"+i).keyword("hots").rank(i).voteNum(10).build();
      rsEventDto =rsEventRepository.save(rsEventDto);
      TradeDto tradeDto = TradeDto.builder().amount(10+i).rank(i).rsEvent(rsEventDto).build();
      tradeRepository.save(tradeDto);
    }

    RsEventDto rsEventDto1= RsEventDto.builder().eventName("热搜").keyword("hots").voteNum(10).build();
    rsEventDto1 =rsEventRepository.save(rsEventDto1);
    String jsonValue =
            String.format(
                    "{\"amount\":100,\"rank\":20}");
    mockMvc.perform(post("/rs/buy/{id}",rsEventDto1.getId())
            .content(jsonValue)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
  }
}
