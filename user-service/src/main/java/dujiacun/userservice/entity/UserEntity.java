package dujiacun.userservice.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserEntity {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserEntity(String userName, String passWord, Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
