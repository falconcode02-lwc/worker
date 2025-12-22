package io.falconFlow.services.genservice;

import io.falconFlow.entity.LoanApplication;
import io.falconFlow.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanApplicationService {

  @Autowired private LoanApplicationRepository loanApplicationRepository;

  public LoanApplication getLoanApplication(Long loanId) {
    return loanApplicationRepository.findById(loanId).get();
  }

  public LoanApplication create(LoanApplication loanApplication) {
    return loanApplicationRepository.save(loanApplication);
  }

  public LoanApplication update(LoanApplication loanApplication) {
    return loanApplicationRepository.save(loanApplication);
  }
}
