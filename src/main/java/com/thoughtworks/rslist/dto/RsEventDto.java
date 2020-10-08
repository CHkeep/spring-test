package com.thoughtworks.rslist.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.*;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "rsEvent")
public class RsEventDto {
  @Id @GeneratedValue private int id;
  private String eventName;
  private String keyword;
  private int voteNum;
  private int rank;
  @ManyToOne private UserDto user;

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "rsEvent")
  private List<VoteDto> voteDtos;

  @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "rsEvent")
  private List<TradeDto> tradeDtos;
}
