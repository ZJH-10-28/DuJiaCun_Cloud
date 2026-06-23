package dujiacun.userservice.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserEntity {
    private Long userId;
    private String userName;
    private String passWord;
    private Integer isAdmin;
    private Date lastLoginDate;
}
