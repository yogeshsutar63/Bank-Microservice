package com.eazybytes.accounts.service;

import com.eazybytes.accounts.constants.AccountsConstant;
import com.eazybytes.accounts.dto.AccountsDto;
import com.eazybytes.accounts.dto.AccountsMsgDto;
import com.eazybytes.accounts.dto.CustomerDto;
import com.eazybytes.accounts.entity.Accounts;
import com.eazybytes.accounts.entity.Customer;
import com.eazybytes.accounts.exception.CustomerAlreadyExistsException;
import com.eazybytes.accounts.exception.ResourceNotFoundException;
import com.eazybytes.accounts.mapper.AccountsMapper;
import com.eazybytes.accounts.mapper.CustomerMapper;
import com.eazybytes.accounts.repo.AccountsRepository;
import com.eazybytes.accounts.repo.CustomerRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService{

    private AccountsRepository accountsRepository;
    private CustomerRepository customerRepository;
    private final StreamBridge streamBridge;

    private static final Logger log= LoggerFactory.getLogger(AccountsServiceImpl.class);

    @Override
    public void createAccount(CustomerDto customerDto) {
        Customer customer= CustomerMapper.mapToCustomer(customerDto, new Customer());
        Optional<Customer> optionalCustomer= customerRepository.findByMobileNumber(customerDto.getMobileNumber());
        if (optionalCustomer.isPresent()) {
            throw new CustomerAlreadyExistsException("Customer Already exists with given mobile number"
                    + customerDto.getMobileNumber());
        }

        Customer savedCustomer = customerRepository.save(customer);
        Accounts savedAccount = accountsRepository.save(createNewAccount(customer));
        sendCommunication(savedAccount,savedCustomer);
    }

    private void sendCommunication(Accounts accounts, Customer customer){
        var accountsMsgDto= new AccountsMsgDto(accounts.getAccountNumber(),customer.getName(),customer.getEmail(),customer.getMobileNumber());
        log.info("Sending communication for the details : {}",accountsMsgDto );
        var result=streamBridge.send("sendCommunication-out-O",accountsMsgDto);
        log.info("Is the Communication request successfully processed? {}",result);

    }

    private Accounts createNewAccount(Customer customer) {
        Accounts newAccounts= new Accounts();
        newAccounts.setCustomerId(customer.getCustomerId());
        long randomAccountNumber= 1000000000L + new Random().nextInt(900000000);
        newAccounts.setAccountNumber(randomAccountNumber);
        newAccounts.setAccountType(AccountsConstant.SAVINGS);
        newAccounts.setBranchAddress(AccountsConstant.ADDRESS);
        return newAccounts;
    }

    @Override
    public CustomerDto fetchAccount(String mobileNumber) {
        Customer customer= customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()-> new ResourceNotFoundException("customer","mobileNumber",mobileNumber)
        );
        Accounts accounts= accountsRepository.findByCustomerId(customer.getCustomerId()).orElseThrow(
                ()->new ResourceNotFoundException("Account","customerId",customer.getCustomerId().toString())
        );
        CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
        customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
        return customerDto;
    }

    @Override
    public boolean updateAccount(CustomerDto customerDto) {
        boolean isUpdated= false;
        AccountsDto accountsDto= customerDto.getAccountsDto();
        if(accountsDto!=null){
            Accounts accounts= accountsRepository.findById(accountsDto.getAccountNumber()).orElseThrow(
                    () -> new ResourceNotFoundException("Account", "AccountNumber", accountsDto.getAccountNumber().toString())
            );

            AccountsMapper.mapToAccounts(accounts,accountsDto);
            accounts= accountsRepository.save(accounts);

            Long customerId= accounts.getCustomerId();

            Customer customer= customerRepository.findById(customerId).orElseThrow(
                    ()-> new ResourceNotFoundException("Customer","CustomerID",customerId.toString())
            );

            CustomerMapper.mapToCustomer(customerDto,customer);
            customerRepository.save(customer);
            isUpdated=true;

        }
        return isUpdated;
    }

    @Override
    public boolean deleteAccount(String mobileNumber) {

        Customer customer= customerRepository.findByMobileNumber(mobileNumber).orElseThrow(
                ()-> new ResourceNotFoundException("customer","mobileNumber",mobileNumber)
        );

        accountsRepository.deleteByCustomerId(customer.getCustomerId());
        customerRepository.deleteById(customer.getCustomerId());
        return true;
    }

    @Override
    public boolean updateCommunicationStatus(Long accountNumber) {
        boolean isUpdated= false;
        if(accountNumber!=null){
            Accounts accounts= accountsRepository.findById(accountNumber).orElseThrow(
                    ()->new ResourceNotFoundException("Account","AccountNumber",accountNumber.toString())
            );
            accounts.setCommunicationSw(true);
            accountsRepository.save(accounts);

            isUpdated=true;
        }

        return isUpdated;
    }
}
