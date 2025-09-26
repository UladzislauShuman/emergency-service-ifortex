package ifortex.shuman.uladzislau.billing.service;

import ifortex.shuman.uladzislau.billing.dto.AccountDetailsDto;

public interface SubscriptionDetailsService {
    AccountDetailsDto getAccountDetailsForUser(Long userId);
}