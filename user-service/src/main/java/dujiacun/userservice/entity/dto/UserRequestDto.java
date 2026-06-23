package dujiacun.userservice.entity.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
@Data
public class UserRequestDto {
    @NotNull
    private Long userId;
    @NotNull
    private String userName;
    @NotNull
    private String passWord;
    private Integer isAdmin;
    private Date lastLoginDate;

}
