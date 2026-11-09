package com.devsuperior.dscommerce.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.devsuperior.dscommerce.security.AuthenticatedUser;
import com.devsuperior.dscommerce.user.UserFixture;
import com.devsuperior.dscommerce.user.dto.UserDTO;
import com.devsuperior.dscommerce.user.exception.UserNotFoundException;
import com.devsuperior.dscommerce.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private AuthenticatedUser authenticatedUser;

	@InjectMocks
	private UserService userService;

	@Nested
	class LoadUserByUsername {

		@Test
		void shouldReturnUserDetailsWithAuthoritiesWhenEmailExists() {
			given(userRepository.findWithRolesByEmail(UserFixture.CLIENT_EMAIL))
					.willReturn(Optional.of(UserFixture.aClientUser()));

			UserDetails userDetails = userService.loadUserByUsername(UserFixture.CLIENT_EMAIL);

			assertThat(userDetails.getUsername()).isEqualTo(UserFixture.CLIENT_EMAIL);
			assertThat(userDetails.getAuthorities())
					.extracting(grantedAuthority -> grantedAuthority.getAuthority())
					.containsExactly(UserFixture.CLIENT_AUTHORITY);
		}

		@Test
		void shouldThrowUsernameNotFoundExceptionWhenEmailDoesNotExist() {
			given(userRepository.findWithRolesByEmail(UserFixture.CLIENT_EMAIL)).willReturn(Optional.empty());

			assertThatThrownBy(() -> userService.loadUserByUsername(UserFixture.CLIENT_EMAIL))
					.isInstanceOf(UsernameNotFoundException.class);
		}

	}

	@Nested
	class FindCurrentUser {

		@Test
		void shouldReturnAuthenticatedUserWhenUserExists() {
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(userRepository.findWithRolesByEmail(UserFixture.CLIENT_EMAIL))
					.willReturn(Optional.of(UserFixture.aClientUser()));

			UserDTO userDTO = userService.findCurrentUser();

			assertThat(userDTO.userId()).isEqualTo(UserFixture.CLIENT_USER_ID);
			assertThat(userDTO.roles()).containsExactly(UserFixture.CLIENT_AUTHORITY);
		}

		@Test
		void shouldThrowUserNotFoundExceptionWhenUserDoesNotExist() {
			given(authenticatedUser.getEmail()).willReturn(UserFixture.CLIENT_EMAIL);
			given(userRepository.findWithRolesByEmail(UserFixture.CLIENT_EMAIL)).willReturn(Optional.empty());

			assertThatThrownBy(() -> userService.findCurrentUser()).isInstanceOf(UserNotFoundException.class);
		}

	}

}
