package dujiacun.userservice.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class UserRequestDto {
    private Long userId;
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserRequestDto(){};

    public UserRequestDto(Long userId,String userName,String passWord,Date lastLoginDate) {
        this.userId = userId;
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
