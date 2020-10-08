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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RsService {
  final RsEventRepository rsEventRepository;
  final UserRepository userRepository;
  final VoteRepository voteRepository;
  final TradeRepository tradeRepository;

  public RsService(RsEventRepository rsEventRepository, UserRepository userRepository, VoteRepository voteRepository,
                   TradeRepository tradeRepository) {
    this.rsEventRepository = rsEventRepository;
    this.userRepository = userRepository;
    this.voteRepository = voteRepository;
    this.tradeRepository = tradeRepository;
  }

  public void vote(Vote vote, int rsEventId) {
    Optional<RsEventDto> rsEventDto = rsEventRepository.findById(rsEventId);
    Optional<UserDto> userDto = userRepository.findById(vote.getUserId());
    if (!rsEventDto.isPresent()
        || !userDto.isPresent()
        || vote.getVoteNum() > userDto.get().getVoteNum()) {
      throw new RuntimeException();
    }
    VoteDto voteDto =
        VoteDto.builder()
            .localDateTime(vote.getTime())
            .num(vote.getVoteNum())
            .rsEvent(rsEventDto.get())
            .user(userDto.get())
            .build();
    voteRepository.save(voteDto);
    UserDto user = userDto.get();
    user.setVoteNum(user.getVoteNum() - vote.getVoteNum());
    userRepository.save(user);
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setVoteNum(rsEvent.getVoteNum() + vote.getVoteNum());
    rsEventRepository.save(rsEvent);
  }

  public ResponseEntity buy(Trade trade, int id) {
    Optional<RsEventDto> rsEventDto= rsEventRepository.findById(id);
    Optional<TradeDto> oldtradeDto = tradeRepository.findByRank(trade.getRank());
    int rsEventDtoLength = Math.toIntExact(rsEventRepository.count());
    if(!rsEventDto.isPresent() ||
        (oldtradeDto.isPresent() && trade.getAmount() < oldtradeDto.get().getAmount()) ||
        trade.getRank() > rsEventDtoLength){
      return ResponseEntity.badRequest().build();
    }
    if(!oldtradeDto.isPresent()){
          TradeDto newtradeDto = TradeDto.builder().amount(trade.getAmount())
            .rank(trade.getRank())
            .rsEvent(rsEventDto.get())
            .build();
          tradeRepository.save(newtradeDto);
    }else if(oldtradeDto.get().getRsEvent().getId() == id){
      oldtradeDto.get().setAmount(trade.getAmount());
    }else if(trade.getAmount() > oldtradeDto.get().getAmount()){
      tradeRepository.delete(oldtradeDto.get());
      rsEventRepository.deleteAllByRank(trade.getRank());
      TradeDto newtradeDto = TradeDto.builder().amount(trade.getAmount())
              .rank(trade.getRank())
              .rsEvent(rsEventDto.get())
              .build();
      tradeRepository.save(newtradeDto);
    }
    RsEventDto rsEvent = rsEventDto.get();
    rsEvent.setRank(trade.getRank());
    rsEventRepository.save(rsEvent);
    return ResponseEntity.ok().build();
  }
}
