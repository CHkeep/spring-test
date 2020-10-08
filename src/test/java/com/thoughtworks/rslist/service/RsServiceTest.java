package com.thoughtworks.rslist.service;

import com.thoughtworks.rslist.domain.Trade;
import com.thoughtworks.rslist.domain.Vote;
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
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RsServiceTest {
  RsService rsService;

  @Mock RsEventRepository rsEventRepository;
  @Mock UserRepository userRepository;
  @Mock VoteRepository voteRepository;
  @Mock TradeRepository tradeRepository;
  @Mock ResponseEntity responseEntity;
  LocalDateTime localDateTime;
  Vote vote;
  Trade trade;

  @BeforeEach
  void setUp() {
    initMocks(this);
    rsService = new RsService(rsEventRepository, userRepository, voteRepository,tradeRepository);
    localDateTime = LocalDateTime.now();
    vote = Vote.builder().voteNum(2).rsEventId(1).time(localDateTime).userId(1).build();
  }

  @Test
  void shouldVoteSuccess() {
    // given
    UserDto userDto =
        UserDto.builder()
            .voteNum(5)
            .phone("18888888888")
            .gender("female")
            .email("a@b.com")
            .age(19)
            .userName("xiaoli")
            .id(2)
            .build();
    RsEventDto rsEventDto =
        RsEventDto.builder()
            .eventName("event name")
            .id(1)
            .keyword("keyword")
            .voteNum(2)
            .user(userDto)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(userRepository.findById(anyInt())).thenReturn(Optional.of(userDto));
    // when
    rsService.vote(vote, 1);
    // then
    verify(voteRepository)
        .save(
            VoteDto.builder()
                .num(2)
                .localDateTime(localDateTime)
                .user(userDto)
                .rsEvent(rsEventDto)
                .build());
    verify(userRepository).save(userDto);
    verify(rsEventRepository).save(rsEventDto);
  }

  @Test
  void shouldThrowExceptionWhenUserNotExist() {
    // given
    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(userRepository.findById(anyInt())).thenReturn(Optional.empty());
    //when&then
    assertThrows(
        RuntimeException.class,
        () -> {
          rsService.vote(vote, 1);
        });
  }

  @Test
  void shouldRepeatBuySuccess() {
    // given
    trade = Trade.builder()
            .rsEventId(10)
            .amount(10)
            .rank(1)
            .build();

    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("热搜10")
                    .id(10)
                    .keyword("hots")
                    .voteNum(10)
                    .rank(1)
                    .build();

    TradeDto oldtradeDto = TradeDto.builder()
            .rsEvent(rsEventDto)
            .rank(1)
            .amount(8)
            .build();


    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(oldtradeDto));
    when(rsEventRepository.count()).thenReturn((long) 5);

    // when
    rsService.buy(trade,1);

    // then
    verify(tradeRepository)
            .findByRank(1);

    verify(rsEventRepository).save(rsEventDto);
    verify(responseEntity).equals(status().isOk());
  }

  @Test
  void shouldFirstBuySuccess() {
    // given
    trade = Trade.builder()
            .rsEventId(2)
            .amount(10)
            .rank(1)
            .build();

    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("热搜2")
                    .id(2)
                    .keyword("hots")
                    .voteNum(10)
                    .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.empty());
    when(rsEventRepository.count()).thenReturn((long) 5);

    // when
    rsService.buy(trade,2);

    // then
    verify(tradeRepository).save(TradeDto.builder()
                            .amount(10).rank(1).rsEvent(rsEventDto).build());

    verify(rsEventRepository).save(rsEventDto);

    verify(responseEntity).equals(status().isOk());
  }

  @Test
  void shouldBuySuccess() {
    // given
    trade = Trade.builder()
            .rsEventId(2)
            .amount(10)
            .rank(1)
            .build();

    RsEventDto oldrsEventDto =
            RsEventDto.builder()
                    .eventName("热搜1")
                    .id(1)
                    .keyword("hots")
                    .voteNum(10)
                    .rank(1)
                    .build();
    rsEventRepository.save(oldrsEventDto);

    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("热搜2")
                    .id(2)
                    .keyword("hots")
                    .voteNum(10)
                    .build();

    TradeDto oldtradeDto = TradeDto.builder()
            .rsEvent(oldrsEventDto)
            .rank(1)
            .amount(8)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(oldtradeDto));
    when(rsEventRepository.count()).thenReturn((long) 5);

    // when
    rsService.buy(trade,2);

    // then
    verify(tradeRepository).save(TradeDto.builder()
            .amount(10).rank(1).rsEvent(rsEventDto).build());

    verify(rsEventRepository).save(rsEventDto);
    verify(responseEntity).equals(status().isOk());
  }

  @Test
  void shouldBuyFailureWhenRsEventIsNull() {
    // given
    trade = Trade.builder()
            .rsEventId(1)
            .amount(10)
            .rank(1)
            .build();

    TradeDto oldtradeDto = TradeDto.builder()
            .rank(1)
            .amount(8)
            .build();


    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.empty());
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(oldtradeDto));
    when(rsEventRepository.count()).thenReturn((long) 5);

    // when
    rsService.buy(trade, 1);
    // then
    verify(responseEntity).equals(status().isBadRequest());
  }

  @Test
  void shouldBuyFailureWhenAccountIsSmall() {
    // given
    // given
    trade = Trade.builder()
            .rsEventId(2)
            .amount(10)
            .rank(1)
            .build();

    RsEventDto oldrsEventDto =
            RsEventDto.builder()
                    .eventName("热搜1")
                    .id(1)
                    .keyword("hots")
                    .voteNum(10)
                    .rank(1)
                    .build();
    rsEventRepository.save(oldrsEventDto);

    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("热搜2")
                    .id(2)
                    .keyword("hots")
                    .voteNum(10)
                    .build();

    TradeDto oldtradeDto = TradeDto.builder()
            .rsEvent(oldrsEventDto)
            .rank(1)
            .amount(100)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(oldtradeDto));
    when(rsEventRepository.count()).thenReturn((long) 5);

    // when
    rsService.buy(trade, 2);

    // then
    verify(responseEntity).equals(status().isBadRequest());
  }

  @Test
  void shouldBuyFailureWhenRankIsLarger() {
    // given
    // given
    trade = Trade.builder()
            .rsEventId(2)
            .amount(10)
            .rank(100)
            .build();

    RsEventDto oldrsEventDto =
            RsEventDto.builder()
                    .eventName("热搜1")
                    .id(1)
                    .keyword("hots")
                    .voteNum(10)
                    .rank(1)
                    .build();
    rsEventRepository.save(oldrsEventDto);

    RsEventDto rsEventDto =
            RsEventDto.builder()
                    .eventName("热搜2")
                    .id(2)
                    .keyword("hots")
                    .voteNum(10)
                    .build();

    TradeDto oldtradeDto = TradeDto.builder()
            .rsEvent(oldrsEventDto)
            .rank(1)
            .amount(8)
            .build();

    when(rsEventRepository.findById(anyInt())).thenReturn(Optional.of(rsEventDto));
    when(tradeRepository.findByRank(anyInt())).thenReturn(Optional.of(oldtradeDto));
    when(rsEventRepository.count()).thenReturn((long) 5);

    // when
    rsService.buy(trade, 2);

    // then
    verify(responseEntity).equals(status().isBadRequest());
  }


}
