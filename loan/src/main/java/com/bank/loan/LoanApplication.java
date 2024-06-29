package com.bank.loan;

import com.bank.loan.dto.LoansContactInfoDto;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing(auditorAwareRef = "auditAwareImpl")
@SpringBootApplication
@EnableConfigurationProperties(value={LoansContactInfoDto.class})
public class LoanApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanApplication.class, args);
	}

}
