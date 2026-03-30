package dujiacun.userservice.entity.dto;

import lombok.Data;

import java.util.Date;
@Data
public class UserResponseDto {
    private String userName;
    private String passWord;
    private Date lastLoginDate;

    public UserResponseDto(String userName,String passWord,Date lastLoginDate) {
        this.userName = userName;
        this.passWord = passWord;
        this.lastLoginDate = lastLoginDate;
    }
}
