package com.devsuperior.dscommerce.user;

import java.time.LocalDate;

import org.springframework.test.util.ReflectionTestUtils;

import com.devsuperior.dscommerce.user.entity.Role;
import com.devsuperior.dscommerce.user.entity.User;

public final class UserFixture {

	public static final Long CLIENT_USER_ID = 1L;

	public static final String CLIENT_EMAIL = "maria@gmail.com";

	public static final String ADMIN_EMAIL = "alex@gmail.com";

	public static final String PASSWORD = "123456";

	public static final String CLIENT_AUTHORITY = "ROLE_CLIENT";

	public static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

	private UserFixture() {
	}

	public static User aClientUser() {
		User user = new User(
				"Maria Brown",
				CLIENT_EMAIL,
				"988888888",
				LocalDate.of(2001, 7, 25),
				"$2a$10$BZEayVp6X1Ry93e44/Rnze0hpK5J3ThbAdUm2OzH.GSWjA4zmtGHW");
		ReflectionTestUtils.setField(user, "userId", CLIENT_USER_ID);
		user.getRoles().add(new Role(CLIENT_AUTHORITY));
		return user;
	}

}
