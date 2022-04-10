package com.tennis.event;

import com.tennis.account.Account;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@NamedEntityGraph(
        name = "Enrollment.withEventAndGroup",
        attributeNodes = {
                @NamedAttributeNode(value = "event",subgraph = "group")
        },
        subgraphs = @NamedSubgraph(name = "group",attributeNodes = @NamedAttributeNode("group"))
)

@Entity
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class Enrollment {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Event event;

    @ManyToOne
    private Account account;

    private LocalDateTime enrolledAt;

    private boolean accepted;

    private boolean attended;
}

