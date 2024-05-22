package hello.springdb2.apply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("aop class={}", callService.getClass());
    }

    @Test
    void externalCallV2() {
        callService.external();
        /*
        external 내부에서 호출되는 internal 메소드는 트랜잭션이 적용된다.
        internal 메소드가 this 를 통해 호출되는 것이 아니고 외부에서 실행되기 때문에 트랜잭션 AOP의 프록시를 통해 호출된다.
         */
    }

    @TestConfiguration
    static class InternalCalkV1Config {

        @Bean
        CallService callService() {
            return new CallService(internalService());
        }

        @Bean
        InternalService internalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("external call");
            printTxInfo();
            internalService.internal();
        }


        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive={}", txActive);
        }

    }

    static class InternalService {
        @Transactional
        public void internal() {
            log.info("internal call");
            printTxInfo();
        }

        private void printTxInfo() {
            boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("txActive={}", txActive);
        }
    }
}
