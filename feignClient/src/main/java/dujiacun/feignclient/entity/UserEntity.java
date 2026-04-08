package dujiacun.feignclient.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserEntity {
    private Long userId;
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserEntity(){};

    public UserEntity(Long userId, String userName, String passWord, Date lastLoginDate) {
        this.userId = userId;
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
