package Gamehub.domain;

import java.util.Date;
import java.util.Set;
public class Event {
    private Long id;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private Set<User> participants;
}
